package byecycle.views.layout.forces;

import org.eclipse.draw2d.geometry.Point;

import byecycle.views.layout.GraphElement;

public abstract class DistanceBasedForce implements Force {

	abstract float intensityGiven(float distance);

	protected float intensityGiven(GraphElement element1, GraphElement element2, float distance) {
		return intensityGiven(distance);
	}

	public void actUpon(GraphElement element1, GraphElement element2) {
		Point p1 = element1.candidatePosition();
		Point p2 = element2.candidatePosition(); 
		
		float distance = (float)Math.max(p1.getDistance(p2), 2);
		float intensity = this.intensityGiven(element1, element2, distance);
		
		float xComponent = ((p2.x - p1.x) / distance) * intensity;
		float yComponent = ((p2.y - p1.y) / distance) * intensity;
		
		element1.addForceComponents(xComponent, yComponent);
		element2.addForceComponents(-xComponent, -yComponent);
	}


}
