//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.dependencygraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Node<PayloadType> {
    
    private final static Random RANDOM = new Random();

    public static <PayloadType> Collection<Node<PayloadType> > createGraph(String[] names) {
		
		List<Node<PayloadType> > result = new ArrayList<Node<PayloadType>>(); 

		for (int i = 0; i < names.length; i++) {
            result.add(new Node<PayloadType>(names[i], "package"));
        }

        produceRandomDependencies(result);
        
        return result;
    }

    private static <PayloadType> void produceRandomDependencies(List<Node<PayloadType>> graph) {
        int dependenciesToCreate = (int)(graph.size() * 1.1);
        
    	while (dependenciesToCreate-- > 0) {
    	    Node node1 = drawOneFrom(graph);
    	    Node node2 = drawOneFrom(graph);
    	    if (node1 == node2) continue;
    	    
    	    node1.addProvider(node2);
    	}
    }

    public static <PayloadType> Node<PayloadType> drawOneFrom(List<Node<PayloadType>> hat) {
        return hat.get(RANDOM.nextInt(hat.size()));
    }

    public Node(String name) {
        this(name, "class");
    }

    public Node(String name, String kind) {
        _name = name;
        _kind = kind;
    }

    private final String _name;
    private final String _kind;
    private final Set<Node> _providers = new HashSet<Node>();
	private PayloadType _payload;

    public String name() {
        return _name;
    }

    public String kind() {
        return _kind;
    }

    public Iterator<Node> providers() {
        return _providers.iterator();
    }

    public void addProvider(Node provider) {
		if (provider == this) return;
		_providers.add(provider);
    }

    public boolean dependsDirectlyOn(Node other) {
        return _providers.contains(other);
    }

	public void payload(PayloadType payload) {
		_payload = payload;
	}
	
	public PayloadType payload() {
		return _payload;
	}

	private boolean seekProvider(Node target, Set<Node> visited) {
		if (this == target) return true;
		
		if (visited.contains(this)) return false;
		visited.add(this);
		
		for (Node<?> neighbor : _providers)
		  if (neighbor.seekProvider(target, visited)) return true;

		return false;
	}

	public boolean dependsOn(Node node) {
		if (this == node) return false;
		Set<Node> visited = new HashSet<Node>();
		return this.seekProvider(node, visited);
	}

}
