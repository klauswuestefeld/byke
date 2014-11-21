package byke.views.layout.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import byke.dependencygraph.Node;
import byke.dependencygraph.SubGraph;

public class DependencyProcessor {

	public <T> Collection<SubGraph<T>> clusterCycles(Collection<Node<T>> graph) {
		Map<Node<T>, SubGraph<T>> cyclesByNode = new HashMap<Node<T>, SubGraph<T>>();
		
		for (Node<T> node : graph) {
			if (cyclesByNode.containsKey(node))
				continue;
			
			Set<Node<T>> cycle = node.cycle();
			SubGraph<T> subGraph = new SubGraph<T>(cycle);
			for (Node<T> visited : cycle)
				cyclesByNode.put(visited, subGraph);
		}
		
		HashSet<SubGraph<T>> ret = new HashSet<SubGraph<T>>(cyclesByNode.values());
		for(SubGraph<T> sub: ret)
			for(Node<T> subNode: sub.payload())
				for(Node<T> provider: subNode.providers())
					sub.addProvider(cyclesByNode.get(provider));
		
		return ret;
	}

}
