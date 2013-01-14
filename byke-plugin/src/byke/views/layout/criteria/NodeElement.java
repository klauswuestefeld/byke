//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views.layout.criteria;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.dependencygraph.Node;
import byke.views.layout.Coordinates;


public class NodeElement {

	public NodeElement(Node<?> node, Rectangle bounds) {
		_node = node;

		_aura = createAura(bounds);
		_auraOffsetX = _aura.width / 2;
		_auraOffsetY = _aura.height / 2;
		centerAura();
	}


	private final Node<?> _node;

	public int _x;
	public int _y;

	private float _resultingForceX;
	private float _resultingForceY;
	private float _stress;

	private final Rectangle _aura;
	private final int _auraOffsetX;
	private final int _auraOffsetY;


	public Node<?> node() {
		return _node;
	}

	
	public String name() {
		return _node.name();
	}

	
	public Coordinates position() {
		return new Coordinates(_x, _y);
	}

	
	public void clearForces() {
		_resultingForceX = 0;
		_resultingForceY = 0;
		_stress = 0f;
	}

	
	protected void addForceComponents(float x, float y) {
		_resultingForceX += x;
		_resultingForceY += y;
		_stress += (float)Math.hypot(x, y);
	}

	
	public float resultingForceX() { return _resultingForceX; }
	public float resultingForceY() { return _resultingForceY; }
	float stress() { return _stress; }
	
	
	private void assertValidNumber(float n) {
		if (Float.isNaN(n)) throw new IllegalArgumentException("NaN received instead of a valid number.");
	}

	
	public boolean dependsDirectlyOn(NodeElement other) {
		return _node.dependsDirectlyOn(other.node());
	}

	
	private Rectangle createAura(Rectangle bounds) {
		Rectangle ret = new Rectangle();
		ret.width = bounds.width + (Constants.AURA_THICKNESS * 2);
		ret.height = bounds.height + (Constants.AURA_THICKNESS * 2);
		return ret;
	}

	
	public Rectangle aura() {
		return _aura;
	}

	
	private void centerAura() {
		_aura.x = _x - _auraOffsetX;
		_aura.y = _y - _auraOffsetY;
	}


	public void position(Coordinates c) {
		position(c._x, c._y);
	}

	
	private void position(int x, int y) {
		assertValidNumber(x);
		assertValidNumber(y);

		_x = x;
		_y = y;

		centerAura();
	}

	
	public void move(int dx, int dy) {
		position(_x + dx, _y + dy);
	}

	public void addForceComponents(float x, float y, NodeElement counterpart) {
		addForceComponents(x, y);
		counterpart.addForceComponents(-x, -y);
	}

}
