package byke.views.layout.ui;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;

import byke.dependencygraph.Node;
import byke.views.layout.algorithm.CircularLayoutAlgorithm;

public class NonMovableSubGraph<T> extends NonMovableGraph<T> {

	
	public NonMovableSubGraph(Composite parent, Collection<Node<T>> graph) {
		super(parent, graph);
		setBackground(LIGHT_YELLOW);

		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm();
		setLayoutAlgorithm(circularLayoutAlgorithm, true);
	}


//	@Override
//	protected Collection<Collection<Node<T>>> calculateSubGraphs(Collection<Node<T>> graph) {
//		Collection<Collection<Node<T>>> newGraph = new ArrayList<Collection<Node<T>>>();
//		
//		for(Node<T> node : graph)
//			newGraph.add(Arrays.asList(node));
//		newGraph.add(graph);
//		
//		return newGraph;
//	}
	
	
//	@Override
//	protected void initGraphFigures(Collection<Node<T>> nodeGraph) {
//		for(Node<T> nodes : nodeGraph)
//			if(nodes.size() < 2)
//				produceNodeFigureFor(nodes);
//		
//		for(Collection<Node<T>> nodes : _nodeFiguresByNode.keySet())
//			createNodeFigures(nodes);
//	}
}
