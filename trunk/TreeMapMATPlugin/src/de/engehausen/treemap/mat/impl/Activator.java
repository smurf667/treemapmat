package de.engehausen.treemap.mat.impl;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * TreeMapMAT plugin activator.
 * Statically stores the plugin object (expected to be a singleton).
 */
public class Activator extends AbstractUIPlugin {

	private static Activator plugin;

	/**
	 * Returns the plugin instance.
	 * @return the plugin instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	@Override
	// non-javadoc: see superclass
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	// non-javadoc: see superclass
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
}
