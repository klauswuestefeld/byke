package byke.views.layout.algorithm;

import byke.views.layout.CartesianLayout;


public interface LayoutAlgorithm {

	boolean improveLayoutStep();

	CartesianLayout layoutMemento();

}