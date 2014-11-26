package byke.views.layout.ui;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;

import byke.views.cache.NodeFigure;
import byke.views.layout.algorithm.CircularLayoutAlgorithm;

public class NonMovableSubGraph extends NonMovableGraph {

	
	public NonMovableSubGraph(Composite parent, Collection<NodeFigure> graph) {
		super(parent, graph);

		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm();
		setLayoutAlgorithm(circularLayoutAlgorithm, true);
	}
	
	@Override
	protected Collection<? extends NodeFigure> clusterCycles(Collection<NodeFigure> graph) {
		return graph;
	}
}
