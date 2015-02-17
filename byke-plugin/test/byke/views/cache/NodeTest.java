package byke.views.cache;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

public class NodeTest {

	private String _xml = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<node id=\"id test\" label=\"label test\"/>\n";


	@Test
	public void unmarshall() throws Exception {
		NodeFigure node = GEXFHelper.unmarshall(NodeFigure.class, _xml);
    
    assertEquals("id test", node.id());
    assertEquals("label test", node.name());
	}
	
	
	@Test
	public void marshall() throws Exception {
		NodeFigure node = new NodeFigure();
		node.id("id test");
		node.name("label test");

		assertEquals(_xml, GEXFHelper.marshall(NodeFigure.class, node, new StringWriter()).toString());
	}
	
}
