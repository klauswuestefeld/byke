package byke.dependencygraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import byke.JavaType;

public class SubGraph <T> extends Node<Collection<Node<T>>> {

	public SubGraph(Collection<Node<T>> nodes) {
		super(nameFor(nodes), JavaType.SUBGRAPH);
		payload(nodes);
	}

	private static <T> String nameFor(Collection<Node<T>> nodes) {
		List<String> names = new ArrayList<String>(nodes.size());
		for(Node<?> node: nodes)
			names.add(node.name());
		Collections.sort(names);
		
		String ret = "";
		for(String n: names)
			ret += ret.isEmpty() ? n : ", " + n;
		return ret;
	}

}
