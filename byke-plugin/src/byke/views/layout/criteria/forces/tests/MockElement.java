package byke.views.layout.criteria.forces.tests;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.views.layout.Coordinates;
import byke.views.layout.criteria.NodeElement;

public class MockElement extends NodeElement {

	String forces = "";
	private final Coordinates position;

	MockElement(int x, int y) {
		super(null, new Rectangle(0,0,0,0), null);
		position = new Coordinates(x, y);
	}
	
	@Override
	public Coordinates position() {
		return position;
	}

	@Override
	public void addForceComponents(float x, float y) {
		forces += "x=" + x + " y=" + y + ";";
	}

}
