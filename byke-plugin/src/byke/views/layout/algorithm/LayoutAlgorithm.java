package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.DependencyElement;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;


public abstract class LayoutAlgorithm<T> {

	protected final List<NodeElement> _nodeElements = new ArrayList<NodeElement>();
	protected final List<DependencyElement> _dependencyElements = new ArrayList<DependencyElement>();
	protected final ArrayList<GraphElement> _allElements = new ArrayList<GraphElement>();

	protected final StressMeter _stressMeter = new StressMeter();
	protected float _lowestStressEver;


	protected LayoutAlgorithm(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		initGraphElements(graph);

		if (initialLayout == null) initialLayout = new CartesianLayout();
		layout(initialLayout);

		_lowestStressEver = measureStress();
	}


	public boolean improveLayoutForAWhile() {
		if (_nodeElements.size() <= 1) return false;
		
		long start = System.nanoTime();
		do {
			improveLayoutStep();
			float stress = measureStress();
			if (stress < _lowestStressEver) {
				adaptToSuccess();
				_lowestStressEver = stress;
				return true;
			}
		} while (System.nanoTime() - start < 1000000); // One millisecond at least.
		
		adaptToFailure();
		return false;
	}

	
	public abstract void improveLayoutStep();

	protected void adaptToFailure() {}
	protected void adaptToSuccess() {}

	protected float measureStress() {
		return _stressMeter.applyForcesTo(_nodeElements, _allElements);
	}

	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (NodeElement node : _nodeElements)
			result.keep(node.name(), node.position());
		return result;
	}

	private void layout(CartesianLayout layout) {
		for (NodeElement node : _nodeElements)
			node.position(layout.coordinatesFor(node.name()));
	}

	protected void initGraphElements(Iterable<Node<T>> graph) {
		Map<Node<T>, NodeElement> nodeElementsByNode = new HashMap<Node<T>, NodeElement>();
		List<DependencyElement> dependencyElements = new ArrayList<DependencyElement>();

		for (Node<T> node : graph) {
			NodeElement dependentElement = produceElementFor(node, nodeElementsByNode);

			for (Node<T> provider : node.providers()) {
				NodeElement providerElement = produceElementFor(provider, nodeElementsByNode);
				dependencyElements.add(new DependencyElement(dependentElement, providerElement));
			}
		}

		_nodeElements.addAll(nodeElementsByNode.values());
		_dependencyElements.addAll(dependencyElements);

		_allElements.addAll(_nodeElements);
		_allElements.addAll(_dependencyElements);
	}

	private NodeElement produceElementFor(Node<T> node, Map<Node<T>, NodeElement> nodeElementsByNode) {
		NodeElement result = nodeElementsByNode.get(node);
		if (result != null) return result;

		result = createNodeElement(node);
		nodeElementsByNode.put(node, result);
		return result;
	}

	protected float minimumTimeNeededToMoveOnePixel() {
		float result = Float.MAX_VALUE;
		for (NodeElement node : _nodeElements) {
			if (node.timeNeededToMoveOnePixel() < result)
				result = node.timeNeededToMoveOnePixel();
		}
		return result;
	}
	
	protected void give(float timeFrame) {
		for (NodeElement node : _nodeElements)
			node.give(timeFrame);
	}

	protected abstract NodeElement createNodeElement(Node<?> node);

}