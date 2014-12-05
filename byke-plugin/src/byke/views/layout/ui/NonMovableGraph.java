package byke.views.layout.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.RadialLayoutAlgorithm;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.GraphWidget;
import org.eclipse.gef4.zest.core.widgets.ZestStyles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import byke.dependencygraph.SubGraph;
import byke.views.BykeView;
import byke.views.cache.NodeFigure;
import byke.views.layout.algorithm.WuestefeldTomaziniLayoutAlgorithm;

public class NonMovableGraph extends GraphWidget {

	private class NonMovableGraphMouseListener implements MouseListener {

		@Override
		public void mouseUp(MouseEvent arg0) {}

		@Override
		public void mouseDown(MouseEvent arg0) {}

		
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			newGraph(((NonMovableGraph)e.getSource()).getSelection());
		}
	}
	
	private Map<NodeFigure, NonMovableNode> _nodeFiguresByNode = new HashMap<NodeFigure, NonMovableNode>();
	
	private Collection<NodeFigure> _parent = new HashSet<NodeFigure>();
	
	public NonMovableGraph(Composite parent, final Collection<NodeFigure> graph) {
		super(parent, ZestStyles.NONE);
		setAnimationEnabled(true);
		setBounds(parent.getBounds());
		
		lockNodeMoves();

		LayoutAlgorithm algorithm = new WuestefeldTomaziniLayoutAlgorithm();
  	setLayoutAlgorithm(algorithm, true);
		
		initGraphFigures(clusterCycles(graph));
		
		addMouseListener(new NonMovableGraphMouseListener());
	}
	
	
	protected Collection<? extends NodeFigure> clusterCycles(final Collection<NodeFigure> graph) {
		DependencyProcessor processor = new DependencyProcessor();
		Collection<SubGraph> newGraph = processor.clusterCycles(graph);
		return newGraph;
	}

	
	private void lockNodeMoves() {
		getLightweightSystem().setEventDispatcher(new SWTEventDispatcher() {
      @Override
      public void dispatchMouseMoved(org.eclipse.swt.events.MouseEvent me) {}
    });
	}
	

	private void initGraphFigures(Collection<? extends NodeFigure> nodeGraph) {
		for(NodeFigure node : nodeGraph)
			createNodeFigures(node);
	}


	private void createNodeFigures(NodeFigure node) {
		GraphNode dependentFigure = produceNodeFigureFor(node);
			
		for (NodeFigure provider : node.providers()) {
			GraphNode providerFigure = produceNodeFigureFor(provider);
			if (dependentFigure.equals(providerFigure)) continue;

			if (providerFigure != null)
				new GraphConnection(this, ZestStyles.CONNECTIONS_DIRECTED, dependentFigure, providerFigure);
		}
	}
	
	
	private NonMovableNode produceNodeFigureFor(NodeFigure subGraph) {
		NonMovableNode result = _nodeFiguresByNode.get(subGraph);
		if (result != null) return result;
		
		result = new NonMovableNode(this, SWT.NONE, subGraph, subGraph.name());
		_nodeFiguresByNode.put(subGraph, result);
		return result;
	}
	
	
	private void newGraph(List<GraphItem> selection) {
		if (selection.isEmpty()) {
			if (!_parent.isEmpty()) {
				new NonMovableSubGraph(composite(), _parent);
				dispose();
			}
			return;
		}
		if (!(selection.get(0) instanceof NonMovableNode)) return;

		NodeFigure subGraph = ((NonMovableNode)selection.get(0)).subGraph();
		if (subGraph.subGraph().size() < 2) return;

		NonMovableGraph nonMovableGraph = new NonMovableSubGraph(composite(), subGraph.subGraph());
		nonMovableGraph._parent = _nodeFiguresByNode.keySet();
		dispose();
	}

	
	private Composite composite() {
		Composite composite;
		try {
			BykeView bikeView = (BykeView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("byke.views.BykeView");
			composite = bikeView._parent;
		} catch (Throwable ex) {
			composite = getShell();
		}
		return composite;
	}

}
