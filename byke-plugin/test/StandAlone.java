import java.util.Collection;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.algorithm.LayoutAlgorithm;
import byke.views.layout.algorithm.relaxer.InertialRelaxer;
import byke.views.layout.ui.GraphCanvas;


public class StandAlone {

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
		
		
		//LayoutAlgorithm<String> algorithm = new RandomAverage<String>(_graph, null, canvas);
		LayoutAlgorithm<String> algorithm = new InertialRelaxer<String>(_graph, null, canvas);
		//LayoutAlgorithm<String> algorithm = new AlgorithmCombination<String>(_graph, null, canvas);

		shell.open();
		shell.layout();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				// try {
				// Thread.sleep(50);
				// } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				algorithm.improveLayoutStep();
				//boolean improved = algorithm.improveLayoutForAWhile();
				//if (improved)
					canvas.useLayout(algorithm.layoutMemento());

				canvas.animationStep(1000);

			//	_display.sleep();
			}
		}
	}

	private Collection<Node<String>> graph() {
		String[] names = new String[3]; //FIXME Start with "Node 9" and test layout with 2 nodes only. The line should be vertical but is not.
		for (int i = 0; i < names.length; i++) {
			names[i] = "Node " + i;
		}
		return Node.createGraph(names);
		
//		return Node.createGraph(new String[]{"1 234", "12 34", "123 4"});
	}

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
	// }

}