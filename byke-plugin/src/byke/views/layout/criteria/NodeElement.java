//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views.layout.criteria;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.dependencygraph.Node;
import byke.views.layout.Coordinates;


public class NodeElement extends GraphElement {

	public NodeElement(Node<?> node, Rectangle bounds, StressMeter stressMeter) {
		_node = node;
		_stressMeter = stressMeter;

		_aura = createAura(bounds);
		_auraOffsetX = _aura.width / 2;
		_auraOffsetY = _aura.height / 2;
		centerAura();
	}


	private final Node<?> _node;

	public int _x;
	public int _y;

	protected float _pendingForceX;
	protected float _pendingForceY;

	private final StressMeter _stressMeter;

	private final Rectangle _aura;
	private final int _auraOffsetX;
	private final int _auraOffsetY;


	public Node<?> node() {
		return _node;
	}

	public String name() {
		return _node.name();
	}

	@Override
	public Coordinates position() {
		return new Coordinates(_x, _y);
	}

	@Override
	public void addForceComponents(float x, float y) {
		_pendingForceX += x;
		_pendingForceY += y;
		_stressMeter.addStress((float)Math.hypot(x, y));
	}

	
	private void assertValidNumber(float n) {
		if (Float.isNaN(n)) throw new IllegalArgumentException("NaN received instead of a valid number.");
	}

	
	public boolean dependsDirectlyOn(NodeElement other) {
		return _node.dependsDirectlyOn(other.node());
	}

	
	private Rectangle createAura(Rectangle bounds) {
		Rectangle result = new Rectangle();
		result.width = bounds.width + (Constants.AURA_THICKNESS * 2);
		result.height = bounds.height + (Constants.AURA_THICKNESS * 2);
		return result;
	}

	
	public Rectangle aura() {
		return _aura;
	}

	
	private void centerAura() {
		_aura.x = _x - _auraOffsetX;
		_aura.y = _y - _auraOffsetY;
	}


	public void clearPendingForces() {
		_pendingForceX = 0;
		_pendingForceY = 0;
	}

	
	public float pendingForceX() {
		return _pendingForceX;
	}
	public float pendingForceY() {
		return _pendingForceY;
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

}
