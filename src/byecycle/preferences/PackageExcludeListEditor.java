package byecycle.preferences;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.PackageSelectionDialog;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

public class PackageExcludeListEditor extends ListEditor {

	protected PackageExcludeListEditor(String name, String labelText,
			Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < items.length; i++) {
			result.append(items[i]).append(' ');
		}

		return result.toString();
	}

	@Override
	protected String getNewInputObject() {
		IRunnableContext context = new BusyIndicatorRunnableContext();
		int style = PackageSelectionDialog.F_REMOVE_DUPLICATES
				| PackageSelectionDialog.F_HIDE_DEFAULT_PACKAGE;
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

		PackageSelectionDialog dialog = new PackageSelectionDialog(getShell(),
				context, style, scope);
		dialog.setTitle("Package Selection");
		dialog.setMessage("Choose a package to be excluded:");

		if (dialog.open() == Window.OK) {
			IPackageFragment res = (IPackageFragment) dialog.getResult()[0];

			return res.getElementName();
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return stringList.split(" ");
	}

}
