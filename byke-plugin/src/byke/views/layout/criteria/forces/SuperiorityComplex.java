package byke.views.layout.criteria.forces;

import java.util.Random;

import byke.views.layout.criteria.Constants;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;


public class SuperiorityComplex implements Force {

	private static final Random RANDOM = new Random();

	public void applyTo(GraphElement element1, GraphElement element2) {

		if (!(element1 instanceof NodeElement)) return;
		if (!(element2 instanceof NodeElement)) return;

		NodeElement node1 = (NodeElement)element1;
		NodeElement node2 = (NodeElement)element2;

		if (node1.dependsDirectlyOn(node2))
			actUponDependentAndProvider(node1, node2);

		if (node2.dependsDirectlyOn(node1))
			actUponDependentAndProvider(node2, node1);
	}

	private void actUponDependentAndProvider(NodeElement dependent, NodeElement provider) {
		float dY = dependent._y - provider._y;
		float dX = dependent._x - provider._x;
		double angle = Math.atan2(dY, dX);

		double torque = Constants.DEPENDENCY_TORQUE * (1 + Math.sin(angle)); //From zero when pointing down, through 1 when horizontal, to 2 when pointing up.
		boolean clockwise = dependent._x == provider._x
			? RANDOM.nextBoolean() //Preserve symmetry.
			: dependent._x < provider._x;
		if (!clockwise) torque = - torque;

		applyTorque(dependent, provider, torque, angle);
	}

	private void applyTorque(NodeElement dependent, NodeElement provider, double torque, double angle) {
//		float xComponent = -(float)(torque * Math.sin(angle));
//		float yComponent = (float)(torque * Math.cos(angle));

		//dependent.addForceComponents(xComponent, yComponent, provider);
		dependent.addForceComponents(0, -0.002f, provider);
		
	}

}
