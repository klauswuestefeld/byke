package byke.views.layout.algorithm.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.algorithm.LayoutAlgorithm;
import byke.views.layout.criteria.NodeElement;

public class RandomAverage<T> extends LayoutAlgorithm<T> {

	private final List<AveragingNode> _averagingNodes;

	private static final Random RANDOM = new Random();
	private static final float INITIAL_RANDOM_AMPLITUDE = 1000;
//	private static final float INITIAL_RANDOM_AMPLITUDE = 100;
//	private static final float INITIAL_RANDOM_AMPLITUDE = 10;
//	private static final float INITIAL_RANDOM_AMPLITUDE = 1;
	private float _randomAmplitude = INITIAL_RANDOM_AMPLITUDE;
	private float _ellapsedTime = 0;

//	private long t0 = System.currentTimeMillis();
	
	public RandomAverage(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);

		_averagingNodes = new ArrayList<AveragingNode>(_nodeElements.size()); // Necessary only to avoid casting all the time.
		for (NodeElement element : _nodeElements)
			_averagingNodes.add((AveragingNode)element);
		
		for (NodeElement element : _nodeElements)
			System.out.println(element.aura()._width);
		
	}

	
	@Override
	public void improveLayoutStep() {
//		if (System.currentTimeMillis() - t0 > 10000) {
//			t0 = System.currentTimeMillis();
//			_randomAmplitude /= 10;
//			System.out.println("_randomAmplitude = " + _randomAmplitude);
//		}
		
		//CartesianLayout currentLayout = layoutMemento();
		randomize();
		_stressMeter.applyForcesTo(_averagingNodes, _allElements);
		//layout(currentLayout);

		float smallestTimeFrame = minimumTimeToMoveOnePixel();
		takeAveragePosition(smallestTimeFrame);
	}
	

	private float minimumTimeToMoveOnePixel() {
		float smallestTimeFrame = 100f;
		for (AveragingNode node : _averagingNodes)
			if (node.timeNeededToMoveOnePixel() < smallestTimeFrame) smallestTimeFrame = node.timeNeededToMoveOnePixel();
		return smallestTimeFrame;
	}

	
	private void takeAveragePosition(float nextTimeFrame) {
		_ellapsedTime += nextTimeFrame;
		
		for (AveragingNode node : _averagingNodes)
			node.takeAveragePosition(_ellapsedTime, nextTimeFrame);
		
		System.out.println("Time Frame: " + nextTimeFrame);
	}

	
	private void randomize() {
		for (AveragingNode node : _averagingNodes)
//			node.position(node._x + random(), node._y + random());
			node.position(random(), random());
	}


	private float random() {
//		int i = RANDOM.nextInt(4);
//		while (i-- > 0) RANDOM.nextInt();
		return (RANDOM.nextFloat() - 0.5f) * _randomAmplitude;
	}

	
	protected NodeElement createNodeElement(Node<?> node) {
		return new AveragingNode(node, _stressMeter);
	}

}
