package byke.views.cache;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

public class GraphTest {

	private String _xml = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<graph defaultedgetype=\"directed\">\n"
			+ "    <nodes>\n"
			+ "        <node label=\"source\" id=\"source\"/>\n"
			+ "        <node label=\"target\" id=\"target\"/>\n"
			+ "    </nodes>\n"
			+ "    <edges>\n"
			+ "        <edge target=\"target\" source=\"source\"/>\n"
			+ "    </edges>\n"
			+ "</graph>\n";


	@Test
	public void unmarshall() throws Exception {
		GraphFigure graph = GEXFHelper.unmarshall(GraphFigure.class, _xml);
    GEXFHelper.verifyGraph(graph);
	}
	
	
	@Test
	public void marshall() throws Exception {
		GraphFigure graph = GEXFHelper.newGraph();
		assertEquals(_xml, GEXFHelper.marshall(GraphFigure.class, graph, new StringWriter()).toString());
	}
	
}
