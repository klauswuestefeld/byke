package byke.tests;


import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import byke.PackageDependencyAnalysis;
import byke.dependencygraph.Node;
import byke.tests.utils.JavaProject;


public class CodeAnalysisTest extends Assert {

	private JavaProject project;

	@Before
	public void beforeCodeAnalysisTest() throws Exception {
		project = new JavaProject();
	}
	@After
	public void afterCodeAnalysisTest() throws Exception {
		project.dispose();
	}
	
	
	@Test
	public void staticField() throws Exception {
		ICompilationUnit a = project.createCompilationUnit("foopackage", "A.java", "class A { int foo = B.foo; }");
		ICompilationUnit b = project.createCompilationUnit("foopackage", "B.java", "class B { static int foo = 42; }");
		ICompilationUnit[] units = {a, b};
		Collection<Node<IBinding>> graph = new PackageDependencyAnalysis("foopackage", units, null).dependencyGraph();
		assertEquals(2, graph.size());
		Iterator<Node<IBinding>> it = graph.iterator();
		Node<IBinding> nodeA = it.next();
		Node<IBinding> nodeB = it.next();
		assertFalse(nodeB.providers().iterator().hasNext());
		assertSame(nodeB, nodeA.providers().iterator().next());
	}

	@Test
	public void methodInvocation() throws Exception {
		ICompilationUnit a = project.createCompilationUnit("foopackage", "A.java", "class A { { B.foo(); } }");
		ICompilationUnit b = project.createCompilationUnit("foopackage", "B.java", "class B { static void foo() {} }");
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
