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
import byke.views.layout.algorithm.random.RandomAverage;
import byke.views.layout.ui.GraphCanvas;


public class StandAlone {

	private final static Random RANDOM = new Random(0);

	
	public static void main(String args[]) {
		new StandAlone();
	}


	private final Collection<Node<String>> _graph = graph();

	private final Display _display = new Display(); // Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants will throw a NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byke");
		shell.setSize(300, 300);

		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		
		GraphCanvas<String> canvas = new GraphCanvas<String>(shell, _graph, new CartesianLayout(), new GraphCanvas.Listener<String>() {
			public void nodeSelected(Node<String> node) {
				System.out.println("Node:" + node);
			}
		});
		
		
		LayoutAlgorithm<String> algorithm = new RandomAverage<String>(_graph, null, canvas);
		//LayoutAlgorithm<String> algorithm = new InertialRelaxer<String>(_graph, null, canvas);
		//LayoutAlgorithm<String> algorithm = new AlgorithmCombination<String>(_graph, null, canvas);

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

	private void work(LayoutAlgorithm<String> algorithm, GraphCanvas<String> canvas) {
		// try {
		// Thread.sleep(50);
		// } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		//algorithm.improveLayoutStep();
		boolean improved = algorithm.improveLayoutForAWhile();
		if (improved)
			canvas.useLayout(algorithm.layoutMemento());

		canvas.animationStep(1);
	}

	private Collection<Node<String>> graph() {
		String[] names = new String[3]; //FIXME Start with "Node 9" and test layout with 2 nodes only. The line should be vertical but is not.
		for (int i = 0; i < names.length; i++)
			names[i] = "Node " + i;

		return createGraph(names);
		
		// return Node.createGraph(new String[]{"1 234", "12 34", "123 4"});

		// private Collection<Node<String>> graph() {
		// Collection<Node<String>> result = new ArrayList<Node<String>>();
		// Node<String> nodeA = new Node<String>("A");
		// Node<String> nodeB = new Node<String>("B");
		// Node<String> nodeC = new Node<String>("C");
		// nodeA.addProvider(nodeB);
		// nodeB.addProvider(nodeA);
		// nodeB.addProvider(nodeC);
		// result.add(nodeA);
		// result.add(nodeB);
		// result.add(nodeC);
		// return result;
}

	
	private static Collection<Node<String>> createGraph(String[] names) {
		List<Node<String>> result = new ArrayList<Node<String>>();
		for (String element : names)
			result.add(new Node<String>(element, JavaType.PACKAGE));
		produceRandomDependencies(result);
		return result;
	}

	
	private static <PayloadType> void produceRandomDependencies(List<Node<PayloadType>> graph) {
		int dependenciesToCreate = (int)(graph.size() * 1.1);
	
		while (dependenciesToCreate-- > 0) {
			Node<PayloadType> node1 = drawOneFrom(graph);
			Node<PayloadType> node2 = drawOneFrom(graph);
			if (node1 == node2) continue;
	
			node1.addProvider(node2);
		}
	}

	
	private static <PayloadType> Node<PayloadType> drawOneFrom(List<Node<PayloadType>> hat) {
		return hat.get(RANDOM.nextInt(hat.size()));
	}

}