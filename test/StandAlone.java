import java.util.*;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class StandAlone {

	public static void main(String args[]) {
		new StandAlone();
	}

	private final List<Node<Object> > _nodes = new ArrayList<Node<Object> >();
	
	private final Collection<Node<Object> > _graph = graph();

	private final Display _display = new Display(); //Has to be initialized
													// before the _graphFigure
													// although there is no
													// explicit dependency, or
													// else ColorConstants will
													// throw a
													// NullPointerException. :(

	private StandAlone() {
		Shell shell = new Shell(_display);
		shell.setText("Byecycle");
		shell.setSize(500, 500);
		
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		GraphCanvas canvas = new GraphCanvas(shell, new GraphCanvas.Listener(){
			public void nodeSelected(Node node) {
				System.out.println("Node:" + node);
			}
		});
		canvas.setGraph((Iterable<Node>)_graph);

		shell.open();
		shell.layout();

		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
				canvas.tryToImproveLayout();
				_display.sleep();
			}
		}
	}


	private Collection<Node<Object> > graph() {
	    String[] names = new String[36];
	    for (int i = 0; i < names.length; i++) {
            //names[i] = "Node skdjfhskdfh.sdkfskdlf.sdfksdfj" + i;
            names[i] = "Node " + i;
        }
		return Node.createGraph(names);
		

/*
		Node transaction = new Node("Transaction", "interface");

		Node prevayler = new Node("Prevayler", "interface");
		prevayler.addProvider(transaction);

		Node impl = new Node("impl", "package");
		impl.addProvider(prevayler);
		impl.addProvider(transaction);

		Node factory = new Node("Factory", "class");
		factory.addProvider(impl);
		factory.addProvider(prevayler);

		
		Node prevaylerpackage = new Node("prevayler", "package");

		Node prevaylerdemo = new Node("prevaylerx.demos", "package");
		prevaylerdemo.addProvider(prevaylerpackage);
		
		Node prevaylertest = new Node("prevaylerx.tests", "package");
		prevaylertest.addProvider(prevaylerpackage);
		
		Node prevaylerplugin = new Node("prevaylerx.plugins.queries", "package");
		prevaylerplugin.addProvider(prevaylerpackage);
		
		
		return new Node[]{transaction, prevayler, impl, factory,
				prevaylerpackage, prevaylertest, prevaylerdemo, prevaylerplugin};

*/
		
		/*

		createDependency("Não Relacional", "Negócio");
		createDependency("Software Livre", "Negócio");
		createDependency("Empresas Pequenas", "Negócio");
		createDependency("Negócio", "CNPJ");
		createDependency("CNPJ", "Falar c Contador");
		createDependency("Negócio", "Vendas");
		createDependency("Vendas", "Serviço/Produto");
		createDependency("Negócio", "Serviço/Produto");
		createDependency("Serviço/Produto", "Desenvolvimento");
		createDependency("Serviço/Produto", "Idéia");
		createDependency("Desenvolvimento", "Dedicação");
		createDependency("Vendas", "Dedicação");
		
		
		
		
		
		
		
		Node[] result = new Node[_nodes.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (Node)_nodes.get(i);
		}
		return result;
		
		*/
	}


	/*
	private void createDependency(String dependent, String provider) {
		produceNode(dependent).addProvider(produceNode(provider));
		
	}*/


	private Node<Object> produceNode(String name) {
		name = " " + name;
		Iterator<Node<Object>> i = _nodes.iterator();
		while (i.hasNext()) {
			Node<Object> candidate = i.next();
			if (candidate.name().equals(name)) return candidate;
		}
		return createNode(name);
	}	
	
	private Node<Object> createNode(String name) {
		Node<Object> result = new Node<Object>(name, "no type");
		_nodes.add(result);
		return result;
	}	


	
}