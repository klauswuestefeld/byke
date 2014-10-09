import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import byke.JavaType;
import byke.dependencygraph.Node;
import byke.views.layout.ui.NonMovableGraph;


public class StandAlone {

	private static final int NUMBER_OF_NODES = 10;
	private static final double DENSITY_OF_DEPENDENCIES = 0.9;

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

		new NonMovableGraph<String>(shell, _graph);

		shell.open();
		shell.layout();

		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				_display.sleep();
			}
		}
	}

	
	private Collection<Node<String>> graph() {
		return createSimpleCyclicDependencyGraph();
		//return randomGraph();
		//return window();
	}


	private Collection<Node<String>> randomGraph() {
		String[] names = new String[NUMBER_OF_NODES];
		for(int i=0; i<NUMBER_OF_NODES;i++)
			names[i] = "Node "+i;
		return createGraph(names);
	}


	private Collection<Node<String>> createSimpleDependencyGraph() {
		Node<String> st1 = new Node<String>("static 1");
		Node<String> st2 = new Node<String>("static 2");
		Node<String> m1 = new Node<String>("method1");
		Node<String> m2 = new Node<String>("method2");
		m1.addProvider(m2);
		return Arrays.asList(st1, st2, m1, m2);
	}
	
	private Collection<Node<String>> createSimpleCyclicDependencyGraph() {
		Node<String> n1 = new Node<String>("n 1");
		Node<String> n2 = new Node<String>("n 2");
		Node<String> n3 = new Node<String>("n 3");
		n1.addProvider(n2);
		n2.addProvider(n1);
		n2.addProvider(n3);
		return Arrays.asList(n1, n2, n3);
	}


	private Collection<Node<String>> window() {
		
		Node<String> tela = new Node<String>("Window(XXXX)");
		Node<String> outraTela = new Node<String>("AnotherWindow(XXXX)");
		Node<String> addItems = new Node<String>("addItems(XXXX)");
		Node<String> addHandlers = new Node<String>("addHandlers(XXXX)()");
		Node<String> addSomething = new Node<String>("addSomething(XXXX)()");
		Node<String> doSomething = new Node<String>("doSomething(XXXX)");
		Node<String> toolbar = new Node<String>("toolbar");
		Node<String> tabSet = new Node<String>("tabSet");
		Node<String> aba1 = new Node<String>("tab1");
		Node<String> aba2 = new Node<String>("tab2");
		Node<String> aba3 = new Node<String>("tab3");
		Node<String> aba4 = new Node<String>("tab4");
		Node<String> aba5 = new Node<String>("tab5");
		Node<String> aba6 = new Node<String>("tab6");
		
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