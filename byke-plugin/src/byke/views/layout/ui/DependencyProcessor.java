package byke.views.layout.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import byke.dependencygraph.Node;
import byke.dependencygraph.SubGraph;

public class DependencyProcessor {

	public <T> Collection<SubGraph<T>> calculateSubGraphs(Collection<Node<T>> graph) {
		Collection<SubGraph<T>> newGraph = new ArrayList<SubGraph<T>>();
		Map<Node<T>, SubGraph<T>> subGraphsByNode = new HashMap<Node<T>, SubGraph<T>>();
		
		for(Node<T> node : graph) {
			if(subGraphsByNode.keySet().contains(node)) continue;
			
			SubGraph<T> subGraph = subGraphFor(node);
			
			for(Node<T> subNode: subGraph.payload()){
				subGraphsByNode.put(subNode, subGraph);
			}
			
			newGraph.add(subGraph);
		}
		
		for(SubGraph<T> sub: newGraph){
			for(Node<T> subNode: sub.payload()){
				for(Node<T> provider: subNode.providers())
					sub.addProvider(subGraphsByNode.get(provider));
			}
		}
		
		return newGraph;
	}

	private <T> SubGraph<T> subGraphFor(Node<T> node) {
		Collection<Node<T>> temp = new ArrayList<Node<T>>();
		
		temp.add(node);
		for(Node<T> provider : node.providers()) {
			if(node.equals(provider))
				continue;
			if(formsCycle(node, provider))
				temp.add(provider);
		}
		SubGraph<T> subGraph = new SubGraph<T>(temp);
		return subGraph;
	}
	
	private <T> boolean formsCycle(Node<?> node, Node<?> provider) {
		return provider.dependsOn(node);
	}
}
