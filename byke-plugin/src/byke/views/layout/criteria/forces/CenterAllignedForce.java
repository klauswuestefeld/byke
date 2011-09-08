package byke.views.layout.criteria.forces;

import byke.views.layout.Coordinates;
import byke.views.layout.criteria.GraphElement;


public abstract class CenterAllignedForce implements Force {

	protected abstract float intensityGiven(GraphElement element1, GraphElement element2);

	public void applyTo(GraphElement element1, GraphElement element2) {
		Coordinates p1 = element1.position();
		Coordinates p2 = element2.position();

		float intensity = this.intensityGiven(element1, element2);
		
		float dx = p2._x - p1._x;
		float dy = p2._y - p1._y;
		double direction = Math.atan2(dx, dy);
		float xComponent = (float)(Math.cos(direction) * intensity);
		float yComponent = (float)(Math.sin(direction) * intensity);

		element1.addForceComponents(xComponent, yComponent, element2);
	}

}
