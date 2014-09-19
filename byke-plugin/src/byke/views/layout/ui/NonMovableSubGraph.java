package byke.views.layout.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.gef4.layout.algorithms.GridLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.RadialLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.SpaceTreeLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.TreeLayoutAlgorithm;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.widgets.Composite;

import byke.dependencygraph.Node;

public class NonMovableSubGraph<T> extends NonMovableGraph<T> {

	
	public NonMovableSubGraph(Composite parent, Collection<Node<T>> graph) {
		super(parent, graph);
		setBackground(LIGHT_YELLOW);

//		SpringLayoutAlgorithm springLayoutAlgorithm = new SpringLayoutAlgorithm();
//		springLayoutAlgorithm.setSpringGravitation(1);
//		springLayoutAlgorithm.setSpringLength(10);
//		springLayoutAlgorithm.setSpringMove(5);
//		setLayoutAlgorithm(springLayoutAlgorithm, true);
		
		
//		RadialLayoutAlgorithm radialLayoutAlgorithm = new RadialLayoutAlgorithm();
//		setLayoutAlgorithm(radialLayoutAlgorithm, false);

		CircularLayoutAlgorithm circularLayoutAlgorithm = new CircularLayoutAlgorithm();
		setLayoutAlgorithm(circularLayoutAlgorithm, true);
		
//		SugiyamaLayoutAlgorithm sugiyamaLayoutAlgorithm = new SugiyamaLayoutAlgorithm();
//		setLayoutAlgorithm(sugiyamaLayoutAlgorithm, true);
		
		
//		GridLayoutAlgorithm gridLayoutAlgorithm = new GridLayoutAlgorithm();
//		gridLayoutAlgorithm.setAspectRatio(10);
//		gridLayoutAlgorithm.setRowPadding(50);
//		setLayoutAlgorithm(gridLayoutAlgorithm, true);
		
//		SpaceTreeLayoutAlgorithm spaceTreeLayoutAlgorithm = new SpaceTreeLayoutAlgorithm();
//		spaceTreeLayoutAlgorithm.setBranchGap(300);
//		spaceTreeLayoutAlgorithm.setLayerGap(100);
//		spaceTreeLayoutAlgorithm.setLeafGap(100);
//		spaceTreeLayoutAlgorithm.setDirection(SpaceTreeLayoutAlgorithm.TOP_DOWN);
//		setLayoutAlgorithm(spaceTreeLayoutAlgorithm, true);
		
//		setLayoutAlgorithm(new TreeLayoutAlgorithm(), true);
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
		
		for(Collection<Node<T>> nodes : _nodeFiguresByNode.keySet())
			createNodeFigures(nodes);
	}
}
