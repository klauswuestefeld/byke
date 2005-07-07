package byecycle.views.layout.forces;

import byecycle.views.layout.GraphElement;
import byecycle.views.layout.NodeFigure;

public class ProviderGravity implements Force {
	
	private static final float DEPENDENCY_THRUST = 0.0003f;

	public void actUpon(GraphElement element1, GraphElement element2) {
		
    	if (!(element1 instanceof NodeFigure)) return;
		if (!(element2 instanceof NodeFigure)) return;
		
    	NodeFigure figure1 = (NodeFigure)element1;
		NodeFigure figure2 = (NodeFigure)element2;
    	
        if (figure1.dependsOn(figure2)) {
            actUponDependentAndProvider(figure1, figure2);
        }
        
        if (figure2.dependsOn(figure1)) {
            actUponDependentAndProvider(figure2, figure1);
        }
	}
	
	private void actUponDependentAndProvider(NodeFigure dependent, NodeFigure provider) {
		
		float dY = Math.abs(provider.candidateY() - dependent.candidateY());  
		boolean inverted = provider.candidateY() < dependent.candidateY();
		
		float thrust = (float) (DEPENDENCY_THRUST * 1.5 * (inverted
			? 1 + (dY / 20)
			: 10 / (10 + dY)
		));
		dependent.up(thrust);
		provider.down(thrust);
	}


}
