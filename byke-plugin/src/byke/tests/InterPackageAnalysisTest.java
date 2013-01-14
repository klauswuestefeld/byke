package byke.tests;

import java.util.Collection;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.Test;

import byke.DependencyAnalysis;
import byke.dependencygraph.Node;

public class InterPackageAnalysisTest extends CodeAnalysisTest {
	@Test
	public void mergeSubpackagesDependenciesOneLevel() throws Exception {
		ICompilationUnit classA = createCompilationUnit("foopackage","A", "import foopackage.subpackage.B; class A { B b; }");
		createCompilationUnit("foopackage", "C","public class C { }");
		createCompilationUnit("foopackage.subpackage","B", "import foopackage.C; public class B { C c; }");
		
		Collection<Node<IBinding>> graph = new DependencyAnalysis(classA.getParent()).dependencyGraph(null);
		assertEquals(3, graph.size());
		
		Node<IBinding> nodeA = findNode("A", graph);
		Node<IBinding> nodeC = findNode("C", graph);
		Node<IBinding> nodePackage = findNode("foopackage.subpackage", graph);
	
		assertUniqueDependency(nodeA,nodePackage);
		assertUniqueDependency(nodePackage,nodeC);
		assertNoDependency(nodeC);
	
		assertEquals(nodePackage.providers().size(), 1);
		assertTrue(nodePackage.providers().contains(nodeC));
	
	}

	@Test
	public void mergeSubpackagesDependenciesSeveralLevels() throws Exception {
		ICompilationUnit classA = createCompilationUnit("foopackage","A", "import foopackage.subpackage.B; class A { B b; }");
		createCompilationUnit("foopackage.subpackage","B", "import foopackage.C; public class B { C c; }");
		createCompilationUnit("foopackage", "C","import foopackage.subpackage.another.D; public class C { D d;}");
		createCompilationUnit("foopackage.subpackage.another", "D","public class D { }");
		
		Collection<Node<IBinding>> graph = new DependencyAnalysis(classA.getParent()).dependencyGraph(null);
		assertEquals(4, graph.size());

		Node<IBinding> nodeA = findNode("A", graph);
		Node<IBinding> nodeC = findNode("C", graph);
		Node<IBinding> nodePackage = findNode("foopackage.subpackage", graph);
		Node<IBinding> nodeSubPackage = findNode("foopackage.subpackage.another", graph);
		
		assertUniqueDependency(nodeA, nodePackage);
		assertUniqueDependency(nodePackage, nodeC);
		assertUniqueDependency(nodeC, nodeSubPackage);
		assertNoDependency(nodeSubPackage);
		
	}
	
	@Test
	public void mergeSubpackagesCiclicDependencies() throws Exception {
		ICompilationUnit classA = createCompilationUnit("foopackage","A", "import foopackage.subpackage.B; class A { B b; }");
		createCompilationUnit("foopackage.subpackage","B", "import foopackage.C; public class B { C c; }");
		createCompilationUnit("foopackage", "C","import foopackage.subpackage.another.D; public class C { D d;}");
		createCompilationUnit("foopackage.subpackage.another", "D","import foopackage.subpackage.B; public class D { B b;}");
		
		Collection<Node<IBinding>> graph = new DependencyAnalysis(classA.getParent()).dependencyGraph(null);
		
		assertEquals(4, graph.size());
		
		Node<IBinding> nodeA = findNode("A", graph);
		Node<IBinding> nodeC = findNode("C", graph);
		Node<IBinding> nodePackage = findNode("foopackage.subpackage", graph);
		Node<IBinding> nodeSubPackage = findNode("foopackage.subpackage.another", graph);
		
		assertUniqueDependency(nodeA, nodePackage);
		assertUniqueDependency(nodePackage, nodeC);
		assertUniqueDependency(nodeC, nodeSubPackage);
		assertUniqueDependency(nodeSubPackage, nodePackage);

		assertDependencyIndirectlyOnly(nodeSubPackage,nodeC);
		assertDependencyIndirectlyOnly(nodeC,nodePackage);
		assertDependencyIndirectlyOnly(nodePackage,nodeSubPackage);
		
	}
	
	private void assertDependencyIndirectlyOnly(Node<IBinding> nodeToAnalyze, Node<IBinding> nodeDependence) {
		assertFalse(nodeToAnalyze.dependsDirectlyOn(nodeDependence));
		assertTrue(nodeToAnalyze.dependsOn(nodeDependence));
	}

	private void assertNoDependency(Node<IBinding> nodeToAnaylize) {
		assertEquals(nodeToAnaylize.providers().size(), 0);
	}

	private void assertUniqueDependency(Node<IBinding> nodeToAnalyse, Node<IBinding> nodeProvider) {
		assertEquals(nodeToAnalyse.providers().size(), 1);
		assertTrue(nodeToAnalyse.providers().contains(nodeProvider));
	}
	
}
