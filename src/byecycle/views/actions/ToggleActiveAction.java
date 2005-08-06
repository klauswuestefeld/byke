//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import byecycle.views.ByecycleView;
import byecycle.views.IByecycleView;

public class ToggleActiveAction implements IViewActionDelegate, IPropertyListener {
	private IByecycleView _viewpart;

	private IAction _action;

	public void init(IViewPart view) {
		_viewpart = (ByecycleView) view;
		_viewpart.addPropertyListener(this);
	}

	public void run(IAction action) {
		_viewpart.toggleActive(action.isChecked());
	}

	public void selectionChanged(IAction action, ISelection selection) {
		_action = action;
	}

	public void propertyChanged(Object source, int propId) {
		assert _viewpart == source;
		if (IByecycleView.ACTIVACITY == propId)
			return;
		_action.setChecked(_viewpart.isPaused());
	}
}
