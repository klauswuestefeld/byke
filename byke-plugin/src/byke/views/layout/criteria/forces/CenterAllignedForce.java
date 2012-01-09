package byke.views.layout.criteria.forces;

import byke.views.layout.Coordinates;
import byke.views.layout.criteria.NodeElement;


public abstract class CenterAllignedForce implements Force {

	protected abstract float intensityGiven(NodeElement n1, NodeElement n2);

	@Override
	public void applyTo(NodeElement n1, NodeElement n2) {
		Coordinates p1 = n1.position();
		Coordinates p2 = node2Position(n2);

		float intensity = this.intensityGiven(n1, n2);
		
		//Tested by ForcesTest
		float dx = p1._x - p2._x;
		float dy = p1._y - p2._y;
		double direction = Math.atan2(dx, dy);
		float xComponent = (float)(Math.sin(direction) * intensity);
		float yComponent = (float)(Math.cos(direction) * intensity);

		n1.addForceComponents(xComponent, yComponent, n2);
	}

	protected Coordinates node2Position(NodeElement n) {
		return n.position();
	}

}
