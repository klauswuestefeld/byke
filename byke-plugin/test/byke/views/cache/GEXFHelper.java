package byke.views.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class GEXFHelper {

	public static <T> T unmarshall(Class<T> clazz, String xml) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (T) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
	}
	
	public static <T extends Writer> T marshall(Class<?> clazz, Object in, T out) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(in, out);
		
		return out;
	}
	
	static Object node(List<NodeFigure> nodes, String id) {
		for(NodeFigure node : nodes)
			if(node.id().equals(id))
				return node;
		return null;
	}
	
	static GraphFigure newGraph() {
		NodeFigure source = new NodeFigure();
		source.id("source");
		source.name("source");
		NodeFigure target = new NodeFigure();
		target.id("target");
		target.name("target");
		
		EdgeFigure edge = new EdgeFigure();
		edge.source("source");
		edge.target("target");
		
		GraphFigure graph = new GraphFigure();
		graph.defaultEdgeType("directed");
		graph.nodes(Arrays.asList(source, target));
		graph.edges(Arrays.asList(edge));
		return graph;
	}
	
	static void verifyGraph(GraphFigure graph) {
		assertEquals("directed", graph.defaultEdgeType());
    assertEquals(2, graph.nodes().size());
    assertNotNull(node(graph.nodes(), "source"));
    assertNotNull(node(graph.nodes(), "target"));
    assertEquals(1, graph.edges().size());
    assertEquals("source", graph.edges().get(0).source());
    assertEquals("target", graph.edges().get(0).target());
	}

}
