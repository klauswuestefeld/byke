//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import byke.views.BykeView;
import byke.views.IBykeView;


public class ToggleActiveAction implements IViewActionDelegate {

	private IBykeView _view;


	@Override
	public void init(IViewPart view) {
		_view = (BykeView)view;
	}

	@Override
	public void run(IAction action) {
		_view.togglePaused(action.isChecked());
	}

	@Override
	public void selectionChanged(IAction ignored, ISelection ignoredToo) {}

}
