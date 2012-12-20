//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import byke.views.BykeView;


public class ToggleActiveHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BykeView bikeView = (BykeView) HandlerUtil.getActivePart(event);
		boolean checked = HandlerUtil.toggleCommandState(event.getCommand());
		
		bikeView.togglePaused(!checked);
		
		return null;
	}
}
