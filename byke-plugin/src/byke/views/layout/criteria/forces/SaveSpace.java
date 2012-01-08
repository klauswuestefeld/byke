package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.NodeElement;


public class SaveSpace extends CenterAllignedForce {

	@Override
	protected float intensityGiven(NodeElement n1, NodeElement n2) {
		return -1;
	}

}
