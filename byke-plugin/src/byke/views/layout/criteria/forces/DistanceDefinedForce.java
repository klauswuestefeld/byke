package byke.views.layout.criteria.forces;

import byke.views.layout.Coordinates;
import byke.views.layout.criteria.NodeElement;

public abstract class DistanceDefinedForce extends CenterAllignedForce {

	protected abstract float intensityGiven(float distance);

	@Override
	protected float intensityGiven(NodeElement n1, NodeElement n2) {
		Coordinates p1 = n1.position();
		Coordinates p2 = node2Position(n2);
		
		float distance = p1.getDistance(p2);
		return intensityGiven(n1, n2, safe(distance));
	}

	
	protected float intensityGiven(NodeElement n1, NodeElement n2, float distance) {
		return intensityGiven(distance);
	}

	
	private float safe(float distance) {
		return Math.max(distance, 0.1f);
	}

}
