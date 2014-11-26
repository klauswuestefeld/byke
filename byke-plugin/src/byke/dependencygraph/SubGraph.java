package byke.dependencygraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import byke.views.cache.NodeFigure;

public class SubGraph extends NodeFigure {

	public SubGraph(Collection<NodeFigure> nodes) {
		_name = _id = nameFor(nodes);
		
		addSubGraph(nodes);
	}

	private static String nameFor(Collection<NodeFigure> nodes) {
		List<String> names = new ArrayList<String>(nodes.size());
		for(NodeFigure node: nodes)
			names.add(node.name());
		Collections.sort(names);
		
		String ret = "";
		for(String n: names)
			ret += ret.isEmpty() ? n : ", " + n;
		return ret;
	}

}
