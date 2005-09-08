//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.preferences;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

public class PatternExcludeListEditor extends ListEditor {
	protected PatternExcludeListEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			result.append(items[i]).append(" \n");
		}
		return result.toString();
	}

	@Override
	protected String getNewInputObject() {
		InputDialog dialog = new InputDialog(getShell(), "Input full qualified class name pattern", "Enter a regex pattern to be excluded:", "",
				new IInputValidator() {
					public String isValid(String newText) {
						try {
							Pattern.compile(newText);
							return null;
						} catch (PatternSyntaxException e) {
							return e.getLocalizedMessage();
						}
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
