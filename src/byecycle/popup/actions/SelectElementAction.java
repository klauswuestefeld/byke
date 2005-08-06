//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import byecycle.views.ByecycleView;
import byecycle.views.IByecycleView;

public class SelectElementAction implements IViewActionDelegate {

	private ISelection _selection;

	public void init(IViewPart view) {
	}

	public void run(IAction action) {
		if (_selection == null)
			return;
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IByecycleView _viewpart = (IByecycleView) activePage.showView(IByecycleView.PERSPECTIVE_ID);
			activePage.activate(_viewpart);
			_viewpart.selectionChanged(_selection);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		_selection = selection;
	}
}
