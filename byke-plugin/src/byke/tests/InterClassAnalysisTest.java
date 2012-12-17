package byke.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.Ignore;
import org.junit.Test;

import byke.BykePlugin;
import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.dependencygraph.Node;
import byke.preferences.PreferenceConstants;

public class InterClassAnalysisTest extends CodeAnalysisTest {

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
	public void mergeClassesThatEndsWithClass() throws Exception {
		IPreferenceStore store = BykePlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATTERN_MERGE_CLASS, "*Class");
		
		ICompilationUnit unit = createCompilationUnit("A", "class A { { BClass.foo(); BClassFoo.foo(); } }");
		createCompilationUnit("AClass", "class AClass { }");
		createCompilationUnit("B", "class B { }");
		createCompilationUnit("BClass", "class BClass { static void foo() {} }");
		createCompilationUnit("BClassFoo", "class BClassFoo { static void foo() {} }");
		
		assertMergedClasses(unit.getParent(), Arrays.asList("A", "B", "BClassFoo"));
	}

	@Test
	public void mergeClassesThatStartsWithClass() throws Exception {
		IPreferenceStore store = BykePlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATTERN_MERGE_CLASS, "Class*");
		
		ICompilationUnit unit = createCompilationUnit("A", "class A { { ClassB.foo(); ClassBFoo.foo(); } }");
		createCompilationUnit("ClassA", "class ClassA { }");
		createCompilationUnit("B", "class B { }");
		createCompilationUnit("ClassB", "class ClassB { static void foo() {} }");
		createCompilationUnit("ClassBFoo", "class ClassBFoo { static void foo() {} }");
		
		assertMergedClasses(unit.getParent(), Arrays.asList("A", "B", "ClassBFoo"));
	}
	
	private void assertMergedClasses(IJavaElement parent, List<String> nodeNames) throws Exception {
		Collection<Node<IBinding>> graph = new DependencyAnalysis(parent).dependencyGraph(null);
		assertEquals(3, graph.size());
		for (Node<IBinding> node : graph)
			assertTrue(nodeNames.contains(node.name()));
		
		assertDepends(parent, "A");
	}

	private void assertDepends(String dependent, String provider) throws CoreException, JavaModelException, InvalidElement {
		ICompilationUnit unit = createCompilationUnit("A", dependent);
		createCompilationUnit("B", provider);
	
		assertDepends(unit.getParent(), "A");
	}
}
