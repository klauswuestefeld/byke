package byke.views.cache;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

public class GEXFFileTest {

	private String _xml = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
			+ "<ns2:gexf xmlns:ns2=\"http://www.gexf.net/1.2draft\">\n"
			+ "    <graph defaultedgetype=\"directed\">\n"
			+ "        <edges>\n"
			+ "            <edge source=\"source\" target=\"target\"/>\n"
			+ "        </edges>\n"
			+ "        <nodes>\n"
			+ "            <node id=\"source\" label=\"source\"/>\n"
			+ "            <node id=\"target\" label=\"target\"/>\n"
			+ "        </nodes>\n"
			+ "    </graph>\n"
			+ "</ns2:gexf>\n";


	@Test
	public void marshall() throws Exception {
		GEXFFile gexf = new GEXFFile();
		gexf.graph(GEXFHelper.newGraph());
		
		assertEquals(_xml, GEXFHelper.marshall(GEXFFile.class, gexf, new StringWriter()).toString());
	}


	@Test
	public void unmarshall() throws Exception {
		GEXFFile gexf = GEXFHelper.unmarshall(GEXFFile.class, _xml);
    GEXFHelper.verifyGraph(gexf.graph());
	}

}
