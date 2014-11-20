package ui;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

import byke.dependencygraph.Node;
import byke.dependencygraph.SubGraph;
import byke.views.layout.ui.DependencyProcessor;
import byke.views.layout.ui.NonMovableGraph;


public class NonMovableGraphTest {

	private NonMovableGraph<String> _graph;


	@Test 
	public void simpleGraph() {
		newGraph(createSimpleCyclicDependencyGraph());

		Assert.assertEquals(2, _graph.getNodes().size());
		Assert.assertEquals(1, _graph.getConnections().size());

		hasNode("n 1, n 2");
		hasNode("n 3");
		
		hasConnection("n 1, n 2", "n 3");
	}
	
	@Test
	public void dependencyProcessing() {
		Collection<SubGraph<String>> processedGraph = new DependencyProcessor().calculateSubGraphs(createSimpleCyclicDependencyGraph());
	
		Assert.assertEquals(2, processedGraph.size());
		SubGraph cycle = getNode("n 1, n 2", processedGraph);
		SubGraph other = getNode("n 3", processedGraph);
		Assert.assertTrue(cycle.providers().contains(other));
	}

	private SubGraph<String> getNode(String name, Collection<SubGraph<String>> graph) {
		for (SubGraph<String> node : graph)
			if (node.name().equals(name)) 
				return node;

		Assert.fail(String.format("Node '%s' not found", name));
		return null;
	}

	public static Collection<Node<String>> createSimpleCyclicDependencyGraph() {
		Node<String> n1 = new Node<String>("n 1");
		Node<String> n2 = new Node<String>("n 2");
		Node<String> n3 = new Node<String>("n 3");
		n1.addProvider(n2);
		n2.addProvider(n1);
		n2.addProvider(n3);
		return Arrays.asList(n1, n2, n3);
	}

	private void newGraph(Collection<Node<String>> nodes) {
		_graph = new NonMovableGraph<String>(new Shell(new Display()), nodes);
	}

	private void hasNode(String name) {
		for (GraphNode node : _graph.getNodes())
			if (node.getText().equals(name)) 
				return;

		Assert.fail(String.format("Node '%s' not found", name));
	}

	private void hasConnection(String from, String to) {
		for (GraphConnection connection : _graph.getConnections())
			if (connection.getSource().getText().equals(from) && connection.getDestination().getText().equals(to)) 
				return;

		Assert.fail(String.format("Connection '%s' -> '%s' not found", from, to));
	}

}
