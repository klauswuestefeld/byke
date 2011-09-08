package byke.views.layout.algorithm.relaxer;

import byke.dependencygraph.Node;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;

class InertialNode extends NodeElement {

	private float _velocityX;
	private float _velocityY;

	public InertialNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}

	@Override
	public void give(float timeFrame) {
		super.give(timeFrame);

		//if (_pendingForceX < 0 == _velocityX < 0) _pendingForceX *= 0.95f;
		//if (_pendingForceY < 0 == _velocityY < 0) _pendingForceY *= 0.95f;

		_velocityX += (_pendingForceX * timeFrame);
		_velocityY += (_pendingForceY * timeFrame);
		
		if (Math.abs(_velocityX) > 1)
			_velocityX = (float)(Math.signum(_velocityX) * Math.pow(Math.abs(_velocityX), 0.8));
		if (Math.abs(_velocityY) > 1)
			_velocityY = (float)(Math.signum(_velocityY) * Math.pow(Math.abs(_velocityY), 0.8));
		
		_velocityX *= 0.93f;
		_velocityY *= 0.93f;
	}

	@Override
	protected float velocityX() {
		return _velocityX + _pendingForceX;
	}

	@Override
	protected float velocityY() {
		return _velocityY + _pendingForceY;
	}

	@Override
	public float timeNeededToMoveOnePixel() {
		float timeToMove = super.timeNeededToMoveOnePixel();
		float timeToAccelerate = 1 / Math.max(Math.abs(_pendingForceX), Math.abs(_pendingForceX));
		return Math.min(timeToMove, timeToAccelerate);
	}

	@Override
	protected void addForceComponents(float x, float y) {
		super.addForceComponents(cap(x), cap(y));
	}

	private float cap(float f) {
		return Math.min(Math.max(f, -0.001f), 0.001f);
	}

	
	
}
