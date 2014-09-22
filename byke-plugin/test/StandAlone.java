import java.util.ArrayList;
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

	@SuppressWarnings("unused")
	private static final int NUMBER_OF_NODES = 40;
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
		return window();
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
		
		Collection<Node<String>> result = new ArrayList<Node<String>>();
		result.add(tela);
		result.add(outraTela);
		result.add(addSomething);
		result.add(addItems);
		result.add(addHandlers);
		result.add(doSomething);
		result.add(toolbar);
		result.add(tabSet);
		result.add(aba1);
		result.add(aba6);
		result.add(aba2);
		result.add(aba3);
		result.add(aba4);
		result.add(aba5);
		
		return result;
	}
	
	
	@SuppressWarnings("unused")
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