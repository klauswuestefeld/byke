//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byke.views.layout.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseListener.Stub;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Composite;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.Coordinates;
import byke.views.layout.NodeSizeProvider;


public class GraphCanvas<T> extends FigureCanvas implements NodeSizeProvider {

	private static final int MARGIN_PIXELS = 3;


	public interface Listener<LT> {
		void nodeSelected(Node<LT> node);
	}


	public GraphCanvas(Composite parent, Collection<Node<T>> graph, CartesianLayout initialLayout, Listener<T> listener) {
		super(parent, new LightweightSystem());
		setScrollBarVisibility(FigureCanvas.AUTOMATIC);

		this.setContents(_graphFigure);

		if (listener == null) throw new IllegalArgumentException("listener");
		_listener = listener;
		_graphFigure.addMouseListener(backgroundDoubleClickListener());

		initGraphFigures(graph);
		initRootGraphFigure();

		initialLayout(translatedToOrigin(initialLayout));
		recomputeGraphFigureSize();
	}


	private final MouseListener _nodeDoubleClickListener = nodeDoubleClickListener();
	private final MouseListener _nodeSingleClickListener = nodeSingleClickListener();

	private final IFigure _graphFigure = new Figure();
	private NodeFigure<T> _selectedNodeFigure;

	private DependencyFigure[] _dependencyFigures;
	private final Map<Node<T>, NodeFigure<T>> _nodeFiguresByNode = new HashMap<Node<T>, NodeFigure<T>>();
	private final Map<IFigure, Node<T>> _nodeByFigure = new HashMap<IFigure, Node<T>>();
	
	private final Listener<T> _listener;
		
	private LayoutMorpher _morpher;


	private Stub backgroundDoubleClickListener() {
		return new MouseListener.Stub() {
			@Override
			public void mouseDoubleClicked(MouseEvent e) {
				_listener.nodeSelected(null);
				e.consume();
			}
		};
	}

	private Stub nodeDoubleClickListener() {
		return new MouseListener.Stub() { @Override
		public void mouseDoubleClicked(MouseEvent event) {
			Node<T> node = _nodeByFigure.get((IFigure)event.getSource());
			_listener.nodeSelected(node);
			event.consume();
		}};
	}
	

