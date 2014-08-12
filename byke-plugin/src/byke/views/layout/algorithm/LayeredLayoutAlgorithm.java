package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.NodeSizeProvider;
import byke.views.layout.criteria.Element;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.NullNodeElement;


public class LayeredLayoutAlgorithm implements LayoutAlgorithm {

	private final NodeElementsByDepth _nodeElements;

	
	public LayeredLayoutAlgorithm(Collection<Node<?>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		_nodeElements = NodeElementsByDepth.asGraphElements(graph, sizeProvider);
		arrange();
	}

	
	@Override
	public boolean improveLayoutStep() {
		return true;
	}

	
	@Override
	public CartesianLayout layoutMemento() {
		CartesianLayout result = new CartesianLayout();
		for (Integer layer : _nodeElements.layers())
			for (NodeElement node : _nodeElements.byLayer(layer))
				result.keep(node.name(), node.position());
		return result;
	}

	
	private void arrange() {
		spread();
		reduceEdgeCrossings();
		beautifier();
	}
	
	
	protected void spread() {
		for (Integer layer : _nodeElements.layers())
			spread(layer);
	}

	
	private void spread(Integer layer) {
		Element lastNodeElement = new NullNodeElement();
		for (NodeElement nodeElement : _nodeElements.byLayer(layer)) {
			nodeElement.x(calculateX(lastNodeElement));
			lastNodeElement = nodeElement;
		}
	}
	
	
	private int calculateX(Element nodeElement) {
		return nodeElement.x() + nodeElement.width();
	}
	
	
	private void beautifier() {
		for (Integer layer : _nodeElements.layers())
			beautifier(layer);
	}


	private void beautifier(Integer layer) {
		for (NodeElement nodeElement : _nodeElements.byLayer(layer)) {
			List<NodeElement> providers = providers(nodeElement);
			
			int startPoint = startPoint(providers, nodeElement);
			Element lastNodeElement = new NullNodeElement();
			for(NodeElement provider : providers) {
				provider.x(startPoint + lastNodeElement.x() + lastNodeElement.width());
				lastNodeElement = provider;
				startPoint = 0;
			}
		}
	}


	private int startPoint(List<NodeElement> providers, NodeElement nodeElement) {
		int totalWidth = 0;
		for(NodeElement provider : providers)
			totalWidth += provider.width();
		
		int qtyProviders = providers.size();
		int width = totalWidth / (qtyProviders == 0 ? 1 : qtyProviders) * qtyProviders / 2;
		return nodeElement.width() / 2 + nodeElement.x() - width;
	}

	
	private List<NodeElement> providers(NodeElement nodeElement) {
		List<NodeElement> providers = new ArrayList<NodeElement>();
		
		for (Integer layer : _nodeElements.layers())
			providers.addAll(providers(nodeElement, layer));
		
		 return providers;
	}


	private List<NodeElement> providers(NodeElement nodeElement, Integer layer) {
		List<NodeElement> providers = new ArrayList<NodeElement>();
		
		for(NodeElement possibleProvider : _nodeElements.byLayer(layer))
			if(isProvider(nodeElement, possibleProvider))
				providers.add(possibleProvider);
		
		return providers;
	}


	private boolean isProvider(NodeElement nodeElement, NodeElement possibleProvider) {
		return nodeElement.node().providers().contains(possibleProvider.node());
	}

	
	private void reduceEdgeCrossings() {
		for (Integer layer : _nodeElements.layers())
			reduceEdgeCrossings(layer);
	}


	private void reduceEdgeCrossings(Integer layer) {
		for(NodeElement nodeElement : _nodeElements.byLayer(layer))
			for(NodeElement otherNodeElement : _nodeElements.byLayer(layer))
				eliminateCrossings(nodeElement, otherNodeElement);
	}

	
	private void eliminateCrossings(NodeElement nodeElement, NodeElement otherNodeElement) {
		if(providers(otherNodeElement).contains(nodeElement))
			return;
		
		changeIfNecessary1(nodeElement, otherNodeElement);
		changeIfNecessary2(nodeElement, otherNodeElement);
	}


	private void changeIfNecessary1(NodeElement nodeElement, NodeElement otherNodeElement) {
		if(nodeElement.x() < otherNodeElement.x())
			for(NodeElement ne : providers(nodeElement))
				for(NodeElement one : providers(otherNodeElement))
					if(ne.x() > one.x())
						change(ne, one);
	}


	private void changeIfNecessary2(NodeElement nodeElement, NodeElement otherNodeElement) {
		if(nodeElement.x() > otherNodeElement.x())
			for(NodeElement ne : providers(nodeElement))
				for(NodeElement one : providers(otherNodeElement))
					if(ne.x() < one.x())
						change(ne, one);
	}

	
	private void change(NodeElement nodeElement, NodeElement otherNodeElement) {
		int tmp = nodeElement.x();
		nodeElement.x(otherNodeElement.x());
		otherNodeElement.x(tmp);
	}
}