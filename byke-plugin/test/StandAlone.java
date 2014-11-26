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


public class StandAlone {

	private static final int NUMBER_OF_NODES = 11;
	private static final double DENSITY_OF_DEPENDENCIES = 1.3;

	private final static Random RANDOM = new Random(0);

	
	public static void main(String args[]) {
		new StandAlone();
	}

	private final Collection<NodeFigure> _graph = graph();

	private final Display _display = new Display(); // Has to be initialized before the _graphFigure although there is no explicit dependency, or else ColorConstants will throw a NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byke");
		shell.setSize(600, 600);

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
//		return NonMovableGraphTest.createSimpleCyclicDependencyGraph();
		return randomGraph();
//		return window();
	}


	protected Collection<NodeFigure> randomGraph() {
		String[] names = new String[NUMBER_OF_NODES];
		for(int i=0; i<NUMBER_OF_NODES;i++)
			names[i] = "Node "+i;
		return createGraph(names);
	}
	
	
	protected Collection<NodeFigure> window() {
		
		NodeFigure tela = new NodeFigure("Window(XXXX)");
		NodeFigure outraTela = new NodeFigure("AnotherWindow(XXXX)");
		NodeFigure addItems = new NodeFigure("addItems(XXXX)");
		NodeFigure addHandlers = new NodeFigure("addHandlers(XXXX)()");
		NodeFigure addSomething = new NodeFigure("addSomething(XXXX)()");
		NodeFigure doSomething = new NodeFigure("doSomething(XXXX)");
		NodeFigure toolbar = new NodeFigure("toolbar");
		NodeFigure tabSet = new NodeFigure("tabSet");
		NodeFigure aba1 = new NodeFigure("tab1");
		NodeFigure aba2 = new NodeFigure("tab2");
		NodeFigure aba3 = new NodeFigure("tab3");
		NodeFigure aba4 = new NodeFigure("tab4");
		NodeFigure aba5 = new NodeFigure("tab5");
		NodeFigure aba6 = new NodeFigure("tab6");
		
		tela.addProvider(addItems);
		tela.addProvider(addSomething);
		tela.addProvider(addHandlers);
		
		outraTela.addProvider(addItems);
		
		addItems.addProvider(tabSet);
		addItems.addProvider(toolbar);
		
		addHandlers.addProvider(doSomething);
		
		doSomething.addProvider(tabSet);
		
		tabSet.addProvider(aba1);
		tabSet.addProvider(aba2);
		tabSet.addProvider(aba6);
		tabSet.addProvider(aba3);
		tabSet.addProvider(aba4);
		tabSet.addProvider(aba5);
		
		//cyclic dependencies
		aba1.addProvider(tabSet);
//		aba3.addProvider(tabSet);
//		aba1.addProvider(addItems);
//		aba3.addProvider(addItems);
//		toolbar.addProvider(addItems);
		
		return Arrays.asList(tela, outraTela, addSomething, addItems, addHandlers, doSomething, toolbar, tabSet, aba1, aba2, aba3, aba4, aba5, aba6);
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