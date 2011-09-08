package byke.views.layout.algorithm.relaxer;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.algorithm.LayoutAlgorithm;
import byke.views.layout.criteria.NodeElement;


public class InertialRelaxer<T> extends LayoutAlgorithm<T> {

	private float _timeFrame = 10;

	public InertialRelaxer(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
	}

	public void improveLayoutStep() {
		if (hasConverged()) return;
		
System.out.println(_timeFrame);
		give(minimumTimeNeededToMoveOnePixel() * _timeFrame);
	}

	@Override
	protected void adaptToFailure() {
		_timeFrame *= 0.996f;
	}

	@Override
	protected void adaptToSuccess() {
		_timeFrame = Math.min(_timeFrame * 1.1f, 100); 
	}

	private boolean hasConverged() {
		return _timeFrame < Constants.MINIMUM_TIME_FRAME;
	}

	protected NodeElement createNodeElement(Node<?> node) {
		return new InertialNode(node, _stressMeter);
	}
	
}
