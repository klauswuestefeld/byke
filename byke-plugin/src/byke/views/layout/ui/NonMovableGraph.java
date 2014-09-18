package byke.views.layout.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.gef4.layout.algorithms.TreeLayoutAlgorithm;
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
	
	protected Collection<Collection<Node<T>>> calculateSubGraphs(Collection<Node<T>> graph) {
		Collection<Collection<Node<T>>> newGraph = new ArrayList<Collection<Node<T>>>();
		
		for(Node<T> node : graph) {
			for(Node<T> provider : node.providers()) {
				if(node.equals(provider))
					continue;
				if(provider.dependsOn(node))
					if(!hasNode(provider, newGraph))
						newGraph.add(Arrays.asList(node, provider));
			}
		}
		
		for(Node<T> node : graph) {
			if(!hasNode(node, newGraph)) {
				List<Node<T>> asList = new ArrayList<Node<T>>();
				asList.add(node);
				newGraph.add(asList);
			}
		}
		
		return newGraph;
	}

	private boolean hasNode(Node<T> provider, Collection<Collection<Node<T>>> newGraph) {
		for(Collection<Node<T>> nodes : newGraph)
			if(nodes.contains(provider))
				return true;
		return false;
	}

	public NonMovableGraph(Composite parent, final Collection<Node<T>> graph) {
		super(parent, ZestStyles.NONE);
		
		getLightweightSystem().setEventDispatcher(new SWTEventDispatcher() {
      @Override
      public void dispatchMouseMoved(org.eclipse.swt.events.MouseEvent me) {}
    });

		
//		setLayoutAlgorithm(new SpringLayoutAlgorithm(), true);
//		setLayoutAlgorithm(new SugiyamaLayoutAlgorithm(), true);
		setLayoutAlgorithm(new TreeLayoutAlgorithm(), true);
		
		
		addSelectionListener(selectionListener());
		
		Collection<? extends Collection<Node<T>>> newGraph = calculateSubGraphs(graph);
		initGraphFigures(newGraph);
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
		
//		return ((Node<?>)destination.getData()).dependsOn((Node<?>)source.getData());
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
