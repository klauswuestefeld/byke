package byke.views.layout.algorithm.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.algorithm.LayoutAlgorithm;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;


public class RandomAverage<T> implements LayoutAlgorithm {

	private static final Random RANDOM = new Random();
	
	private final List<AveragingNode> _nodeElements = new ArrayList<AveragingNode>();


	public RandomAverage(Iterable<Node<T>> graph, NodeSizeProvider sizeProvider) {
		initNodeElements(graph, sizeProvider);
	}

	
	@Override
	public boolean improveLayoutStep() {
		if (_nodeElements.size() <= 1) return false;

		int scale = 5000;
		for (AveragingNode node : _nodeElements)
			node.position(RANDOM.nextInt(scale), RANDOM.nextInt(scale));

		StressMeter.applyAsymmetricalForcesTo(_nodeElements);
		
		for (AveragingNode node : _nodeElements)
			node.takeAveragePositionDividedBy(scale);
		
		return false;
	}


	@Override
	public CartesianLayout layoutMemento() {
		CartesianLayout ret = new CartesianLayout();
		for (NodeElement node : _nodeElements)
			ret.keep(node.name(), node.position());
		return ret;
	}

	
	private void initNodeElements(Iterable<Node<T>> graph, NodeSizeProvider sizeProvider) {
		for (Node<T> node : graph)
			_nodeElements.add(elementFor(node, sizeProvider));
	}

	
	private static AveragingNode elementFor(Node<?> node, NodeSizeProvider sizeProvider) {
		return new AveragingNode(node, sizeProvider.sizeGiven(node));
	}

}