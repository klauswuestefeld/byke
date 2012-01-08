package byke.views.layout.criteria.forces;

import static byke.views.layout.criteria.Constants.DEPENDENCY_PREFERRED_SIZE;
import byke.views.layout.criteria.NodeElement;


public class DependenciesDown extends DistanceDefinedForce {

	@Override
	protected float intensityGiven(float nonZeroDistance) {
		return DEPENDENCY_PREFERRED_SIZE - nonZeroDistance;
	}

	@Override
	public void applyTo(NodeElement n1, NodeElement n2) {
		if (n1.dependsDirectlyOn(n2) || n2.dependsDirectlyOn(n1))
			super.applyTo(n1, n2);
	}

}
