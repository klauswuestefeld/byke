// Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B. de Oliveira.
// This is free software. See the license distributed along with this file.

package byke.views.layout.criteria;

import java.util.List;

import byke.views.layout.criteria.forces.AlphabeticalOrder;
import byke.views.layout.criteria.forces.DependenciesDown;
import byke.views.layout.criteria.forces.Force;
import byke.views.layout.criteria.forces.NonCluttering;
import byke.views.layout.criteria.forces.NonOverlapping;
import byke.views.layout.criteria.forces.SaveSpace;


public class StressMeter {

	private static final Force ALPHABETICAL_ORDER = new AlphabeticalOrder();
	private static final Force DEPENDENCIES_DOWN = new DependenciesDown();
	private static final Force SAVE_SPACE = new SaveSpace();
	private static final Force NON_OVERLAPPING = new NonOverlapping();
	private static final Force NON_CLUTTERING = new NonCluttering();
	
	
	private static void applyForces(NodeElement n1, NodeElement n2) {
		applyAsymmetricalForcesTo(n1, n2);
		
		// Converging:
//	SAVE_SPACE.applyTo(n1, n2);
		
		// Diverging:
//	NON_CLUTTERING.applyTo(n1, n2);
//	NON_OVERLAPPING.applyTo(n1, n2);
	}


	private static void applyAsymmetricalForcesTo(NodeElement n1, NodeElement n2) {
//	ALPHABETICAL_ORDER.applyTo(n1, n2);
		DEPENDENCIES_DOWN.applyTo(n1, n2);
	}

	
	public static float applyForcesTo(NodeElement chosen, List<NodeElement> all) {
		chosen.clearForces();
		
		for (NodeElement other : all)
			if (other != chosen)
				applyForces(chosen, other);

		return chosen.stress();
	}

	
	public static void applyAsymmetricalForcesTo(List<? extends NodeElement> nodes) {
		for (NodeElement n : nodes) n.clearForces();

		for (int i = 0; i < nodes.size(); i++)
			for (int j = i + 1; j < nodes.size(); j++)
				applyAsymmetricalForcesTo(nodes.get(i), nodes.get(j));
	}
	
}
