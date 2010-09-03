package de.engehausen.treemap.mat.impl;

import org.eclipse.mat.query.ResultMetaData;

import de.engehausen.treemap.IWeightedTreeModel;
import de.engehausen.treemap.mat.ISnapshotNode;
import de.engehausen.treemap.mat.query.IResultTreeModel;

/**
 * Container object to hold the results of a query for a weighted
 * tree model.
 */
public class ResultTreeModelImpl implements IResultTreeModel {
	
	private final IWeightedTreeModel<ISnapshotNode> model;
	private final ResultMetaData metaData;

	/**
	 * Creates the result.
	 * @param aModel the model to expose, must not be <code>null</code>.
	 * @param aMetaData the meta data of the result, may be <code>null</code>.
	 */
	public ResultTreeModelImpl(final IWeightedTreeModel<ISnapshotNode> aModel, final ResultMetaData aMetaData) {
		model = aModel;
		metaData = aMetaData;
	}

	@Override
	// non-javadoc: see interface
	public IWeightedTreeModel<ISnapshotNode> getTreeModel() {
		return model;
	}

	@Override
	// non-javadoc: see interface
	public ResultMetaData getResultMetaData() {
		return metaData;
	}

}
