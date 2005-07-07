package byecycle.views.layout.forces;

import org.eclipse.draw2d.geometry.Rectangle;

import byecycle.views.layout.GraphElement;
import byecycle.views.layout.NodeFigure;

public class Aversion extends DistanceBasedForce {
	
	public void actUpon(GraphElement element1, GraphElement element2) {
		if (!(element1 instanceof NodeFigure)) return;
		if (!(element2 instanceof NodeFigure)) return;
		super.actUpon(element1, element2);
		//TODO: Consider making the figures "slide" off each other rather than simply repel.
	}

	@Override
	protected float intensityGiven(GraphElement element1, GraphElement element2, float ignored) {
    	NodeFigure figure1 = (NodeFigure)element1;
		NodeFigure figure2 = (NodeFigure)element2;
		
		Rectangle intersection = figure1.aura().getIntersection(figure2.aura());
		int area = intersection.getSize().getArea();
		return -0.0000001f * (float)Math.pow(area, 1.5);
	}

	float intensityGiven(float distance) {
		throw new IllegalStateException();
	}
}
