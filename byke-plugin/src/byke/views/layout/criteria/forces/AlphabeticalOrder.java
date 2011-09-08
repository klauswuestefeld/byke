package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;


public class AlphabeticalOrder implements Force {

	public void applyTo(GraphElement element1, GraphElement element2) {

		if (!(element1 instanceof NodeElement)) return;
		if (!(element2 instanceof NodeElement)) return;

		NodeElement node1 = (NodeElement)element1;
		NodeElement node2 = (NodeElement)element2;

		if (node1.name().compareToIgnoreCase(node2.name()) < 0) {
			actUponFirstAndSecond(node1, node2);
		} else {
			actUponFirstAndSecond(node2, node1);
		}
	}

	private void actUponFirstAndSecond(NodeElement first, NodeElement second) {
		float dx = Math.max(Math.abs(first._x - second._x), 1);
		float dy = Math.max(Math.abs(first._y - second._y), 1);

		first.addForceComponents(-Constants.ALPHABETICAL_ORDER_THRUST / dx, 0, second);
		first.addForceComponents(0, -Constants.ALPHABETICAL_ORDER_THRUST / dy, second);
	}

}
