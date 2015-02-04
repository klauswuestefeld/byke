import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import byke.views.cache.NodeFigure;
import byke.views.layout.ui.NonMovableGraph;
import byke.views.layout.ui.MnemonicColorsTest;


public class StandAlone {

	private static final int NUMBER_OF_NODES = 200;
	private static final double DENSITY_OF_DEPENDENCIES = 1.1;

	private final static Random RANDOM = new Random();

	
	public static void main(String args[]) {
		new StandAlone();
	}

	private final Collection<NodeFigure> _graph = graph();

	private final Display _display = new Display(); // Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants will throw a NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byke");
		shell.setSize(1600, 1200);

		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		new NonMovableGraph(shell, _graph);

		shell.open();
//		shell.layout();

		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				_display.sleep();
			}
		}
	}

	
	private Collection<NodeFigure> graph() {
		return MnemonicColorsTest.cyclesIsRedGraph();
	}


	protected Collection<NodeFigure> randomGraph() {
		String[] names = new String[NUMBER_OF_NODES];
		for(int i=0; i<NUMBER_OF_NODES;i++)
			names[i] = "Node "+i;
		return createGraph(names);
	}
	
	
	protected Collection<NodeFigure> window() {
		
		NodeFigure window = new NodeFigure("Window(XXXX)");
		NodeFigure anotherWindow = new NodeFigure("AnotherWindow(XXXX)");
		NodeFigure addItems = new NodeFigure("addItems(XXXX)");
		NodeFigure addHandlers = new NodeFigure("addHandlers(XXXX)()");
		NodeFigure addSomething = new NodeFigure("addSomething(XXXX)()");
		NodeFigure doSomething = new NodeFigure("doSomething(XXXX)");
		NodeFigure toolbar = new NodeFigure("toolbar");
		NodeFigure tabSet = new NodeFigure("tabSet");
		NodeFigure tab1 = new NodeFigure("tab1");
		NodeFigure tab2 = new NodeFigure("tab2");
		NodeFigure tab3 = new NodeFigure("tab3");
		NodeFigure tab4 = new NodeFigure("tab4");
		NodeFigure tab5 = new NodeFigure("tab5");
		NodeFigure tab6 = new NodeFigure("tab6");
		
		window.addProvider(addItems);
		window.addProvider(addSomething);
		window.addProvider(addHandlers);
		
		anotherWindow.addProvider(addItems);
		
		addItems.addProvider(tabSet);
		addItems.addProvider(toolbar);
		
		addHandlers.addProvider(doSomething);
		
		doSomething.addProvider(tabSet);
		
		tabSet.addProvider(tab1);
		tabSet.addProvider(tab2);
		tabSet.addProvider(tab6);
		tabSet.addProvider(tab3);
		tabSet.addProvider(tab4);
		tabSet.addProvider(tab5);
		
		//cyclic dependencies
//		tab1.addProvider(tabSet);
//		tab3.addProvider(tabSet);
//		tab1.addProvider(addItems);
//		tab3.addProvider(addItems);
		toolbar.addProvider(addItems);
		toolbar.addProvider(anotherWindow);
		
		return Arrays.asList(window, anotherWindow, addSomething, addItems, addHandlers, doSomething, toolbar, tabSet, tab1, tab2, tab3, tab4, tab5, tab6);
	}
	
	
	private static Collection<NodeFigure> createGraph(String[] names) {
		List<NodeFigure> result = new ArrayList<NodeFigure>();
		for (String element : names) {
			NodeFigure node = new NodeFigure();
			node.name(element);
			result.add(node);
		}
		produceRandomDependencies(result);
		return result;
	}

	
	private static void produceRandomDependencies(List<NodeFigure> graph) {
		int dependenciesToCreate = (int)(graph.size() * DENSITY_OF_DEPENDENCIES);
	
		while (dependenciesToCreate-- > 0) {
			NodeFigure node1 = drawOneFrom(graph);
			NodeFigure node2 = drawOneFrom(graph);
			if (node1 == node2) continue;
	
			node1.addProvider(node2);
		}
	}

	
	private static NodeFigure drawOneFrom(List<NodeFigure> hat) {
		return hat.get(RANDOM.nextInt(hat.size()));
	}

}