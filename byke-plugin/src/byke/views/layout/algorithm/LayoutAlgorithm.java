package byke.views.layout.algorithm;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.signum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;


public class LayoutAlgorithm<T> {

	private static final Random RANDOM = new Random();
	
	private final List<NodeElement> _nodeElements = new ArrayList<NodeElement>();

	private float _lowestStressEver;

	private int nodeJoltContdown = 0;


	public LayoutAlgorithm(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		initGraphElements(graph, sizeProvider);
		applyLayout(initialLayout == null ? new CartesianLayout() : initialLayout);
		_lowestStressEver = measureStress();
	}

	
	public boolean improveLayoutStep() {
		if (_nodeElements.size() <= 1) return false;
		
		for (NodeElement node : _nodeElements)
			relaxByOnePixel(node);

		joltSomeNodeIfNecessary();
		
		return hasImproved();
	}


	private void joltSomeNodeIfNecessary() {
		if (nodeJoltContdown-- != 0) return;
		
		int rx = RANDOM.nextInt(1000) - 500;
		int ry = RANDOM.nextInt(1000) - 500;
		NodeElement node = _nodeElements.get(RANDOM.nextInt(_nodeElements.size()));
		node.move(rx, ry);
		nodeJoltContdown = max(abs(rx), abs(ry));
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
		int dx = (int)signum(node.resultingForceX());
		int dy = (int)signum(node.resultingForceY());
		node.move(dx, dy);
	}


	private float measureStress() {
		return StressMeter.applyForcesTo(_nodeElements);
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

	
	private void initGraphElements(Iterable<Node<T>> graph, NodeSizeProvider sizeProvider) {
		for (Node<T> node : graph)
			_nodeElements.add(elementFor(node, sizeProvider));
	}

	
	private NodeElement elementFor(Node<T> node, NodeSizeProvider sizeProvider) {
		return new NodeElement(node, sizeProvider.sizeGiven(node));
	}

}