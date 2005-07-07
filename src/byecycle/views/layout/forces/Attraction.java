package byecycle.views.layout.forces;

import byecycle.views.layout.GraphElement;
import byecycle.views.layout.NodeFigure;

public class Attraction extends DistanceBasedForce {

	@Override
    public float intensityGiven(float distance) {
        return  (distance - 25) * 0.000002f; //TODO Play with this formula. Zero it to see REPULSION acting alone.
    }
	
	@Override
	public void actUpon(GraphElement element1, GraphElement element2) {
		
		if (element1 instanceof NodeFigure && element2 instanceof NodeFigure) {
		
			NodeFigure figure1 = (NodeFigure)element1;
			NodeFigure figure2 = (NodeFigure)element2;
			if (figure1.dependsOn(figure2) ||
				figure2.dependsOn(figure1)) {
				super.actUpon(element1, element2);
			}
		}
	}

}
