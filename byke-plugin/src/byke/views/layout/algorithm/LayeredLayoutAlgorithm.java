package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.NodeElement;


public class LayeredLayoutAlgorithm implements LayoutAlgorithm {

	private static final int LAYER_HEIGHT = 50;
	private static final int LAYER_WIDTH = 10;
	
	protected final List<NodeElement> nodeElements;

	public LayeredLayoutAlgorithm(Iterable<Node<?>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		NodesByDepth nodesByDepth = NodesByDepth.layeringOf(graph);
		nodeElements = asGraphElements(nodesByDepth, sizeProvider);
		arrange();
	}

	
	@Override
	public boolean improveLayoutStep() {
		return true;
	}

	@Override
	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (NodeElement node : nodeElements)
			result.keep(node.name(), node.position());
		return result;
	}

	
	protected void arrange() {
		for (NodeElement node : nodeElements) {
			List<NodeElement> children = children(node);
			int size = children.size();
			if(size == 0)
				continue;
			
			int widths = 0;
			for (NodeElement child : children)
				widths += child.aura().width;
			int offset = widths / children.size();
			
			for (int i = 0; i < children.size(); i++) {
				NodeElement child = children.get(i);
				child.x = newX(node, child, i - size / 2, offset);
			}
		}
		
		for (NodeElement node : nodeElements)
			adjustMinimumDistance(node);
			
	}
	

	private int newX(NodeElement node, NodeElement child, int position, int offset) {
		return node.x + (position * offset) + node.aura().width / 2 - child.aura().width / 2;
	}


	private void adjustMinimumDistance(NodeElement node) {
		for(NodeElement sibling : siblings(node))
			if(hasIntersection(node, sibling))
				sibling.x = node.x + node.aura().width + LAYER_WIDTH;
	}


	private boolean hasIntersection(NodeElement node, NodeElement sibling) {
		return sibling.x > node.x && sibling.x < node.x + node.aura().width || 
				sibling.x + sibling.aura().width > node.x && sibling.x + sibling.aura().width < node.x + node.aura().width;
	}

	private List<NodeElement> siblings(NodeElement node) {
		List<NodeElement> siblings = new  ArrayList<NodeElement>();
		for(NodeElement nodeElement : nodeElements)
			if(nodeElement.y == node.y)
				siblings.add(nodeElement);
		
		siblings.remove(node);
		
		Collections.sort(siblings, new NodeComparator());
		
		return siblings;
	}

	private List<NodeElement> children(NodeElement node) {
		List<NodeElement> children = new ArrayList<NodeElement>();

		for (Node<?> n : node.node().providers()) {
			NodeElement nElement = find(n);
			if(!n.providers().contains(node.node()))
				children.add(nElement);
		}

		Collections.sort(children, new NodeComparator());

		return children;
	}

	
	private NodeElement find(Node<?> n) {
		for (NodeElement node : nodeElements)
			if(node.node().equals(n))
				return node;

			return null;
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