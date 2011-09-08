package byke.views.layout.criteria.forces;

import byke.views.layout.Coordinates;
import byke.views.layout.criteria.GraphElement;

public abstract class DistanceDefinedForce extends CenterAllignedForce {

	protected abstract float intensityGiven(float distance);

	@Override
	protected float intensityGiven(GraphElement element1, GraphElement element2) {
		Coordinates p1 = element1.position();
		Coordinates p2 = element2.position();
		
		float distance = p1.getDistance(p2);
		return intensityGiven(safe(distance));
	}

	private float safe(float distance) {
		return Math.max(distance, 0.1f);
	}

}
