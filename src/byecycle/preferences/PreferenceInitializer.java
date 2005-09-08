package byecycle.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import byecycle.ByecyclePlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

       public void initializeDefaultPreferences() {
               IPreferenceStore store = ByecyclePlugin.getDefault()
                               .getPreferenceStore();
               store.setDefault(PreferenceConstants.P_PACKAGE_EXCLUDES,
                               "java.lang java.util");
               store.setDefault(PreferenceConstants.P_PATTERN_EXCLUDES,"");
       }

}
