//Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byke.views.layout.criteria;

import byke.views.layout.Coordinates;


public abstract class GraphElement {

	public abstract Coordinates position();

	public abstract void addForceComponents(float f, float g);

	public void addForceComponents(float x, float y, GraphElement counterpart) {
		addForceComponents(x, y);
		counterpart.addForceComponents(-x, -y);
	}

}
