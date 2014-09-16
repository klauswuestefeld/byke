package byke.views.layout.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.gef4.layout.algorithms.SpringLayoutAlgorithm;
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
import byke.views.layout.CartesianLayout;
import byke.views.layout.Coordinates;

public class NonMovableGraph<T> extends GraphWidget {

	private final Color _red = new Color(getDisplay(), new RGB(255, 0, 0));
	private final Color _black = new Color(getDisplay(), new RGB(0, 0, 0));
	private final Color _yellow = new Color(getDisplay(), new RGB(204, 204, 0));
	
	private final Map<Node<T>, NonMovableNode> _nodeFiguresByNode = new HashMap<Node<T>, NonMovableNode>();
	private GraphNode _selectedNodeFigure;
	
	public NonMovableGraph(Composite parent, Collection<Node<T>> graph) {
		super(parent, SWT.NONE);
		
		getLightweightSystem().setEventDispatcher(new SWTEventDispatcher() {
      @Override
      public void dispatchMouseMoved(org.eclipse.swt.events.MouseEvent me) {}
    });

		setLayoutAlgorithm(new SpringLayoutAlgorithm(), true);
//		setLayoutAlgorithm(new SugiyamaLayoutAlgorithm(), true);
//		setLayoutAlgorithm(new TreeLayoutAlgorithm(), true);
		addSelectionListener(new SelectionAdapter() {
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
    });
		if(graph != null)
			initGraphFigures(graph);
	}

	private boolean doNodesHaveCyclicDependency(GraphNode source, GraphNode destination) {
		return ((Node<T>)destination.getData()).dependsOn((Node<T>)source.getData());
	}
	
	private void initGraphFigures(Iterable<Node<T>> nodeGraph) {
		for (Node<T> node : nodeGraph) {
			GraphNode dependentFigure = produceNodeFigureFor(node);

			for (Node<T> provider : node.providers()) {
				GraphNode providerFigure = produceNodeFigureFor(provider);
				GraphConnection connection = new GraphConnection(this, ZestStyles.CONNECTIONS_DIRECTED, dependentFigure, providerFigure);
				connection.changeLineColor(doNodesHaveCyclicDependency(connection.getSource(), connection.getDestination()) ? _red : _black);
			}
		}

	}
	
	private NonMovableNode produceNodeFigureFor(Node<T> node) {
		NonMovableNode result = _nodeFiguresByNode.get(node);
		if (result != null) return result;

		result = new NonMovableNode(this, SWT.NONE, node);
		_nodeFiguresByNode.put(node, result);
		return result;
	}

	public void useLayout(CartesianLayout newLayout) {
		for(GraphNode node : getNodes()) {
			Coordinates coordinates = newLayout.coordinatesFor(node.getText());
			node.getFigure().getBounds().x = coordinates._x;
			node.getFigure().getBounds().y = coordinates._y;
		}
	}
}
