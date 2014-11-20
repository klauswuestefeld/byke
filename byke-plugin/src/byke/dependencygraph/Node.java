//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byke.dependencygraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.IBinding;

import byke.JavaType;


public class Node<PayloadType> {

	public Node(String name) {
		this(name, JavaType.CLASS);
	}

	public Node(String name, JavaType kind) {
		_name = name;
		_kind = kind;
	}


	private final String _name;

	private final JavaType _kind;

	private final Set<Node<PayloadType>> _providers = new HashSet<Node<PayloadType>>();

	private PayloadType _payload;


	public String name() {
		return _name;
	}

	public JavaType kind() {
		return _kind;
	}

	public Collection<Node<PayloadType>> providers() {
		return _providers;
	}

	public void addProvider(Node<PayloadType> provider) {
		if (provider == this) return;
		_providers.add(provider);
	}
	
	public void addProviders(Collection<Node<PayloadType>> providers) {
		for (Node<PayloadType> node : providers) {
			addProvider(node);
		}
	}
	
	public void removeProvider(Node<IBinding> provider) {
		_providers.remove(provider);
	}

	public boolean dependsDirectlyOn(Node<?> other) {
		return _providers.contains(other);
	}

	public void payload(PayloadType payload) {
		_payload = payload;
	}

	public PayloadType payload() {
		return _payload;
	}

	private boolean seekProvider(Node<?> target, Set<Node<?>> visited) {
		if (this == target) return true;

		if (visited.contains(this)) return false;
		visited.add(this);

		for (Node<?> neighbor : _providers)
			if (neighbor.seekProvider(target, visited)) return true;

		return false;
	}

	public Set<Node<PayloadType>> cycle() {
		HashSet<Node<PayloadType>> ret = new HashSet<Node<PayloadType>>();
		accumulateCycle(ret);
		return ret;
	}

	private void accumulateCycle(Set<Node<PayloadType>> cycle) {
		if (cycle.contains(this)) return;
		cycle.add(this);

		for (Node<PayloadType> neighbor : _providers)
			if (neighbor.dependsOn(this))
				neighbor.accumulateCycle(cycle);
	}

	public boolean dependsOn(Node<?> node) {
		if (this == node) return false;
		Set<Node<?>> visited = new HashSet<Node<?>>();
		return this.seekProvider(node, visited);
	}

	@Override
	public int hashCode() {
		return this._name.hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof Node<?>)) return false;
		Node<?> n = (Node<?>)arg0;
		return _kind.equals(n._kind) && _name.equals(n._name);
	}

	@Override
	public String toString() {
		return "<" + name() + ">";
	}

}
