import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import byke.JavaType;
import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.algorithm.LayoutAlgorithm;
import byke.views.layout.algorithm.LayoutAlgorithmBase;
import byke.views.layout.ui.GraphCanvas;


public class StandAlone {

	private static final int NUMBER_OF_NODES = 5;
	private static final double DENSITY_OF_DEPENDENCIES = 1.8;

	private final static Random RANDOM = new Random(0);

	
	public static void main(String args[]) {
		new StandAlone();
	}


	private final Collection<Node<String>> _graph = graph();

	private final Display _display = new Display(); // Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants will throw a NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byke");
		shell.setSize(600, 600);

		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		
		GraphCanvas<String> canvas = new GraphCanvas<String>(shell, _graph, new CartesianLayout(), new GraphCanvas.Listener<String>() {
			@Override public void nodeSelected(Node<String> node) {
				System.out.println("Node:" + node);
			}
		});
		
		
		LayoutAlgorithm algorithm =
			new LayoutAlgorithmBase<String>(_graph, null, canvas);

		shell.open();
		shell.layout();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				work(algorithm, canvas);
			//	_display.sleep();
			}
		}
	}

	
	private void work(LayoutAlgorithm algorithm, GraphCanvas<String> canvas) {
		boolean improved = algorithm.improveLayoutStep();
//		if (improved)
			canvas.useLayout(algorithm.layoutMemento());

		canvas.animationStep(1);
	}

	
	private Collection<Node<String>> graph() {
		
		String[] names = new String[NUMBER_OF_NODES];
		for (int i = 0; i < names.length; i++)
			names[i] = "Node " + i;
		return createGraph(names);

		
//		Collection<Node<String>> result = new ArrayList<Node<String>>();
//		Node<String> n1 = new Node<String>("1");
//		Node<String> n2 = new Node<String>("2");
//		Node<String> n3 = new Node<String>("3");
//		Node<String> n4 = new Node<String>("4");
//		Node<String> n5 = new Node<String>("5");
//		Node<String> n6 = new Node<String>("6");
//		Node<String> n7 = new Node<String>("7");
//		n1.addProvider(n2);
//		n2.addProvider(n3);
//		n3.addProvider(n4);
//		n4.addProvider(n5);
//		n5.addProvider(n6);
//		n6.addProvider(n7);
//		n1.addProvider(n3);
//		n1.addProvider(n4);
//		n1.addProvider(n5);
//		n1.addProvider(n6);
//		n1.addProvider(n7);
//		result.add(n1);
//		result.add(n2);
//		result.add(n3);
//		result.add(n4);
//		result.add(n5);
//		result.add(n6);
//		return result;

		
//		return Node.createGraph(new String[]{"1 234", "12 34", "123 4"});

		
//		Collection<Node<String>> result = new ArrayList<Node<String>>();
//		Node<String> nodeA = new Node<String>("main");
//		Node<String> nodeB = new Node<String>("banana");
//		Node<String> nodeC = new Node<String>("abacaxi");
//		nodeA.addProvider(nodeB);
//		nodeB.addProvider(nodeC);
//		nodeC.addProvider(nodeA);
//		result.add(nodeA);
//		result.add(nodeB);
//		result.add(nodeC);
//		return result;

		
//		 Collection<Node<String>> result = new ArrayList<Node<String>>();
//		 Node<String> nodeA = new Node<String>("Agua");
//		 Node<String> nodeB = new Node<String>("Luz");
//		 Node<String> nodeC = new Node<String>("Gás");
//		 Node<String> node1 = new Node<String>("Casa 1");
//		 Node<String> node2 = new Node<String>("Casa 2");
//		 Node<String> node3 = new Node<String>("Casa 3");
//		 nodeA.addProvider(node1);
//		 nodeB.addProvider(node1);
//		 nodeC.addProvider(node1);
//		 nodeA.addProvider(node2);
//		 nodeB.addProvider(node2);
//		 nodeC.addProvider(node2);
//		 nodeA.addProvider(node3);
//		 nodeB.addProvider(node3);
//		 nodeC.addProvider(node3);
//		 result.add(nodeA);
//		 result.add(nodeB);
//		 result.add(nodeC);
//		 result.add(node1);
//		 result.add(node2);
//		 result.add(node3);
//		 return result;
}

	
	private static Collection<Node<String>> createGraph(String[] names) {
		List<Node<String>> result = new ArrayList<Node<String>>();
		for (String element : names)
			result.add(new Node<String>(element, JavaType.PACKAGE));
		produceRandomDependencies(result);
		return result;
	}

	
	private static <T> void produceRandomDependencies(List<Node<T>> graph) {
		int dependenciesToCreate = (int)(graph.size() * DENSITY_OF_DEPENDENCIES);
	
		while (dependenciesToCreate-- > 0) {
			Node<T> node1 = drawOneFrom(graph);
			Node<T> node2 = drawOneFrom(graph);
			if (node1 == node2) continue;
	
			node1.addProvider(node2);
		}
	}

	
	private static <T> Node<T> drawOneFrom(List<Node<T>> hat) {
		return hat.get(RANDOM.nextInt(hat.size()));
	}

}