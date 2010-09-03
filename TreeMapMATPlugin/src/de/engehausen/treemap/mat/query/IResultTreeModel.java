package de.engehausen.treemap.mat.query;

import org.eclipse.mat.query.IResult;

import de.engehausen.treemap.IWeightedTreeModel;
import de.engehausen.treemap.mat.ISnapshotNode;

/**
 * The result of the {@link HeapTreeMapQuery}.
 */
public interface IResultTreeModel extends IResult {

	/**
	 * Returns the weighted tree model of snapshot nodes.
	 * @return the weighted tree model of snapshot nodes, never <code>null</code>.
	 */
	IWeightedTreeModel<ISnapshotNode> getTreeModel();
	
}
