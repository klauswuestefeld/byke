package byke.preferences;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

public class MergeClassListEditor extends ListEditor {
	
	protected MergeClassListEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < items.length; i++)
			result.append(items[i]).append(" \n");

		return result.toString();
	}

	@Override
	protected String getNewInputObject() {
		InputDialog dialog = new InputDialog(
				getShell(), "Merge classes with similar names", 
				"Enter the class name pattern you want to merge:", null, new IInputValidator() {
					@Override public String isValid(String newText) {
						return newText.matches("\\*\\w*||\\w*\\*") && !newText.isEmpty() ? null : "Invalid pattern. Try *[any word] or [any word]*";
					}
				});
		if (dialog.open() == Window.OK) {
			return dialog.getValue();
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return stringList.split("\\s+");
	}
}
