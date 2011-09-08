//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.GraphElement;


public interface Force {

	void applyTo(GraphElement element1, GraphElement element2);

}
