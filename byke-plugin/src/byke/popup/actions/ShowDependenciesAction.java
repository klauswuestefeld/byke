//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import byke.views.IBykeView;


public class ShowDependenciesAction implements IViewActionDelegate {

	private ISelection _selection;


	@Override
	public void init(IViewPart view) {
		// Apparently never called.
	}

	
	@Override
	public void run(IAction ignored) {
		if (_selection == null) return;
		bykeView().showDependencies(_selection);
	}

	
	private IBykeView bykeView() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IBykeView result = null;
		try {
			result = (IBykeView)activePage.showView(IBykeView.PERSPECTIVE_ID);
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}

		activePage.activate(result);
		return result;
	}

	
	@Override
	public void selectionChanged(IAction ignored, ISelection selection) {
		_selection = selection;
	}
}
