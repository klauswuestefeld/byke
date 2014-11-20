package byke.views.layout.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.gef4.layout.algorithms.SpaceTreeLayoutAlgorithm;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.GraphWidget;
import org.eclipse.gef4.zest.core.widgets.ZestStyles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import byke.dependencygraph.Node;
import byke.dependencygraph.SubGraph;

public class NonMovableGraph<T> extends GraphWidget {

	protected final Color _red = new Color(getDisplay(), new RGB(255, 0, 0));
	protected final Color _black = new Color(getDisplay(), new RGB(0, 0, 0));
	protected final Color _yellow = new Color(getDisplay(), new RGB(204, 204, 0));
	
	protected Map<SubGraph<T>, NonMovableNode<T>> _nodeFiguresByNode = new HashMap<SubGraph<T>, NonMovableNode<T>>();
	protected GraphNode _selectedNodeFigure;
	
	
	public NonMovableGraph(Composite parent, final Collection<Node<T>> graph) {
		super(parent, ZestStyles.NONE);
		
		lockNodeMoves();
		SpaceTreeLayoutAlgorithm spaceTreeLayoutAlgorithm = new SpaceTreeLayoutAlgorithm();
		spaceTreeLayoutAlgorithm.setBranchGap(150);
		spaceTreeLayoutAlgorithm.setLayerGap(50);
		spaceTreeLayoutAlgorithm.setLeafGap(30);
		spaceTreeLayoutAlgorithm.setDirection(SpaceTreeLayoutAlgorithm.TOP_DOWN);
		setLayoutAlgorithm(spaceTreeLayoutAlgorithm, true);
		
		addSelectionListener(selectionListener());
		
		DependencyProcessor processor = new DependencyProcessor();
		Collection<SubGraph<T>> newGraph = processor.calculateSubGraphs(graph);
		initGraphFigures(newGraph);
	}


	public Collection<Node<T>> nodes() {
		Set<Node<T>> nodes = new HashSet<Node<T>>();
		for(SubGraph<T> n : _nodeFiguresByNode.keySet())
			nodes.addAll(n.payload());
		return nodes;
	}
	
	private void lockNodeMoves() {
		getLightweightSystem().setEventDispatcher(new SWTEventDispatcher() {
      @Override
      public void dispatchMouseMoved(org.eclipse.swt.events.MouseEvent me) {}
    });
	}
	

	private SelectionAdapter selectionListener() {
		return new SelectionAdapter() {
			@Override
      public void widgetSelected(SelectionEvent e) {
        if(e.item instanceof GraphNode) {
        	if(_selectedNodeFigure != null)
        		for(GraphConnection connection : (List<GraphConnection>)_selectedNodeFigure.getSourceConnections()) {
  						//connection.changeLineColor(doNodesHaveCyclicDependency(connection.getSource(), connection.getDestination()) ? _red : _black);
  						connection.setLineWidth(1);
        		}
        	
        	GraphNode g = (GraphNode)e.item;
        	for(GraphConnection connection : (List<GraphConnection>)g.getSourceConnections()) {
						//connection.changeLineColor(doNodesHaveCyclicDependency(connection.getSource(), connection.getDestination()) ? _red : _yellow);
						connection.setLineWidth(3);
        	}
        	_selectedNodeFigure = g;
        }
      }
    };
	}

	
//	protected boolean doNodesHaveCyclicDependency(GraphNode source, GraphNode destination) {
//		Collection<Node<?>> destinations = (Collection<Node<?>>)destination.getData();
//		Collection<Node<?>> sources = (Collection<Node<?>>)source.getData();
//		for(Node<?> d : destinations)
//			for(Node<?> s : sources)
//				if(d.dependsOn(s))
//					return true;
//		return false;
//	}
	
	protected void initGraphFigures(Collection<SubGraph<T>> nodeGraph) {
		for(SubGraph<T> node : nodeGraph)
			createNodeFigures(node);
	}


	protected void createNodeFigures(SubGraph<T> node) {
		GraphNode dependentFigure = produceNodeFigureFor(node);
			
		for (Object provider : node.providers()) {
			GraphNode providerFigure = produceNodeFigureFor((SubGraph<T>)provider);
			if (dependentFigure.equals(providerFigure)) continue;

			if (providerFigure != null)
				new GraphConnection(this, ZestStyles.CONNECTIONS_DIRECTED, dependentFigure, providerFigure);
		}
	}
	
	
	protected NonMovableNode<T> nodeFigureFor(Node<?> node) {
		for(Entry<SubGraph<T>, NonMovableNode<T>> entry : _nodeFiguresByNode.entrySet()) {
			if(entry.getKey().equals(node))
				return entry.getValue();
		}
		return null;
	}

	protected NonMovableNode<T> produceNodeFigureFor(SubGraph<T> subGraph) {
		NonMovableNode<T> result = _nodeFiguresByNode.get(subGraph);
		if (result != null) return result;
		
		result = new NonMovableNode<T>(this, SWT.NONE, subGraph);
		_nodeFiguresByNode.put(subGraph, result);
		return result;
	}
}
