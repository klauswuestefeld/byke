package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;


public class DependencySpring extends DistanceDefinedForce {

	private static final float SPRING_FORCE = Constants.DEPENDENCY_SPRING_FORCE;
	private static final int IDEAL_SIZE = Constants.DEPENDENCY_SPRING_PREFERRED_SIZE;


	@Override
	public float intensityGiven(float distance) {
		return (distance - IDEAL_SIZE) * SPRING_FORCE; // TODO Play with this formula.
	}

	@Override
	public void applyTo(GraphElement element1, GraphElement element2) {

		if (element1 instanceof NodeElement && element2 instanceof NodeElement) {

			NodeElement Element1 = (NodeElement)element1;
			NodeElement Element2 = (NodeElement)element2;
			if (Element1.dependsDirectlyOn(Element2) || Element2.dependsDirectlyOn(Element1)) {
				super.applyTo(element1, element2);
			}
		}
	}

}
