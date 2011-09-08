// Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
// This is free software. See the license distributed along with this file.

package byke.views.layout.criteria;

import byke.views.layout.Coordinates;


public class DependencyElement extends GraphElement {

	private final NodeElement _dependent;
	private final NodeElement _provider;


	public DependencyElement(NodeElement dependent, NodeElement provider) {
		_dependent = dependent;
		_provider = provider;

	}

	public Coordinates position() {
		Coordinates p1 = _dependent.position();
		Coordinates p2 = _provider.position();
		float centerX = (p1._x + p2._x) / 2;
		float centerY = (p1._y + p2._y) / 2;
		return new Coordinates(centerX, centerY);
	}

	protected void addForceComponents(float x, float y) {
		float halfX = x / 2;
		float halfY = y / 2;
		_dependent.addForceComponents(halfX, halfY);
		_provider.addForceComponents(halfX, halfY);
	}

}