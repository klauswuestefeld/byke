package byke.views.layout.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.swt.widgets.Composite;

import byke.dependencygraph.Node;

public class NonMovableSubGraph<T> extends NonMovableGraph<T> {

	
	public NonMovableSubGraph(Composite parent, Collection<Node<T>> graph) {
		super(parent, graph);
		setBackground(LIGHT_YELLOW);
	}


	@Override
	protected Collection<Collection<Node<T>>> calculateSubGraphs(Collection<Node<T>> graph) {
		Collection<Collection<Node<T>>> newGraph = new ArrayList<Collection<Node<T>>>();
		
		for(Node<T> node : graph)
			newGraph.add(Arrays.asList(node));
		newGraph.add(graph);
		
		return newGraph;
	}
	
	
	@Override
	protected void initGraphFigures(Collection<? extends Collection<Node<T>>> nodeGraph) {
		for(Collection<Node<T>> nodes : nodeGraph)
			if(nodes.size() < 2)
				produceNodeFigureFor(nodes);
		
		for(Collection<Node<T>> nodes : nodeGraph)
			createNodeFigures(nodes);
		
		for(Collection<Node<T>> nodes : nodeGraph)
			produceNodeFigureFor(nodes);
	}
}
