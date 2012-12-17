package byke.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import byke.BykePlugin;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BykePlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATTERN_EXCLUDES, ".*Exception$");
		store.setDefault(PreferenceConstants.P_PATTERN_MERGE_CLASS, "*Class");
	}

}
