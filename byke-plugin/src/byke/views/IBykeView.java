/*
 * Created on 2005年8月1日
 * $id$
 */
package byke.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;


public interface IBykeView extends ISelectionListener, IWorkbenchPart {

	public static final String PERSPECTIVE_ID = "byke.views.BykeView";


	public void showDependencies(ISelection _selection);

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection);


}