package byke.tests;


import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

	
	private void assertDepends(String dependent, String provider) throws CoreException, JavaModelException {
		ICompilationUnit a = project.createCompilationUnit("foopackage", "A.java", "package foopackage; " + dependent);
		ICompilationUnit b = project.createCompilationUnit("foopackage", "B.java", "package foopackage; " + provider);

		//project.buildProject(null);
		project.joinAutoBuild();
		assertBuildOK();

		ICompilationUnit[] units = {a, b};
		Collection<Node<IBinding>> graph = new PackageDependencyAnalysis("foopackage", units, null).dependencyGraph();
		Iterator<Node<IBinding>> it = graph.iterator();
		Node<IBinding> nodeA = it.next();
		assertTrue(nodeA.name().contains("A"));
		
		while (it.hasNext()) {
			Node<IBinding> providerNode = it.next();
			assertTrue("Should be provider: " + providerNode.name(), nodeA.providers().contains(providerNode));
		}
	}
	
	
	private void assertBuildOK() throws CoreException {
		IMarker[] problems = project.getProject().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		for (IMarker problem : problems)
			if (problem.getAttribute(IMarker.SEVERITY).equals(IMarker.SEVERITY_ERROR))
				fail("" + problem.getAttribute(IMarker.MESSAGE));
	}

}
