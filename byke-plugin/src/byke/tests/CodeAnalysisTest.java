package byke.tests;


import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.Assert;
import org.junit.Test;

import byke.PackageDependencyAnalysis;
import byke.dependencygraph.Node;


public class CodeAnalysisTest extends Assert {

	@Test
	public void staticField() throws Exception {
		JavaProject p = new JavaProject();
		ICompilationUnit a = p.createCompilationUnit("foopackage", "A.java", "class A { int foo = B.foo; }");
		ICompilationUnit b = p.createCompilationUnit("foopackage", "B.java", "class B { static int foo = 42; }");
		ICompilationUnit[] units = {a, b};
		Collection<Node<IBinding>> graph = new PackageDependencyAnalysis("foopackage", units, null).dependencyGraph();
		assertEquals(2, graph.size());
		Iterator<Node<IBinding>> it = graph.iterator();
		Node<IBinding> nodeA = it.next();
		Node<IBinding> nodeB = it.next();
		assertFalse(nodeB.providers().iterator().hasNext());
		assertSame(nodeB, nodeA.providers().iterator().next());
	}
	
}
