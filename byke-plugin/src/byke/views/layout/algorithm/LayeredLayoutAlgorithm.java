package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;


public class LayeredLayoutAlgorithm implements LayoutAlgorithm {

	private static final int LAYER_HEIGHT = 50;

	protected final List<NodeElement> nodeElements;

	private float lowestStressEver;


	public LayeredLayoutAlgorithm(Iterable<Node<?>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		NodesByDepth nodesByDepth = NodesByDepth.layeringOf(graph);
		nodeElements = asGraphElements(nodesByDepth, sizeProvider);
		arrangeWith(initialLayout == null ? new CartesianLayout() : initialLayout);
		lowestStressEver = relaxTowardLocalMinimum();
	}

	
	@Override
	public boolean improveLayoutStep() {
		if (nodeElements.size() <= 1) return false;
		float stress = relaxTowardLocalMinimum();
		return hasImproved(stress);
	}


	private float relaxTowardLocalMinimum() {
		return 0;
	}


	private boolean hasImproved(float currentStress) {
		if (currentStress < lowestStressEver) {
			lowestStressEver = currentStress;
			return true;
		}
		return false;
	}




	@Override
	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (NodeElement node : nodeElements)
			result.keep(node.name(), node.position());
		return result;
	}

	
	protected void arrangeWith(CartesianLayout layout) {
		Random random = new Random();
		for (NodeElement node : nodeElements)
//		node.position(layout.coordinatesFor(node.name())._x, node.y);
			node.position(random .nextInt(700), node.y);
	}

	
	private static <T> List<NodeElement> asGraphElements(NodesByDepth nodesByLayer, NodeSizeProvider sizeProvider) {
		List<NodeElement> ret = new ArrayList<NodeElement>();
		for (Node<?> node : nodesByLayer.graph())
			ret.add(asGraphElement(node, nodesByLayer, sizeProvider));
		return ret;
	}

	
	private static NodeElement asGraphElement(Node<?> node, NodesByDepth nodesByLayer, NodeSizeProvider sizeProvider) {
		NodeElement ret = new NodeElement(node, sizeProvider.sizeGiven(node));
		ret.position(ret.x, nodesByLayer.depthOf(node) * LAYER_HEIGHT);
		return ret;
	}

}