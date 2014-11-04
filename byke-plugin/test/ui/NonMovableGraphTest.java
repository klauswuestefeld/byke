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
import byke.views.layout.ui.NonMovableGraph;


public class NonMovableGraphTest {

	private NonMovableGraph<String> _graph;


	@Test
	public void simpleGraph() {
		newGraph(createSimpleCyclicDependencyGraph());

		Assert.assertEquals(4, _graph.getNodes().size());
		Assert.assertEquals(6, _graph.getConnections().size());

		hasNode("n 1");
		hasNode("n 2");
		hasNode("n 3");
		hasNode("n 1, n 2");

		hasConnection("n 1, n 2", "n 1");
		hasConnection("n 1, n 2", "n 2");
		hasConnection("n 1, n 2", "n 3");
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
