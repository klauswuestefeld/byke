package byke.views.layout.ui;

import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import byke.views.cache.NodeFigure;

public class MnemonicColorsTest {

	private static Shell PARENT;
	private NonMovableGraph _graph;
	
	
	@BeforeClass
	public static void setUp() {
		PARENT = new Shell(new Display());
	}

	
	private void newGraph(Collection<NodeFigure> nodes) {
		_graph = new NonMovableGraph(PARENT, nodes);
	}

	
	private GraphNode getNode(String name) {
		for(GraphNode node : _graph.getNodes())
			if(node.getText().equals(name))
				return node;
		return null;
	}
	
	
	/*
	 *    a
	 *   /|\
	 *  b c d
	 */
	@Test 
	public void allNodesHaveDifferentColors() {
		newGraph(allNodesHaveDifferentColorsGraph());

		Set<Color> colors = new HashSet<Color>();
		colors.add(getNode("a").getBackgroundColor());
		colors.add(getNode("b").getBackgroundColor());
		colors.add(getNode("c").getBackgroundColor());
		colors.add(getNode("d").getBackgroundColor());
		
		Assert.assertThat(colors.size(), equalTo(4));
	}

	/*
	 *    a, b
	 */
	@Test 
	public void cycleIsRed() {
		newGraph(cyclesIsRedGraph());
		
		Assert.assertThat(getNode("a, b").getBackgroundColor(), equalTo(byke.views.layout.ui.BykeColors.RED));
	}

	
	public static Collection<NodeFigure> allNodesHaveDifferentColorsGraph() {
		NodeFigure a = new NodeFigure("a");
		NodeFigure b = new NodeFigure("b");
		NodeFigure c = new NodeFigure("c");
		NodeFigure d = new NodeFigure("d");
		
		a.addProvider(b);
		a.addProvider(c);
		a.addProvider(d);
		
		return Arrays.asList(a, b, c, d);
	}

	
	public static Collection<NodeFigure> cyclesIsRedGraph() {
		NodeFigure a = new NodeFigure("a");
		NodeFigure b = new NodeFigure("b");
		
		a.addProvider(b);
		b.addProvider(a);
		
		return Arrays.asList(a, b);
	}
	
}
