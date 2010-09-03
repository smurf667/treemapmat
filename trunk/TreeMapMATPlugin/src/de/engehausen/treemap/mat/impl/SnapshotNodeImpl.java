package de.engehausen.treemap.mat.impl;

import de.engehausen.treemap.mat.ISnapshotNode;

/**
 * Implementation of a snapshot node.
 */
public class SnapshotNodeImpl implements ISnapshotNode {
	
	private final int id;

	/**
	 * Creates the snapshot node with the given ID.
	 * @param anID the ID of the node
	 */
	public SnapshotNodeImpl(final int anID) {
		id = anID;
	}

	@Override
	// non-javadoc: see interface
	public int getID() {
		return id;
	}

	@Override
	// non-javadoc: see interface
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof SnapshotNodeImpl) {
			return id == ((SnapshotNodeImpl) o).id;
		} else {
			return false;
		}
	}

	@Override
	// non-javadoc: see interface
	public int hashCode() {
		return id;
	}

}
