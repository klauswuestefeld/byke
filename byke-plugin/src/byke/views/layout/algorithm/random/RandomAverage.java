//package byke.views.layout.algorithm.random;
//
//import java.util.Random;
//
//import byke.dependencygraph.Node;
//import byke.views.layout.CartesianLayout;
//import byke.views.layout.NodeSizeProvider;
//import byke.views.layout.algorithm.LayoutAlgorithm;
//import byke.views.layout.criteria.NodeElement;
//
//public class RandomAverage<T> extends LayoutAlgorithm<T> {
//
//	private static final Random RANDOM = new Random();
//	private static final float INITIAL_RANDOM_AMPLITUDE = 1000;
//	private float _randomAmplitude = INITIAL_RANDOM_AMPLITUDE;
//	
//	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
//		super(graph, initialLayout, sizeProvider);
//	}
//
//	
//	@Override
//	public void improveLayoutStep() {
//		//CartesianLayout currentLayout = layoutMemento();
//		randomize();
//		_stressMeter.applyForcesTo(_averagingNodes, _allElements);
//		//layout(currentLayout);
//
//		takeAveragePosition(smallestTimeFrame);
//	}
//	
//
//	private void randomize() {
//		for (AveragingNode node : _averagingNodes)
//			node.position(node._x + random(), node._y + random());
//	}
//
//
//	private float random() {
//		return (RANDOM.nextFloat() - 0.5f) * _randomAmplitude;
//	}
//
//	
//	protected NodeElement createNodeElement(Node<?> node) {
//		return new AveragingNode(node, _stressMeter);
//	}
//
//}
