package byke.dependencygraph;

import java.util.Collection;

import byke.JavaType;

public class SubGraph <T> extends Node<Collection<Node<T>>> {

	public SubGraph(Collection<Node<T>> nodes) {
		super(nameFor(nodes), JavaType.SUBGRAPH);
		payload(nodes);
	}

	private static <T> String nameFor(Collection<Node<T>> nodes) {
		String name = "";
		for(Node<?> n: nodes){
			if(!name.isEmpty()) name += ", ";
			name += n.name();
		}
		return name;
	}

}
