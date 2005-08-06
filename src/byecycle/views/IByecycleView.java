/*
 * Created on 2005年8月1日
 * $id$
 */
package byecycle.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;

public interface IByecycleView extends ISelectionProvider, IWorkbenchPart{

	public static final int ACTIVACITY = 1;

	public static final String PERSPECTIVE_ID = "byecycle.views.ByecycleView";

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public abstract void selectionChanged(IWorkbenchPart part, ISelection selection);

	public abstract void selectionChanged(ISelection selection);

	public abstract void addSelectionChangedListener(ISelectionChangedListener listener);

	public abstract ISelection getSelection();

	public abstract void removeSelectionChangedListener(ISelectionChangedListener listener);

	public abstract void setSelection(ISelection selection);

	public abstract void toggleActive(boolean pause);

	public abstract void addPropertyListener(IPropertyListener action);

	public abstract boolean isPaused();

}