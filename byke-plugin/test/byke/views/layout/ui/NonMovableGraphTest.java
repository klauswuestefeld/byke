package byke.views.layout.ui;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

import byke.dependencygraph.SubGraph;
import byke.views.cache.NodeFigure;


public class NonMovableGraphTest extends Assert {

	private NonMovableGraph _graph;


	@Test 
	public void simpleGraph() {
		newGraph(createSimpleCyclicDependencyGraph());

		Assert.assertEquals("Actual " + _graph.getNodes(), 2, _graph.getNodes().size());
		Assert.assertEquals(1, _graph.getConnections().size());

		hasNode("n 1, n 2");
		hasNode("n 3");
		
		hasConnection("n 1, n 2", "n 3");
	}

	
	@Test
	public void dependencyProcessing() {
		Collection<SubGraph> processedGraph = new DependencyProcessor().clusterCycles(createSimpleCyclicDependencyGraph());
	
		Assert.assertEquals(2, processedGraph.size());
		SubGraph cycle = getNode("n 1, n 2", processedGraph);
		SubGraph other = getNode("n 3", processedGraph);
		Assert.assertTrue(cycle.providers().contains(other));
	}

	
	//	a -> b
	//	b -> a
	//	b -> c
	//	c -> b
	@Test
	public void dependencyProcessing2() {
		NodeFigure a = new NodeFigure("a");
		NodeFigure b = new NodeFigure("b");
		NodeFigure c = new NodeFigure("c");
		a.addProvider(b);
		b.addProvider(a);
		b.addProvider(c);
		c.addProvider(b);

		Collection<SubGraph> processedGraph = new DependencyProcessor().clusterCycles(Arrays.asList(a, b, c));
	
		Assert.assertEquals("Actual: " + processedGraph, 1, processedGraph.size());
		assertNotNull(getNode("a, b, c", processedGraph));
	}


	private SubGraph getNode(String name, Collection<SubGraph> graph) {
		for (SubGraph node : graph)
			if (node.name().equals(name)) 
				return node;

		Assert.fail(String.format("Node '%s' not found", name));
		return null;
	}
	

	public static Collection<NodeFigure> createSimpleCyclicDependencyGraph() {
		NodeFigure n1 = new NodeFigure("n 1");
		NodeFigure n2 = new NodeFigure("n 2");
		NodeFigure n3 = new NodeFigure("n 3");
		n1.addProvider(n2);
		n2.addProvider(n1);
		n2.addProvider(n3);
		return Arrays.asList(n1, n2, n3);
	}

	private void newGraph(Collection<NodeFigure> nodes) {
		_graph = new NonMovableGraph(new Shell(Display.findDisplay(Thread.currentThread())), nodes);
	}

	private void hasNode(String name) {
		for (GraphNode node : _graph.getNodes())
			if (node.getText().equals(name)) 
				return;

		Assert.fail("Node " + name + " not found in " + _graph.getNodes().toString());
	}

	private void hasConnection(String from, String to) {
		for (GraphConnection connection : _graph.getConnections())
			if (connection.getSource().getText().equals(from) && connection.getDestination().getText().equals(to)) 
				return;

		Assert.fail(String.format("Connection '%s' -> '%s' not found", from, to));
	}

}
