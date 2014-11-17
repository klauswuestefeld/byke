package byke.views.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import byke.views.BykeView;

public class ExportDependencyAnalysisHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BykeView bikeView = (BykeView)HandlerUtil.getActivePart(event);
		bikeView.exportDependencyAnalysis();

		return null;
	}
	
}