package de.engehausen.treemap.mat.impl;

/**
 * A well-performing stack of <code>int</code>s.
 */
public class IntStack {
	
	private int[] data;
	private int pos;

	/**
	 * Creates the stack with the given initial size.
	 * @param initialSize the initial size, must be &gt; 0.
	 */
	public IntStack(final int initialSize) {
		data = new int[initialSize];
		pos = 0;
	}

	/**
	 * Indicates if the stack is empty or not.
	 * @return <code>true</code> if the stack is empty, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return pos == 0;
	}

	/**
	 * Removes the topmost element from the stack.
	 * @return the topmost element of the stack.
	 */
	public int pop() {
		return data[--pos];
	}

	/**
	 * Puts the given value onto the stack
	 * @param val the value to put onto the stack.
	 */
	public void push(final int val) {
		if (pos == data.length) {
			// resize by making the array 50% bigger
			final int resized[] = new int[data.length*3/2];
			System.arraycopy(data, 0, resized, 0, data.length);
			data = resized;
		}
		data[pos++] = val;
	}

	/**
	 * Returns the number of elements held by the stack.
	 * @return the number of elements held by the stack.
	 */
	public int size() {
		return pos;
	}

	/**
	 * The current maximum size of elements the stack can
	 * hold without resizing (if the stack cannot hold the
	 * elements, it will resize).
	 * @return current maximum number of elements the stack can hold.
	 */
	public int max() {
		return data.length;
	}
	
}
