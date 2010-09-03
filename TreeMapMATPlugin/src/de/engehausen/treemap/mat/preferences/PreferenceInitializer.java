package de.engehausen.treemap.mat.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.engehausen.treemap.mat.impl.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements PreferenceConstants {
	
	@Override
	// non-javadoc: see superclass
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final String prefs = store.getString(TREEMAP_PREF_KEY);
		if (prefs == null || prefs.length()==0) {
			store.putValue(TREEMAP_PREF_KEY, DEFAULT_COLOR_RULES);
		}
	}

}
