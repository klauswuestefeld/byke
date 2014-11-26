package byke.views.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "node")
public class NodeFigure {

	@XmlAttribute(name = "id")
	protected String _id;
	@XmlAttribute(name = "label")
	protected String _name;
	
	private Set<NodeFigure> _providers = new HashSet<NodeFigure>();
	private Set<NodeFigure> _subGraph = new HashSet<NodeFigure>();

	
	public NodeFigure() {
	}

	public NodeFigure(String name) {
		_id = name;
		_name = name;
	}


	public String id() {
		return _id;
	}
	
	
	public void id(String id) {
		_id = id;
	}
	
	
	public String name() {
		return _name;
	}
	
	
	public void name(String name) {
		_name = name;
	}
	
	
	public Set<NodeFigure> cycle() {
		HashSet<NodeFigure> ret = new HashSet<NodeFigure>();
		accumulateCycle(ret);
		return ret;
	}
	
	
	public Collection<NodeFigure> providers() {
		return _providers;
	}
	
	
	private void accumulateCycle(Set<NodeFigure> cycle) {
		if (cycle.contains(this)) return;
		cycle.add(this);

		for (NodeFigure neighbor : _providers)
			if (neighbor.dependsOn(this))
				neighbor.accumulateCycle(cycle);
	}
	
	
	private boolean dependsOn(NodeFigure node) {
		if (this == node) return false;
		Set<NodeFigure> visited = new HashSet<NodeFigure>();
		return seekProvider(node, visited);
	}
	
	
	private boolean seekProvider(NodeFigure target, Set<NodeFigure> visited) {
		if (this == target) return true;

		if (visited.contains(this)) return false;
		visited.add(this);

		for (NodeFigure neighbor : _providers)
			if (neighbor.seekProvider(target, visited)) return true;

		return false;
	}


	public void addProvider(NodeFigure node) {
		if(node == this) return;
		if(_subGraph.contains(node)) return;
		_providers.add(node);
	}


	public void addSubGraph(Collection<NodeFigure> nodes) {
		_subGraph.addAll(nodes);
		
		for(NodeFigure node : nodes)
			for(NodeFigure provider : node.providers())
				addProvider(provider);
	}
	
	
	public Set<NodeFigure> subGraph() {
		return _subGraph;
	}
	
	
	@Override
	public String toString() {
		return "<" + name() + ">";
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return name().equals(((NodeFigure)obj).name());
	}
	
	
	@Override
	public int hashCode() {
		return name().hashCode();
	}
}
