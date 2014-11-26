package byke.views.cache;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "graph")
public class GraphFigure {

	@XmlAttribute(name = "defaultedgetype")
	private String _defaultEdgeType;
	
	@XmlElementWrapper(name = "nodes")
  @XmlElement(name = "node")
	private List<NodeFigure> _nodes;
	@XmlElementWrapper(name = "edges")
	@XmlElement(name = "edge")
	private List<EdgeFigure> _edges;

	
	public List<NodeFigure> nodes() {
		return _nodes;
	}
	
	
	public void nodes(List<NodeFigure> nodes) {
		_nodes = nodes;
	}
	
	
	public List<EdgeFigure> edges() {
		return _edges;
	}
	
	
	public void edges(List<EdgeFigure> edges) {
		_edges = edges;
	}
	
	public String defaultEdgeType() {
		return _defaultEdgeType;
	}


	public void defaultEdgeType(String defaultEdgeType) {
		_defaultEdgeType = defaultEdgeType;
	}
}
