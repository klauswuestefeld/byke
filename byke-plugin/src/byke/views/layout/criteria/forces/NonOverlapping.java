package byke.views.layout.criteria.forces;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.views.layout.Coordinates;
import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.NodeElement;


public class NonOverlapping extends CenterAllignedForce {

	@Override
	protected float intensityGiven(NodeElement n1, NodeElement n2) {
		Rectangle intersection = n1.aura().getIntersection(n2.aura());
		return intersection.width * intersection.height * Constants.NON_OVERLAPPING;
	}

	@Override
	public void applyTo(NodeElement n1, NodeElement n2) {
		Coordinates p1 = n1.position();
		Coordinates p2 = n2.position();
	
		float intensity = this.intensityGiven(n1, n2);
		
		//Tested by ForcesTest
		float dx = p1._x - p2._x;
		float dy = p1._y - p2._y;
		double direction = Math.atan2(dx, dy);
		float xComponent = (float)(Math.sin(direction) * intensity);
		float yComponent = (float)(Math.cos(direction) * intensity);
	
		n1.addForceComponents(xComponent, yComponent, n2);
	}
	
}
