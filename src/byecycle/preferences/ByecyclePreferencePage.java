package byecycle.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import byecycle.ByecyclePlugin;

/**
 * This class represents the Byecycle preference page.
 * <p>
 * This preference page can be used to control the list of packages to exclude
 * from the dependency analysis.
 */
public class ByecyclePreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public ByecyclePreferencePage() {
        super(GRID);
        setPreferenceStore(ByecyclePlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the package exclude list field editor.
     */
    public void createFieldEditors() {
        addField(new PackageExcludeListEditor(PreferenceConstants.P_PACKAGE_EXCLUDES,
                "&Packages e&xcluded in dependency graphs:", getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }

}