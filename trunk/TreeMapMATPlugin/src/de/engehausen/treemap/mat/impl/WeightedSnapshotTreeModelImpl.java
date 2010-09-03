package de.engehausen.treemap.mat.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.BitField;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.Units;

import de.engehausen.treemap.ILabelProvider;
import de.engehausen.treemap.IRectangle;
import de.engehausen.treemap.ITreeModel;
import de.engehausen.treemap.IWeightedTreeModel;
import de.engehausen.treemap.mat.ISnapshotNode;
import de.engehausen.treemap.mat.ISnapshotProvider;
import de.engehausen.treemap.mat.Messages;

/**
 * Result tree model (a weighted tree model implementation). To become
 * fully initialized, the {@link #initialize(IProgressListener)} method
 * must be called after the model creation.
 * <p>The method {@link #initialize(IProgressListener)} computes the weighted
 * tree model by traversing the graph of the snapshot nodes as formed by its
 * GC root nodes, turning it into a tree and assigning the accumulated weight
 * of its individual nodes accordingly. This may be a naive approach and the
 * built tree is just <i>one of many</i> possible trees that could be formed
 * from the graph. It will not be safe to conclude certain containments just
 * from the tree map; the tree map of the heap can just give an idea of the
 * object distribution and size. "Normal" analysis of the heap will be required
 * to fully understand the object reference situation in the heap and how to
 * possibly reduce the heap size.
 * <p>This tree model is <b>not thread-safe</b>.
 */
public class WeightedSnapshotTreeModelImpl implements IWeightedTreeModel<ISnapshotNode>, ILabelProvider<ISnapshotNode>, ISnapshotProvider {
	
	protected static Iterator<ISnapshotNode> EMPTY_ITERATOR = Collections.<ISnapshotNode>emptyList().iterator();
	protected final long weight[];
	protected final int parent[];
	protected final ISnapshot snapshot;
	
	// temporary objects to better the performance of hasChildren/getChildren
	private Iterator<ISnapshotNode> preparedIterator;
	private int idForIterator;

	/**
	 * Creates the weighted tree model from the snapshot.
	 * @param aSnapshot the snapshot, must not be <code>null</code>.
	 * @param aListener the progress listener
	 * @return the weighted tree model
	 * @throws SnapshotException in case of error
	 */
	public static IWeightedTreeModel<ISnapshotNode> createModel(final ISnapshot aSnapshot, final IProgressListener aListener) throws SnapshotException {
		WeightedSnapshotTreeModelImpl model = new WeightedSnapshotTreeModelImpl(aSnapshot);
		model.initialize(aListener);
		return model;
	}

	/**
	 * Creates an uninitialized tree model.
	 * @param aSnapshot the snapshot to use for building the tree model, must not be <code>null</code>.
	 * @throws SnapshotException in case of error
	 */
	protected WeightedSnapshotTreeModelImpl(final ISnapshot aSnapshot) throws SnapshotException {
		snapshot = aSnapshot;
		final int size = aSnapshot.getSnapshotInfo().getNumberOfObjects();
		weight = new long[size];
		parent = new int[size];
		resetPreparedIterator();
	}

	/**
	 * Computes the weighted tree model information from the snapshot.
	 * The root node is a "virtual" node with Integer.MAX_VALUE for ID
	 * (assumed not to exist in the snapshot), and its children are the
	 * GC roots.
	 * <p>This method must only be called once.
	 * @param progress the progress listener
	 * @throws SnapshotException in case of error
	 */
	protected void initialize(final IProgressListener progress) throws SnapshotException {
		if (weight[0] > 0) {
			throw new SnapshotException(Messages.STR_ALREADY_INITIALIZED);
		}
		final int[] gcroots = snapshot.getGCRoots();
		// begin task, use #gcroots/16 steps for the progress listener
		progress.beginTask(Messages.STR_CONVERT_GRAPH_TREE, gcroots.length>>4);
		final BitField visited = new BitField(weight.length);
		final IntStack stack = new IntStack(400);
		for (int i = gcroots.length - 1; i >= 0; i--) {
			if ( (i&0xf) == 0) { // update every 16 gc roots
				progress.worked(1);
			}
			traverseGCRoot(gcroots[i], visited, stack);
			if (progress.isCanceled()) {
				throw new SnapshotException(Messages.STR_COMP_CANCELLED);
			}
		}
	}

	/**
	 * Build a tree from the object graph starting at each GC root and
	 * compute the summed up weights for each subtree. This is O(n*log(n)),
	 * unfortunately, I think.
	 * 
	 * @param root the root node
	 * @param visited bit field of already visited nodes
	 * @param stack integer stack
	 * @return the summed up weight of the root node
	 * @throws SnapshotException in case of error
	 */
	protected long traverseGCRoot(final int root, final BitField visited, final IntStack stack) throws SnapshotException {
		if (!stack.isEmpty()) {
			throw new IllegalStateException(Messages.STR_STACK_NOT_EMPTY);
		}
		stack.push(root);
		parent[root] = Integer.MAX_VALUE; // root has no parent
		while (!stack.isEmpty()) {
			final int node = stack.pop();
			if (!visited.get(node)) {
				visited.set(node);
				final long w = snapshot.getHeapSize(node);
				// assign node weight
				weight[node] = w;
				int runner = parent[node];
				// update all parent weights
				while (runner != Integer.MAX_VALUE) {
					weight[runner] += w;
					runner = parent[runner];
				}
				final int[] children = snapshot.getOutboundReferentIds(node);
				if (children.length > 0) {
					for (int i = children.length - 1; i >= 0; i--) {
						final int c = children[i];
						// about the "not a gc root" check:
						// it is a bit surprising that by following just
						// outgoing references for one gcroot one can end
						// up at another? I assume that this happens when
						// a root is originally a GC root and then later
						// referenced by other objects.
						if (!visited.get(c) && !snapshot.isGCRoot(c)) {
							parent[c] = node;
							stack.push(c);
						}
					}
				}
			}
		}
		return weight[root];		
	}

