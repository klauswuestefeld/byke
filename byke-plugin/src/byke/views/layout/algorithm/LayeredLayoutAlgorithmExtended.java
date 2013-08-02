package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;

public class LayeredLayoutAlgorithmExtended extends LayeredLayoutAlgorithm {

	private static final int LAYER_WIDTH = 100;
	
	public LayeredLayoutAlgorithmExtended(Iterable<Node<?>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		super(graph, initialLayout, sizeProvider);
	}
	
	@Override
	protected void arrangeWith(CartesianLayout layout) {
		Collections.sort(nodeElements, new Comparator<NodeElement>() {
			@Override
			public int compare(NodeElement o1, NodeElement o2) {
				return o1.y < o2.y ? 0 : 1;
			}});
		
		
		for (NodeElement node : nodeElements) {
			List<NodeElement> children = children(node);
			for(int i = 0; i < children.size(); i++) {
				double qty = 0;
				
				if(children.size() % 2 == 0 && children.size() / 2 <= i)
					qty = 0.5;
				if(children.size() % 2 == 0 && children.size() / 2 - 1 >= i)
					qty = 0.5;
				NodeElement child = children.get(i);
				child.position((node.aura().width / 2 + node.x) - (child.aura().width / 2) + (int)(LAYER_WIDTH * (i + qty - children.size() / 2)), child.y);
			}
		}
	}
	
	private List<NodeElement> children(NodeElement node) {
		List<NodeElement> children = new ArrayList<NodeElement>();
		
		for(Node<?> n : node.node().providers()) {
			NodeElement nElement = find(n);
			children.add(nElement);
		}
		
		Collections.sort(children, new Comparator<NodeElement>() {
			@Override
			public int compare(NodeElement o1, NodeElement o2) {
				return o1.name().compareTo(o2.name());
			}});
		
		return children; 
	}
	
	private NodeElement find(Node<?> n) {
		for (NodeElement node : nodeElements)
			if(node.node().equals(n))
				return node;

			return null;
	}

}
