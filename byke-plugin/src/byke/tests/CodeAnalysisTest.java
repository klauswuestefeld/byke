package byke.tests;


import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import byke.DependencyAnalysis;
import byke.InvalidElement;
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
	
	
	@Ignore
	@Test
	public void genericConstraint1() throws Exception {
		assertDepends(
			"class A <T extends B> {}",
			"class B {}"
		);
	}

	
	@Ignore
	@Test
	public void genericConstraint2() throws Exception {
		assertDepends(
			"abstract class A implements Iterable<B> {}",
			"class B {}"
		);
	}

	
	@Test
	public void staticField() throws Exception {
		assertDepends(
			"class A { int foo = B.foo; }",
			"class B { static int foo = 42; }"
		);
	}

	
	@Test
	public void constructor() throws Exception {
		assertDepends(
			"class A { Object foo = new B(); }",
			"class B {}"
		);
	}
	
	
	@Test
	public void instanceField() throws Exception {
		assertDepends(
			"class A { Object foo = B.newC().field; }",
			"class B { static C newC() {return null;} } class C { int field; }"
		);
	}
	
	
	@Test
	public void staticFieldImport() throws Exception {
		assertDepends(
			"import static foopackage.B.foo; class A { int f = foo; }",
			"class B { static int foo = 42; }"
		);
	}
	
	
	@Test
	public void methodInvocation() throws Exception {
		assertDepends(
			"class A { { B.foo(); } }",
			"class B { static void foo() {} }"
		);
	}

	
	@Test
	public void declaredException() throws Exception {
		assertDepends(
			"class A { void foo() throws B {} }",
			"class B extends Exception {  }"
		);
	}

	
	@Test
	public void methodCallsMethod() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { void main() { foo(); } void foo() {} }");
		assertDepends(a, "main");
	}

	
	@Test
	public void externalMethodCallsAreIgnored() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { void main() { valid(); B.invalid(); } void valid(){} }");
		createCompilationUnit("B", "class B { static void invalid() {} }");
		assertDepends(a, "main");
	}

	
	@Test
	public void methodReadsField() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { int b; void main() { int a = this.b; } }");
		assertDepends(a, "main");
	}

	
	@Ignore
	@Test
	public void methodCallsConstructor() throws Exception {
		ICompilationUnit a = createCompilationUnit("A", "class A { static void main() { new A(); } }");
		assertDepends(a, "main");
	}

	
	private void assertDepends(String dependent, String provider) throws CoreException, JavaModelException, InvalidElement {
		ICompilationUnit unit = createCompilationUnit("A", dependent);
		createCompilationUnit("B", provider);

		assertDepends(unit.getParent(), "A");
	}
	
	
	private ICompilationUnit createCompilationUnit(String className, String code) throws CoreException {
		return project.createCompilationUnit("foopackage", className + ".java", "package foopackage; " + code);
	}
	
	
	private void assertDepends(IJavaElement element, String dependentName) throws CoreException, InvalidElement {
		//project.buildProject(null);
		project.joinAutoBuild();
		assertBuildOK();

		
		Collection<Node<IBinding>> graph = new DependencyAnalysis(element).dependencyGraph(null);
		assertTrue(""+graph, graph.size() > 1);

		Node<IBinding> dependent = findNode(dependentName, graph);
		
		Iterator<Node<IBinding>> it = graph.iterator();
		while (it.hasNext()) {
			Node<IBinding> provider = it.next();
			if (provider == dependent) continue;
			assertFalse("Invalid provider detected.", provider.name().equals("invalid"));
			assertTrue("Should be provider: " + provider.name(), dependent.providers().contains(provider));
		}
	}
	
	
	private Node<IBinding> findNode(String suffix, Collection<Node<IBinding>> graph) {
		for (Node<IBinding> node : graph)
			if (node.name().endsWith(suffix)) return node;
		
		throw new IllegalStateException("Node " + suffix + " not found in " + graph);
	}
	
	
	private void assertBuildOK() throws CoreException {
		IMarker[] problems = project.getProject().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		for (IMarker problem : problems)
			if (problem.getAttribute(IMarker.SEVERITY).equals(IMarker.SEVERITY_ERROR))
				fail("" + problem.getAttribute(IMarker.MESSAGE));
	}

}
