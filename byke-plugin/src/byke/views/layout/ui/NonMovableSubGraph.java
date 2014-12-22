package byke.views.layout.ui;

import java.util.Collection;
import java.util.List;

import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.swt.widgets.Composite;

import byke.views.cache.NodeFigure;
import byke.views.layout.algorithm.CircularLayoutAlgorithm;

public class NonMovableSubGraph extends NonMovableGraph {

	
	public NonMovableSubGraph(Composite parent, Collection<NodeFigure> graph, Collection<NodeFigure> parentGraph) {
		super(parent, graph);
		_parent = parentGraph;

		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm();
		setLayoutAlgorithm(circularLayoutAlgorithm, true);
	}
	
	@Override
	protected Collection<? extends NodeFigure> clusterCycles(Collection<NodeFigure> graph) {
		return graph;
	}
	
	@Override
	protected void newGraph(List<GraphItem> selection) {
		if (selection.isEmpty()) {
			new NonMovableGraph(composite(), _parent);
			dispose();
		}
	}
}
