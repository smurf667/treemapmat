package de.engehausen.treemap.mat;

import org.eclipse.osgi.util.NLS;

/**
 * Localized strings for tree map UI.
 */
public class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "de.engehausen.treemap.mat.messages"; //$NON-NLS-1$
	
	/** "add" text */
	public static String STR_ADD;
	/** "already initialized" error text */
	public static String STR_ALREADY_INITIALIZED;
	/** "color" text */
	public static String STR_COLOR;
	/** "computation canceled" message */
	public static String STR_COMP_CANCELLED;
	/** "convert graph to weighted tree" text */
	public static String STR_CONVERT_GRAPH_TREE;
	/** "default color" text */
	public static String STR_DEFAULT_COLOR;
	/** "delete" text */
	public static String STR_DELETE;
	/** pane title text */
	public static String STR_PANE_TITLE;
	/** "pattern" text */
	public static String STR_PATTERN;
	/** "pattern-or-substring" text */
	public static String STR_PATTERN_OR_SUBSTR;
	/** "preferences description" text */
	public static String STR_PREF_DESC;
	/** "regular expression" abbreviation */
	public static String STR_REGULAR_EXPRESSION;
	/** "stack not empty" error text */
	public static String STR_STACK_NOT_EMPTY;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
