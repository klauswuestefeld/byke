package byke.views.layout.criteria.forces;

import static byke.views.layout.criteria.Constants.SUPERIORITY_COMPLEX_FORCE;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;


public class SuperiorityComplex implements Force {

	public void applyTo(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeElement)) return;
		if (!(element2 instanceof NodeElement)) return;

		NodeElement n1 = (NodeElement)element1;
		NodeElement n2 = (NodeElement)element2;

		if (n1.dependsDirectlyOn(n2))
			actUponDependentAndProvider(n1, n2);

		if (n2.dependsDirectlyOn(n1))
			actUponDependentAndProvider(n2, n1);
	}

	private void actUponDependentAndProvider(NodeElement dependent, NodeElement provider) {
		dependent.addForceComponents(0, SUPERIORITY_COMPLEX_FORCE, provider);
	}

}
