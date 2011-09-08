//Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byke.views.layout.ui;

import org.eclipse.draw2d.IFigure;


public abstract class GraphFigure {

	private IFigure _figure;


	IFigure figure() {
		return _figure == null ? _figure = produceFigure() : _figure;
	}

	abstract IFigure produceFigure();

}
