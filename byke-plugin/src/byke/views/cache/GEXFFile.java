package byke.views.cache;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "gexf", namespace="http://www.gexf.net/1.2draft")
public class GEXFFile {

	@XmlElement(name = "graph")
	private GraphFigure _graph;

	
	public GraphFigure graph() {
		return _graph;
	}

	
	public void graph(GraphFigure graph) {
		_graph = graph;
	}
}
