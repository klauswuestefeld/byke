package byke.views.layout.algorithm;

import static java.lang.Math.signum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;


public class LayoutAlgorithmBase<T> implements LayoutAlgorithm {

	private static final Random RANDOM = new Random();
	
	private final List<NodeElement> _nodeElements = new ArrayList<NodeElement>();

	private float _lowestStressEver;

	private boolean localMinimumReached;


	public LayoutAlgorithmBase(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		initGraphElements(graph, sizeProvider);
		applyLayout(initialLayout == null ? new CartesianLayout() : initialLayout);
		_lowestStressEver = relaxTowardLocalMinimum();
	}

	
	@Override
	public boolean improveLayoutStep() {
		if (_nodeElements.size() <= 1) return false;

		if (localMinimumReached)
			jolt();
		
		localMinimumReached = true;
		float stress = relaxTowardLocalMinimum();
		if (!localMinimumReached) return false;
		
		return hasImproved(stress);
	}


	private float relaxTowardLocalMinimum() {
		float stress = 0;
		for (NodeElement node : _nodeElements)
			stress += stressOnNodeAfterTryingToRelaxByOnePixel(node);
		return stress;
	}


	private void jolt() {
		for (NodeElement node : _nodeElements) {
			int rx = RANDOM.nextInt(300) - 150;
			int ry = RANDOM.nextInt(300) - 150;
			node.move(rx, ry);
		}
	}


	private boolean hasImproved(float currentStress) {
		if (currentStress < _lowestStressEver) {
			_lowestStressEver = currentStress;
			return true;
		}
		return false;
	}


	private float stressOnNodeAfterTryingToRelaxByOnePixel(NodeElement node) {
		float stressOnNode = StressMeter.applyForcesTo(node, _nodeElements);
		int dx = (int)signum(node.resultingForceX());
		stressOnNode = stressAfterTryingMove(node, stressOnNode, dx, 0);
		int dy = (int)signum(node.resultingForceY());
		stressOnNode = stressAfterTryingMove(node, stressOnNode, 0, dy);
		return stressOnNode;
	}


	private float stressAfterTryingMove(NodeElement node, float previousStress, int dx, int dy) {
		node.move(dx, dy);
		float newStress = StressMeter.applyForcesTo(node, _nodeElements);
		if (newStress < previousStress) {
			previousStress = newStress;
			localMinimumReached = false;
		} else
			node.move(-dx, -dy);
		return previousStress;
	}


	@Override
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