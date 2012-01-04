// Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B. de Oliveira.
// This is free software. See the license distributed along with this file.

package byke.views.layout.criteria;

import java.util.List;

import byke.views.layout.criteria.forces.AlphabeticalOrder;
import byke.views.layout.criteria.forces.DependencySpring;
import byke.views.layout.criteria.forces.Force;
import byke.views.layout.criteria.forces.Gravity;
import byke.views.layout.criteria.forces.MutualExclusion;
import byke.views.layout.criteria.forces.StaticElectricity;
import byke.views.layout.criteria.forces.SuperiorityComplex;


public class StressMeter {

	private static final Force SUPERIORITY_COMPLEX = new SuperiorityComplex();
	private static final Force ALPHABETICAL_ORDER = new AlphabeticalOrder();
	private static final Force DEPENDENCY_SPRING = new DependencySpring();
	private static final Force GRAVITY = new Gravity();
	private static final Force STATIC_ELECTRICITY = new StaticElectricity();
	private static final Force MUTUAL_EXCLUSION = new MutualExclusion();
	
	private float _reading;


	void addStress(float stress) {
		if (Float.isNaN(stress)) throw new IllegalArgumentException("Stress cannot be NaN.");
		_reading += stress;
	}

	private void reset() {
		_reading = 0;
	}

	public float applyForcesTo(List<? extends NodeElement> nodes, List<GraphElement> graphElements) {
		reset();

		for (NodeElement node : nodes)
			node.clearPendingForces();

		for (int i = 0; i < graphElements.size(); i++) {
			GraphElement element1 = graphElements.get(i);

			for (int j = i + 1; j < graphElements.size(); j++) {
				GraphElement element2 = graphElements.get(j);

				// Symmetry breakers: (important for RandomAverage algorithm)
				SUPERIORITY_COMPLEX.applyTo(element1, element2);
				ALPHABETICAL_ORDER.applyTo(element1, element2);

				// Converging:
				DEPENDENCY_SPRING.applyTo(element1, element2);
				GRAVITY.applyTo(element1, element2);

				// Diverging:
				STATIC_ELECTRICITY.applyTo(element1, element2);
				MUTUAL_EXCLUSION.applyTo(element1, element2);
			}
		}

		return _reading;
	}

	public float reading() {
		return _reading;
	}

}
