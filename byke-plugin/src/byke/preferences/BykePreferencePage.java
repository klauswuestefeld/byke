package byke.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import byke.BykePlugin;


/**
 * This class represents the Byke preference page.
 * <p>
 * This preference page can be used to control the list of packages to exclude from the dependency analysis.
 */
public class BykePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public BykePreferencePage() {
		super(GRID);
		setPreferenceStore(BykePlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the package exclude list field editor.
	 */
	public void createFieldEditors() {
		addField(new PatternExcludeListEditor(PreferenceConstants.P_PATTERN_EXCLUDES, "&Classes excluded from dependency graphs: (regex patterns)", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {}

}