	@Override
	// non-javadoc: see interface
	public String getLabel(final ITreeModel<IRectangle<ISnapshotNode>> model, final IRectangle<ISnapshotNode> node) {
		try {
			final int id = node.getNode().getID();
			final long size = snapshot.getHeapSize(id);
			final StringBuilder sb = new StringBuilder(80);
			sb.append(snapshot.getObject(id).getTechnicalName())
			  .append(" (").append(Units.Storage.of(size).format(size)) //$NON-NLS-1$
			  .append("/").append(Units.Storage.of(weight[id]).format(weight[id])) //$NON-NLS-1$
			  .append(")"); //$NON-NLS-1$
			return sb.toString();
		} catch (SnapshotException e) {
			return "???"; //$NON-NLS-1$
		}
	}

	@Override
	public Iterator<ISnapshotNode> getChildren(final ISnapshotNode node) {
		final int id = node.getID();
		if (id == idForIterator) {
			try {
				return preparedIterator;
			} finally {
				resetPreparedIterator();
			}
		} else {
			try {
				return createNodeIterator(id, snapshot, this);
			} catch (SnapshotException e) {
				throw new IllegalStateException(e);
			}			
		}
	}

	@Override
	public ISnapshotNode getParent(final ISnapshotNode node) {
		final int id = node.getID();
		return id!=Integer.MAX_VALUE?createNode(parent[id]):null;
	}

	@Override
	public ISnapshotNode getRoot() {
		return createNode(Integer.MAX_VALUE);
	}

	@Override
	public boolean hasChildren(final ISnapshotNode node) {
		idForIterator = node.getID();
		try {
			preparedIterator = createNodeIterator(idForIterator, snapshot, this);
		} catch (SnapshotException e) {
			throw new IllegalStateException(e);
		}
		return preparedIterator.hasNext();
	}

	@Override
	public long getWeight(ISnapshotNode node) {
		return weight[node.getID()];
	}

	@Override
	public ISnapshot getSnapshot() {
		return snapshot;
	}

	/**
	 * Creates a new snapshot node with the given ID.
	 * Nodes are not referenced here to actually save memory, even
	 * though this might cause more garbage than sensible in other
	 * scenarios.
	 * @param id the ID to use for the node
	 * @return the created node, never <code>null</code>.
	 */
	protected ISnapshotNode createNode(final int id) {
		return new SnapshotNodeImpl(id);
	}

	/**
	 * Resets the "prepared" iterator.
	 */
	private void resetPreparedIterator() {
		preparedIterator = null;
		idForIterator = -1;		
	}
	
	/**
	 * Creates an iterator for the children of a node.
	 * @param parentID the parent ID
	 * @param snapshot the snapshot to use for backing the nodes.
	 * @param aModel the model supplying the nodes in the iterator
	 * @return the iterator, never <code>null</code>.
	 * @throws SnapshotException in case of error.
	 */
	private Iterator<ISnapshotNode> createNodeIterator(final int parentID, final ISnapshot snapshot, final WeightedSnapshotTreeModelImpl aModel) throws SnapshotException {
		final int[] outbound = parentID!=Integer.MAX_VALUE?snapshot.getOutboundReferentIds(parentID):snapshot.getGCRoots();
		if (outbound.length > 0) {
			return new NodeIterator(parentID, outbound, this);
		} else {
			return EMPTY_ITERATOR;
		}
	}

	/**
	 * Node iterator implementation. Calls back to {@link WeightedSnapshotTreeModelImpl#createNode(int)}
	 * to create its elements. The information on children is backed by the
	 * snapshot; the given parents' <i>outgoing</i> references only are used.
	 */
	private static class NodeIterator implements Iterator<ISnapshotNode> {
		
		private final WeightedSnapshotTreeModelImpl model;
		private final int[] outbound;
		private int pos;
		private final int parent;
		
		protected NodeIterator(final int aParent, final int[] outboundReferents, final WeightedSnapshotTreeModelImpl aModel) throws SnapshotException {
			model = aModel;
			outbound = outboundReferents;
			parent = aParent;
			pos = 0;
		}

		@Override
		public boolean hasNext() {
			while (pos < outbound.length && model.parent[outbound[pos]] != parent) {
				// look for next outbound that we consider to be a child
				// of the node 'id'
				pos++;
			}
			return pos < outbound.length;
		}

		@Override
		public ISnapshotNode next() {
			if (pos == outbound.length) {
				throw new NoSuchElementException();
			}
			return model.createNode(outbound[pos++]);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}
