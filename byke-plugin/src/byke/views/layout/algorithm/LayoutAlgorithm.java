package byke.views.layout.algorithm;

import static java.lang.Math.signum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.DependencyElement;
import byke.views.layout.criteria.GraphElement;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;


public class LayoutAlgorithm<T> {

	private static final int ONE_TENTH_OF_A_SECOND = 100;
	private static final Random RANDOM = new Random(0);
	
	protected final List<NodeElement> _nodeElements = new ArrayList<NodeElement>();
	protected final List<DependencyElement> _dependencyElements = new ArrayList<DependencyElement>();
	protected final ArrayList<GraphElement> _allElements = new ArrayList<GraphElement>();

	protected final StressMeter _stressMeter = new StressMeter();
	protected float _lowestStressEver;

	private final NodeSizeProvider _sizeProvider;
	private int nodeJoltContdown = 0;


	public LayoutAlgorithm(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		_sizeProvider = sizeProvider;
		initGraphElements(graph);

		applyLayout(initialLayout == null ? new CartesianLayout() : initialLayout);

		_lowestStressEver = measureStress();
	}


	public boolean improveLayoutForAWhile() {
		if (_nodeElements.size() <= 1) return false;
		
		long start = System.currentTimeMillis();
		do {
			if (	improveLayoutStep()) return true;
		} while (System.currentTimeMillis() - start < ONE_TENTH_OF_A_SECOND);
		
		return false;
	}

	public boolean improveLayoutStep() {
		if (_nodeElements.size() <= 1) return false;
		
		joltSomeNodeIfNecessary();

		for (NodeElement node : _nodeElements)
			relaxByOnePixel(node);

		return hasImproved();
	}


	private void joltSomeNodeIfNecessary() {
		if (nodeJoltContdown-- != 0) return;
		
		int rx = RANDOM.nextInt(400);
		int ry = RANDOM.nextInt(400);
		NodeElement node = _nodeElements.get(RANDOM.nextInt(_nodeElements.size()));
		node.move(rx, ry);
		nodeJoltContdown = Math.max(rx, ry);
	}


	private boolean hasImproved() {
		float stress = measureStress();
		if (stress < _lowestStressEver) {
			_lowestStressEver = stress;
			return true;
		}
		return false;
	}


	private void relaxByOnePixel(NodeElement node) {
		node.move((int)signum(node.pendingForceX()), (int)signum(node.pendingForceY()));
	}


	private float measureStress() {
		return _stressMeter.applyForcesTo(_nodeElements);
	}

	
	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (NodeElement node : _nodeElements)
			result.keep(node.name(), node.position());
		return result;
	}

	
	private void applyLayout(CartesianLayout layout) {
		for (NodeElement node : _nodeElements)
			node.position(layout.coordinatesFor(node.name()));
	}

	
	private void initGraphElements(Iterable<Node<T>> graph) {
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

		result = new NodeElement(node, _sizeProvider.sizeGiven(node), _stressMeter);
		nodeElementsByNode.put(node, result);
		return result;
	}

}