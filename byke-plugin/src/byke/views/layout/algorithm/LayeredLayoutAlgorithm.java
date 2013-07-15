package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.List;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;


public class LayeredLayoutAlgorithm<T> implements LayoutAlgorithm {

	private static final int LAYER_DIFFERENCE = 50;

	private final List<NodeElement> nodeElements;

	private float lowestStressEver;

	private boolean nodeHasMoved;


	public LayeredLayoutAlgorithm(Iterable<Node<T>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		nodeElements = asGraphElements(graph, sizeProvider);
		arrangeWith(initialLayout == null ? new CartesianLayout() : initialLayout);
		arrangeInLayers();
		lowestStressEver = relaxTowardLocalMinimum();
	}

	
	private void arrangeInLayers() {
		do {
			nodeHasMoved = false;
			for (NodeElement ne : nodeElements)
				moveProvidersDown(ne.node());
		} while (nodeHasMoved);
	}


	private void moveProvidersDown(Node<?> dependent) {
		for (Node<?> provider : dependent.providers())
			moveProviderDownIfNecesary(dependent, provider);
	}


	private void moveProviderDownIfNecesary(Node<?> dependent, Node<?> provider) {
		boolean isDependecyCycle = provider.dependsOn(dependent);
		int layerDifference = isDependecyCycle ? 0 : LAYER_DIFFERENCE;
		moveDownIfNecessary(dependent, provider, layerDifference);
	}


	private void moveDownIfNecessary(Node<?> upperNode, Node<?> lowerNode, int layerDifference) {
		NodeElement upper = element(upperNode);
		NodeElement lower = element(lowerNode);
		int upperLimit = upper.y + layerDifference;
		if (lower.y >= upperLimit) return;
		nodeHasMoved = true;
		lower.position(lower.x, upperLimit);
	}


	private NodeElement element(Node<?> node) {
		for (NodeElement ne : nodeElements)
			if (ne.node() == node) return ne;
		throw new IllegalStateException();
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

	
	private void arrangeWith(CartesianLayout layout) {
		for (NodeElement node : nodeElements)
			node.position(layout.coordinatesFor(node.name()));
	}

	
	private static <T> List<NodeElement> asGraphElements(Iterable<Node<T>> graph, NodeSizeProvider sizeProvider) {
		List<NodeElement> ret = new ArrayList<NodeElement>();
		for (Node<T> node : graph)
			ret.add(asGraphElement(node, sizeProvider));
		return ret;
	}

	
	private static NodeElement asGraphElement(Node<?> node, NodeSizeProvider sizeProvider) {
		return new NodeElement(node, sizeProvider.sizeGiven(node));
	}

}