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

import byecycle.JavaType;

public class Node<PayloadType extends Object> {

    private final static Random RANDOM = new Random();

    public static <PayloadType> Collection<Node<PayloadType>> createGraph(
            String[] names) {

        List<Node<PayloadType>> result = new ArrayList<Node<PayloadType>>();

        for (String element : names) {
            result.add(new Node<PayloadType>(element, JavaType.PACKAGE));
        }

        produceRandomDependencies(result);

        return result;
    }

    private static <PayloadType> void produceRandomDependencies(
            List<Node<PayloadType>> graph) {
        int dependenciesToCreate = (int) (graph.size() * 1.1);

        while (dependenciesToCreate-- > 0) {
            Node node1 = drawOneFrom(graph);
            Node node2 = drawOneFrom(graph);
            if (node1 == node2)
                continue;

            node1.addProvider(node2);
        }
    }

    public static <PayloadType> Node<PayloadType> drawOneFrom(
            List<Node<PayloadType>> hat) {
        return hat.get(RANDOM.nextInt(hat.size()));
    }

    public Node(String name) {
        this(name, JavaType.CLASS);
    }

    @Deprecated
    public Node(String name, String kind) {
        _name = name;
        if(kind.equals("package")) {
            _kind = JavaType.PACKAGE;
        }else if(kind.equals("class")) {
            _kind = JavaType.CLASS;
        }else if(kind.equals("interface")) {
            _kind = JavaType.INTERFACE;
        }else if(kind.equals("enum")) {
            _kind = JavaType.ENUM;
        }else if(kind.equals("annotation")) {
            _kind = JavaType.ANNOTATION;
        }else {
            throw new IllegalArgumentException(kind);
        }
    }

    public Node(String name, JavaType kind) {
        _name = name;
        _kind = kind;
    }

    private final String _name;

    private final JavaType _kind;

    private final Set<Node> _providers = new HashSet<Node>();

    private PayloadType _payload;

    public String name() {
        return _name;
    }

    public JavaType kind2() {
        return _kind;
    }

    @Deprecated
    public String kind() {
        return _kind.toString().toLowerCase();
    }

    public Iterator<Node> providers() {
        return _providers.iterator();
    }

    public void addProvider(Node provider) {
        if (provider == this)
            return;
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
        if (this == target)
            return true;

        if (visited.contains(this))
            return false;
        visited.add(this);

        for (Node<?> neighbor : _providers)
            if (neighbor.seekProvider(target, visited))
                return true;

        return false;
    }

    public boolean dependsOn(Node node) {
        if (this == node)
            return false;
        Set<Node> visited = new HashSet<Node>();
        return this.seekProvider(node, visited);
    }

    @Override
    public int hashCode() {
        return this._kind.hashCode() * 29 ^ this._name.hashCode();
    }

    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof Node))
            return false;
        Node n = (Node) arg0;
        return _kind.equals(n._kind) && _name.equals(n._kind);
    }

}
