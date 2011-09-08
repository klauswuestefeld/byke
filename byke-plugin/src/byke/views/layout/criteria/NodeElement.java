//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views.layout.criteria;

import byke.dependencygraph.Node;
import byke.views.layout.Coordinates;
import byke.views.layout.FloatRectangle;


public class NodeElement extends GraphElement {

	public NodeElement(Node<?> node, StressMeter stressMeter) {
		_node = node;
		_stressMeter = stressMeter;

		_aura = createAura();
		_auraOffsetX = _aura._width / 2;
		_auraOffsetY = _aura._height / 2;
		centerAura();
	}


	private final Node<?> _node;

	public float _x;
	public float _y;

	protected float _pendingForceX;
	protected float _pendingForceY;

	private final StressMeter _stressMeter;

	private int _width;
	private int _height;

	private final FloatRectangle _aura;
	private final float _auraOffsetX;
	private final float _auraOffsetY;


	public Node<?> node() {
		return _node;
	}

	public String name() {
		return _node.name();
	}

	public Coordinates position() {
		return new Coordinates(_x, _y);
	}

	protected void addForceComponents(float x, float y) {
		_pendingForceX += x;
		_pendingForceY += y;
		_stressMeter.addStress((float)Math.hypot(x, y));
	}

	public void position(Coordinates coordinates) {
		position(coordinates._x, coordinates._y);
	}

	public void position(float x, float y) {
		assertValidNumber(x);
		assertValidNumber(y);

		_x = x;
		_y = y;

		centerAura();
	}

	private void assertValidNumber(float n) {
		if (Float.isNaN(n)) throw new IllegalArgumentException("NaN received instead of a valid number.");
	}

	public boolean dependsDirectlyOn(NodeElement other) {
		return _node.dependsDirectlyOn(other.node());
	}

	private FloatRectangle createAura() {
		FloatRectangle result = new FloatRectangle();
		result._width = _width + (Constants.AURA_THICKNESS * 2);
		result._height = _height + (Constants.AURA_THICKNESS * 2);
		return result;
	}

	public FloatRectangle aura() {
		return _aura;
	}

	private void centerAura() {
		_aura._x = _x - _auraOffsetX;
		_aura._y = _y - _auraOffsetY;
	}

	public void clearPendingForces() {
		_pendingForceX = 0;
		_pendingForceY = 0;
	}

	public float timeNeededToMoveOnePixel() {
		return 1 / Math.max(Math.abs(velocityX()), Math.abs(velocityY()));
	}

	protected float velocityX() {
		return _pendingForceX;
	}

	protected float velocityY() {
		return _pendingForceY;
	}

	/** "Give: To yield to physical force." Dictionary.com */
	public void give(float timeFrame) {
		float dX = velocityX() * timeFrame;
		float dY = velocityY() * timeFrame;
	
		position(_x + dX, _y + dY);
	}

}
