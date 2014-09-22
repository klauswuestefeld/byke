package byke.views.layout.ui;

import java.util.ArrayList;
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

public class NonMovableGraph<T> extends GraphWidget {

	protected final Color _red = new Color(getDisplay(), new RGB(255, 0, 0));
	protected final Color _black = new Color(getDisplay(), new RGB(0, 0, 0));
	protected final Color _yellow = new Color(getDisplay(), new RGB(204, 204, 0));
	
	protected Map<Collection<Node<T>>, NonMovableNode<T>> _nodeFiguresByNode = new HashMap<Collection<Node<T>>, NonMovableNode<T>>();
	protected GraphNode _selectedNodeFigure;
	
	public Collection<Node<T>> nodes() {
		Set<Node<T>> nodes = new HashSet<Node<T>>();
		for(Collection<Node<T>> n : _nodeFiguresByNode.keySet())
			nodes.addAll(n);
		return nodes;
	}
	
	protected Collection<Collection<Node<T>>> calculateSubGraphs(Collection<Node<T>> graph) {
		Collection<Collection<Node<T>>> newGraph = new ArrayList<Collection<Node<T>>>();
		
		for(Node<T> node : graph) {
			Collection<Node<T>> temp = new ArrayList<Node<T>>();
			temp.add(node);
			for(Node<T> provider : node.providers()) {
				if(node.equals(provider))
					continue;
				if(provider.dependsOn(node))
					temp.add(provider);
			}
			
			Collection<Node<T>> cyclics = new ArrayList<Node<T>>();

			for(Collection<Node<T>> x : newGraph) {
				for(Node<T> y : temp)
					if(x.contains(y)) {
						cyclics = x;
						break;
					}
			}
			
			for(Node<T> n : temp)
				if(!cyclics.contains(n))
					cyclics.add(n);
			
			newGraph.add(cyclics);
		}
		
		return newGraph;
	}

	
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
		
		Collection<? extends Collection<Node<T>>> newGraph = calculateSubGraphs(graph);
		try { // FIXME there's a NPE on this call, when you double click a node without subnodes
			initGraphFigures(newGraph);
		} catch (RuntimeException e) {
			dispose();
			e.printStackTrace();
		}
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
  						connection.changeLineColor(doNodesHaveCyclicDependency(connection.getSource(), connection.getDestination()) ? _red : _black);
  						connection.setLineWidth(1);
        		}
        	
        	GraphNode g = (GraphNode)e.item;
        	for(GraphConnection connection : (List<GraphConnection>)g.getSourceConnections()) {
						connection.changeLineColor(doNodesHaveCyclicDependency(connection.getSource(), connection.getDestination()) ? _red : _yellow);
						connection.setLineWidth(3);
        	}
        	_selectedNodeFigure = g;
        }
      }
    };
	}

	
	protected boolean doNodesHaveCyclicDependency(GraphNode source, GraphNode destination) {
		Collection<Node<?>> destinations = (Collection<Node<?>>)destination.getData();
		Collection<Node<?>> sources = (Collection<Node<?>>)source.getData();
		for(Node<?> d : destinations)
			for(Node<?> s : sources)
				if(d.dependsOn(s))
					return true;
		return false;
	}
	
	protected void initGraphFigures(Collection<? extends Collection<Node<T>>> nodeGraph) {
		for(Collection<Node<T>> nodes : nodeGraph)
			produceNodeFigureFor(nodes);
		
		for(Collection<Node<T>> nodes : nodeGraph)
			createNodeFigures(nodes);
	}


	protected void createNodeFigures(Collection<Node<T>> nodes) {
		GraphNode dependentFigure = produceNodeFigureFor(nodes);
		for (Node<T> node : nodes) {
			
			for (Node<T> provider : node.providers()) {
				GraphNode providerFigure = nodeFigureFor(provider);
				if(dependentFigure.equals(providerFigure))
					continue;

				GraphConnection connection = new GraphConnection(this, ZestStyles.CONNECTIONS_DIRECTED, dependentFigure, providerFigure);
				connection.changeLineColor(doNodesHaveCyclicDependency(connection.getSource(), connection.getDestination()) ? _red : _black);
			}
		}
	}
	
	
	protected NonMovableNode<T> nodeFigureFor(Node<T> node) {
		for(Entry<Collection<Node<T>>, NonMovableNode<T>> entry : _nodeFiguresByNode.entrySet()) {
			if(entry.getKey().contains(node))
				return entry.getValue();
		}
		return null;
	}
	

	protected NonMovableNode<T> produceNodeFigureFor(Collection<Node<T>> nodes) {
		NonMovableNode<T> result = _nodeFiguresByNode.get(nodes);
		if (result != null) return result;
		
		result = new NonMovableNode<T>(this, SWT.NONE, nodes);
		_nodeFiguresByNode.put(nodes, result);
		return result;
	}
}
