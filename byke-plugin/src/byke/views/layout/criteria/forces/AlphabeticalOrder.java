package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.NodeElement;


public class AlphabeticalOrder implements Force {

	@Override
	public void applyTo(NodeElement n1, NodeElement n2) {
		if (n1.name().compareToIgnoreCase(n2.name()) < 0) {
			actUponFirstAndSecond(n1, n2);
		} else {
			actUponFirstAndSecond(n2, n1);
		}
	}

	private void actUponFirstAndSecond(NodeElement first, NodeElement second) {
		float dx = Math.max(Math.abs(first._x - second._x), 1);
		float dy = Math.max(Math.abs(first._y - second._y), 1);

		float cx = -Constants.ALPHABETICAL_ORDER_THRUST / dx;
		float cy = -Constants.ALPHABETICAL_ORDER_THRUST / dy;
		first.addForceComponents(cx, cy, second);
	}

}
