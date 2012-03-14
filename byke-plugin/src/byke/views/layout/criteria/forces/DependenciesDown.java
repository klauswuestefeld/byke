package byke.views.layout.criteria.forces;

import static byke.views.layout.criteria.Constants.DEPENDENCY_PREFERRED_SIZE;
import byke.views.layout.Coordinates;
import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.NodeElement;


public class DependenciesDown extends DistanceDefinedForce {

	@Override
	protected float intensityGiven(float nonZeroDistance) {
		return -nonZeroDistance * Constants.DEPENDENCY;
	}

	@Override
	public void applyTo(NodeElement n1, NodeElement n2) {
		if (n1.dependsDirectlyOn(n2)) super.applyTo(n1, n2);
		if (n2.dependsDirectlyOn(n1)) super.applyTo(n2, n1);
	}
	
	@Override
	protected Coordinates node2Position(NodeElement provider) {
		//Pretends the provider is above its position so that the dependent is attracted to that position.
		Coordinates p = provider.position();
		return new Coordinates(p._x, p._y - DEPENDENCY_PREFERRED_SIZE);
	}

}
