package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import byke.dependencygraph.Node;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;

public class NodeElementsByDepth {
	
	private static final int LAYER_HEIGHT = 100;
	private final Map<Integer, List<NodeElement>> _nodesByDepth;

	
	static NodeElementsByDepth asGraphElements(Collection<Node<?>> graph, NodeSizeProvider sizeProvider) {
		NodesByDepth nodesByDepth = NodesByDepth.layeringOf(graph);
		Map<Integer, List<NodeElement>> ret = new HashMap<Integer, List<NodeElement>>();
		for (Node<?> node : nodesByDepth.graph()) {
			int depth = nodesByDepth.depthOf(node);
			List<NodeElement> list = ret.get(depth);
			if(list == null)
				ret.put(depth, new ArrayList<NodeElement>());
			
			ret.get(depth).add(asGraphElement(node, nodesByDepth, sizeProvider));
		}
		
		return new NodeElementsByDepth(ret);
	}
	
	
	private static NodeElement asGraphElement(Node<?> node, NodesByDepth nodesByLayer, NodeSizeProvider sizeProvider) {
		NodeElement ret = new NodeElement(node, sizeProvider.sizeGiven(node));
		ret.position(ret.x(), nodesByLayer.depthOf(node) * LAYER_HEIGHT);
		return ret;
	}
	
	
	private NodeElementsByDepth(Map<Integer, List<NodeElement>> nodesByDepth) {
		_nodesByDepth = nodesByDepth;
	}

	
	public Set<Integer> layers() {
		return _nodesByDepth.keySet();
	}

	
	public List<NodeElement> byLayer(Integer layer) {
		List<NodeElement> list = _nodesByDepth.get(layer);
		Collections.sort(list, new Comparator<NodeElement>() {
			@Override public int compare(NodeElement o1, NodeElement o2) {
				return o1.name().compareTo(o2.name());
			}
		});
		return list;
	}
	
}
