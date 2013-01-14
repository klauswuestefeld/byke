package byke.views.layout.algorithm.random;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.dependencygraph.Node;
import byke.views.layout.criteria.NodeElement;

class AveragingNode extends NodeElement {

	private long turns = 0;
	private double totalX;
	private double totalY;

	
	AveragingNode(Node<?> node, Rectangle bounds) {
		super(node, bounds);
	}

	
	void takeAveragePositionDividedBy(int factor) {
		turns++;
		totalX += resultingForceX();
		totalY += resultingForceY();
		int avgX = (int)(totalX / turns / factor);
		int avgY = (int)(totalY / turns / factor);
		System.out.println(node().name() + " " + avgX);
		position(avgX, avgY);
	}

}
