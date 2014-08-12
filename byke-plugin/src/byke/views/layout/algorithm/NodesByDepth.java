package byke.views.layout.algorithm;

import java.util.HashMap;
import java.util.Map;

import byke.dependencygraph.Node;

class NodesByDepth {

	private final Iterable<Node<?>> graph;
	private final Map<Node<?>, Integer> nodesByDepth = new HashMap<Node<?>, Integer>();
	private boolean nodeHasMoved;

	
	static NodesByDepth layeringOf(Iterable<Node<?>> graph) {
		NodesByDepth ret = new NodesByDepth(graph);
		ret.arrangeInLayers();
		return ret;
	}

	
	private NodesByDepth(Iterable<Node<?>> graph) {
		this.graph = graph;
	}

	
	Iterable<Node<?>> graph() {
		return graph;
	}

	
	int depthOf(Node<?> node) {
		return nodesByDepth.containsKey(node)
				? nodesByDepth.get(node)
				: 0;
	}
	
	private void arrangeInLayers() {
		do {
			nodeHasMoved = false;
			for (Node<?> node : graph)
				moveProvidersDown(node);
		} while (nodeHasMoved);
	}


	private void moveProvidersDown(Node<?> dependent) {
		for (Node<?> provider : dependent.providers())
			moveProviderDownIfNecesary(dependent, provider);
	}


	private void moveProviderDownIfNecesary(Node<?> dependent, Node<?> provider) {
		boolean isDependecyCycle = provider.dependsOn(dependent);
		int layerDifference = isDependecyCycle ? 0 : 1;
		moveDownIfNecessary(dependent, provider, layerDifference);
	}


	private void moveDownIfNecessary(Node<?> upperNode, Node<?> lowerNode, int layerDifference) {
		int minimumDepth = depthOf(upperNode) + layerDifference;
		if (depthOf(lowerNode) >= minimumDepth) return;
		nodeHasMoved = true;
		nodesByDepth.put(lowerNode, minimumDepth);
	}
	
}
