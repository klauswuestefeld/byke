//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byke;

import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;
import java.util.*;


/**
 * The main plugin class to be used in the desktop.
 */
public class BykePlugin extends AbstractUIPlugin {
	// The shared instance.
	private static BykePlugin plugin;
	// Resource bundle.
	private ResourceBundle resourceBundle;


	/**
	 * The constructor.
	 */
	public BykePlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("byke.BykePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static BykePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = BykePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
