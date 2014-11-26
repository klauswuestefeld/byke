package byke.views.cache;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

public class EdgeTest {

	private String _xml = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<edge target=\"target\" source=\"source\"/>\n";


	@Test
	public void unmarshall() throws Exception {
		EdgeFigure edge = GEXFHelper.unmarshall(EdgeFigure.class, _xml);
    
    assertEquals("source", edge.source());
    assertEquals("target", edge.target());
	}
	
	
	@Test
	public void marshall() throws Exception {
		NodeFigure source = new NodeFigure();
		source.id("source");
		NodeFigure target = new NodeFigure();
		target.id("target");
		
		EdgeFigure edge = new EdgeFigure();
		edge.source("source");
		edge.target("target");
		
		assertEquals(_xml, GEXFHelper.marshall(EdgeFigure.class, edge, new StringWriter()).toString());
	}
}
