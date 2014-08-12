//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views.layout.criteria;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.dependencygraph.Node;
import byke.views.layout.Coordinates;


public class NodeElement implements Element {

	public NodeElement(Node<?> node, Rectangle bounds) {
		_node = node;

		_aura = createAura(bounds);
		_auraOffsetX = _aura.width / 2;
		_auraOffsetY = _aura.height / 2;
		centerAura();
	}


	private final Node<?> _node;

	private int _x;
	private int _y;

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
		return new Coordinates(x(), y());
	}

	
	public void clearForces() {
		_resultingForceX = 0;
		_resultingForceY = 0;
		_stress = 0f;
	}

	
	public float resultingForceX() { return _resultingForceX; }
	public float resultingForceY() { return _resultingForceY; }
	float stress() { return _stress; }
	
	
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
		_aura.x = x() - _auraOffsetX;
		_aura.y = y() - _auraOffsetY;
	}


	public void position(Coordinates c) {
		position(c._x, c._y);
	}

	
	public void position(int newX, int newY) {
		x(newX);
		y(newY);

		centerAura();
	}

	
	public void move(int dx, int dy) {
		position(x() + dx, y() + dy);
	}

	public void addForceComponents(float fx, float fy, NodeElement counterpart) {
		addForceComponents(fx, fy);
		counterpart.addForceComponents(-fx, -fy);
	}

	
	protected void addForceComponents(float fx, float fy) {
		_resultingForceX += fx;
		_resultingForceY += fy;
		_stress += (float)Math.hypot(fx, fy);
	}

	@Override
	public String toString() {
		return node().toString();
	}


	public int y() {
		return _y;
	}


	public void y(int y) {
		_y = y;
	}


	@Override
	public int x() {
		return _x;
	}


	public void x(int x) {
		_x = x;
	}


	@Override
	public int width() {
		return aura().width;
	}
}
