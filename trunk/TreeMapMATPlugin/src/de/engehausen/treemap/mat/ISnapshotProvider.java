package de.engehausen.treemap.mat;

import org.eclipse.mat.snapshot.ISnapshot;

import de.engehausen.treemap.IWeightedTreeModel;

/**
 * Returns the associated snapshot.
 * It is <b>necessary</b> for a {@link IWeightedTreeModel} implementation
 * of {@link ISnapshotNode} nodes to implement the provider interface
 * for the treemap pane to work properly in MAT.
 */
public interface ISnapshotProvider {

	/**
	 * Returns the snapshot.
	 * @return the snapshot; may be <code>null</code> if
	 * none is associated with the object.
	 */
	ISnapshot getSnapshot();

}