	private Stub nodeSingleClickListener() {
		return new MouseListener.Stub() { @Override
		public void mousePressed(MouseEvent event) {
			selectNode((IFigure) event.getSource());
			event.consume();
		}};
	}	

		
	private void selectNode(IFigure figure) {
		if (figure == null) throw new IllegalStateException(); //TODO Delete if this never happens.				

		if (_selectedNodeFigure != null)
			_selectedNodeFigure.notifyNodeDeselected();
		
		//FIXME this sucks and I'm not sure I want to create another map.
		Node<T> node = this._nodeByFigure.get(figure);
		NodeFigure<T> nodeFigure = _nodeFiguresByNode.get(node);
		nodeFigure.notifyNodeSelected();				
		_selectedNodeFigure = nodeFigure;
	}

	
	public void animationStep() {
		animationStep(3);
	}

	
	private void refreshDependencies() {
		for (DependencyFigure dependencyFigure : _dependencyFigures)
			dependencyFigure.refresh();
	}

	
	private void initRootGraphFigure() {
		for (NodeFigure<?> nodeFigure : nodeFigures()) {
			IFigure figure = nodeFigure.figure();
			_graphFigure.add(figure);
			figure.setSize(figure.getPreferredSize());
		}

		for (DependencyFigure dependencyFigure : _dependencyFigures)
			_graphFigure.add(dependencyFigure.figure());
	}

	
	private Collection<NodeFigure<T>> nodeFigures() {
		return _nodeFiguresByNode.values();
	}

	
	private void initGraphFigures(Iterable<Node<T>> nodeGraph) {

		List<DependencyFigure> dependencyFigures = new ArrayList<DependencyFigure>();

		for (Node<T> node : nodeGraph) {
			NodeFigure<T> dependentFigure = produceNodeFigureFor(node);

			for (Node<T> provider : node.providers()) {
				NodeFigure<T> providerFigure = produceNodeFigureFor(provider);
				DependencyFigure nodeDependency = new DependencyFigure(dependentFigure, providerFigure);
				dependencyFigures.add(nodeDependency);
			}
		}

		_dependencyFigures = new DependencyFigure[dependencyFigures.size()];
		_dependencyFigures = dependencyFigures.toArray(_dependencyFigures);
	}

	
	private NodeFigure<T> produceNodeFigureFor(Node<T> node) {
		NodeFigure<T> result = _nodeFiguresByNode.get(node);
		if (result != null) return result;

		result = new NodeFigure<T>(node);
		_nodeFiguresByNode.put(node, result);
		final IFigure figure = result.figure();

		figure.addMouseListener(_nodeDoubleClickListener);
		figure.addMouseListener(_nodeSingleClickListener);
		_nodeByFigure.put(figure, node);
		return result;
	}

	
	private void initialLayout(CartesianLayout initialLayout) {

		for (NodeFigure<?> figure : nodeFigures()) {
			Coordinates coordinates = initialLayout.coordinatesFor(figure.name());
			figure.position(new Point(coordinates._x, coordinates._y));
		}
	}

	
	public void useLayout(CartesianLayout newLayout) {
		CartesianLayout translatedLayout = translatedToOrigin(newLayout);
		_morpher = new LayoutMorpher(nodeFigures(), translatedLayout);
	}

	
	@Override
	public Rectangle sizeGiven(Node<?> node) {
		Rectangle result = _nodeFiguresByNode.get(node).figure().getBounds();
		return new Rectangle(result);
	}

	
	private static CartesianLayout translatedToOrigin(CartesianLayout layout) {
		int smallestX = Integer.MAX_VALUE;
		int smallestY = Integer.MAX_VALUE;

		for (String nodeName : layout.nodeNames()) {
			Coordinates coordinates = layout.coordinatesFor(nodeName);
			if (coordinates._x < smallestX) smallestX = coordinates._x;
			if (coordinates._y < smallestY) smallestY = coordinates._y;
		}

		int dx = -smallestX + MARGIN_PIXELS;
		int dy = -smallestY + MARGIN_PIXELS;
		
		CartesianLayout result = new CartesianLayout();
		for (String nodeName : layout.nodeNames()) {
			Coordinates coordinates = layout.coordinatesFor(nodeName);
			result.keep(nodeName, coordinates.translatedBy(dx, dy));
		}
		return result;
	}
	
	public void animationStep(int size) {
		if (_morpher == null) return;
		_morpher.morphingStep(size);
		if (_morpher.done()) _morpher = null;

		refreshDependencies();

		recomputeGraphFigureSize();
	}

	
	private void recomputeGraphFigureSize() {
		// Recompute the size of the _graphFigure so scrolling will work
		int maxX = 0;
		int maxY = 0;
		for (NodeFigure<T> figure : _nodeFiguresByNode.values())
		{
			Rectangle figureBounds = figure.figure().getBounds();
			maxX = Math.max(maxX, figureBounds.x + figureBounds.width);
			maxY = Math.max(maxY, figureBounds.y + figureBounds.height);
		}
		_graphFigure.setSize(maxX, maxY);
		_graphFigure.setPreferredSize(_graphFigure.getSize());
	}

	public void zoom(int x, int y, int factor) {
		for(Map.Entry<IFigure, Node<T>> entry : _nodeByFigure.entrySet()) {
			IFigure key = entry.getKey();

			key.getBounds().x = calculateZoom(key.getBounds().x, x, factor);
			key.getBounds().y = calculateZoom(key.getBounds().y, y, factor);
			
			recomputeGraphFigureSize();
		}
	}

	private int calculateZoom(int figurePosition, int mousePosition, int factor) {
		if(mousePosition > figurePosition)
			return figurePosition + 1 * factor;
		return figurePosition - 1 * factor;
	}
}
