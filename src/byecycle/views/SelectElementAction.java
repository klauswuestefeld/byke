//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class SelectElementAction implements IViewActionDelegate {

    public void init(IViewPart view) {
    }

    public void run(IAction action) {
        if (_selection == null)
            return;
        final IWorkbenchPage activePage = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage();
        try {
            ByecycleView _viewpart = (ByecycleView) activePage.showView(ByecycleView.PERSPECTIVE_ID);
            activePage.activate(_viewpart);
            _viewpart.selectionChanged(_selection);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    ISelection _selection;

    public void selectionChanged(IAction action, ISelection selection) {
        _selection = selection;
    }
}
