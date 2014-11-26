package byke.views.layout.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import byke.dependencygraph.SubGraph;
import byke.views.cache.NodeFigure;

public class DependencyProcessor {

	public Collection<SubGraph> clusterCycles(Collection<NodeFigure> graph) {
		Map<NodeFigure, SubGraph> cyclesByNode = new HashMap<NodeFigure, SubGraph>();
		
		for (NodeFigure node : graph) {
			if (cyclesByNode.containsKey(node))
				continue;
			
			Set<NodeFigure> cycle = node.cycle();
			SubGraph subGraph = new SubGraph(cycle);
			for (NodeFigure visited : cycle)
				cyclesByNode.put(visited, subGraph);
		}
		
		return new HashSet<SubGraph>(cyclesByNode.values());
	}

}
