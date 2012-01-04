package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;


public class DependencySpring extends DistanceDefinedForce {

	private static final float SPRING_FORCE = Constants.DEPENDENCY_SPRING_FORCE;
	private static final int IDEAL_SIZE = Constants.DEPENDENCY_SPRING_PREFERRED_SIZE;


	@Override
	protected float intensityGiven(float nonZeroDistance) {
		return (nonZeroDistance - IDEAL_SIZE) * SPRING_FORCE;
	}

	@Override
	public void applyTo(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeElement)) return;
		if (!(element2 instanceof NodeElement)) return;

		NodeElement n1 = (NodeElement)element1;
		NodeElement n2 = (NodeElement)element2;
		if (n1.dependsDirectlyOn(n2) || n2.dependsDirectlyOn(n1))
			super.applyTo(n1, n2);
	}

}
