package de.engehausen.treemap.mat.query;

import org.eclipse.mat.query.IQuery;
import org.eclipse.mat.query.IResult;
import org.eclipse.mat.query.annotations.Argument;
import org.eclipse.mat.query.annotations.CommandName;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;

import de.engehausen.treemap.mat.impl.ResultTreeModelImpl;
import de.engehausen.treemap.mat.impl.WeightedSnapshotTreeModelImpl;

/**
 * The MAT query which shows the heap as a tree map.
 */
@CommandName("heap_treemap")
public class HeapTreeMapQuery implements IQuery {

	@Argument
	public ISnapshot snapshot;

	@Override
	public IResult execute(final IProgressListener progress) throws Exception {
		return new ResultTreeModelImpl(WeightedSnapshotTreeModelImpl.createModel(snapshot, progress), null);
	}

}
