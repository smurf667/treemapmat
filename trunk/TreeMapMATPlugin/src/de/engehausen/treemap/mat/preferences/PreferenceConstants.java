package de.engehausen.treemap.mat.preferences;

import org.eclipse.swt.graphics.RGB;

/**
 * Constants for the TreeMap preferences.
 */
public interface PreferenceConstants {

	/** key used to store the color matching rules in the preference store */
	String TREEMAP_PREF_KEY = "de.engehausen.treemap.mat.colrules"; //$NON-NLS-1$

	/** default color matching rules */
	String DEFAULT_COLOR_RULES = 
		"B0B0B0\t" + // default color //$NON-NLS-1$
		"2F74D0\tbyte[]\tfalse\t" + //$NON-NLS-1$
		"DFE32D\tchar[]\tfalse\t" + //$NON-NLS-1$
		"C9EAF3\tjava.lang.String\tfalse\t" + //$NON-NLS-1$
		"8CD1E6\tclass.*\ttrue\t" + //$NON-NLS-1$
		"FF8A8A\tjava\\.lang\\..*\ttrue\t" + //$NON-NLS-1$
		"36F200\tjava\\.util\\..*\ttrue"; //$NON-NLS-1$

	/** default color */
	RGB DEFAULT_COLOR = new RGB(176, 176, 176); // this must match the color specified above

}
