package byke.views.layout.criteria.forces;

import byke.views.layout.FloatRectangle;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;


public class MutualExclusion extends CenterAllignedForce {

	@Override
	protected float intensityGiven(GraphElement element1, GraphElement element2) {
		NodeElement node = (NodeElement)element1;
		NodeElement node2 = (NodeElement)element2;

		FloatRectangle intersection = node.aura().intersection(node2.aura());
		return -intersection.area();
		
		
//		FloatRectangle intersection = node.aura().intersection(node2.aura());
//
//		float hypotenuse = (float)Math.hypot(intersection._height, intersection._width);
//		if (hypotenuse < 0.1) return;
//		
//		float intensity = intersection.area() * -0.0003f;
//		
//		float xComponent = (intersection._height / hypotenuse) * intensity; //Yes. The x component is proportional to the >>HEIGHT<< of the intersection.
//		float yComponent = (intersection._width / hypotenuse) * intensity; //Yes. The y component is proportional to the >>WIDTH<< of the intersection.
		
//		node.addForceComponents(xComponent, yComponent, node2);
	}

	@Override
	public void applyTo(GraphElement element1, GraphElement element2) {
		if (element1 instanceof NodeElement && element2 instanceof NodeElement) {
			super.applyTo(element1, element2);
		}
	}

}
