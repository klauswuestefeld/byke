package byke.views.layout.criteria.forces;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.NodeElement;


public class NonOverlapping extends CenterAllignedForce {

	@Override
	protected float intensityGiven(NodeElement n1, NodeElement n2) {
		Rectangle intersection = n1.aura().getIntersection(n2.aura());
		return intersection.width * intersection.height * Constants.NON_CLUTTERING;
	}
	
}
