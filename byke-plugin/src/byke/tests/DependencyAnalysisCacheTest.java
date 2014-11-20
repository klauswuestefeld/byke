package byke.tests;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.Assert;
import org.junit.Test;

import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.dependencygraph.Node;
import byke.views.DependencyAnalysisCache;

public class DependencyAnalysisCacheTest extends CodeAnalysisTest {

	@Test
	public void cacheForPackage() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { }");
	
		String layout = cacheFor(unit.getParent());
		Assert.assertEquals("digraph foopackage {\n  \"Test\"\n}\n", layout);
	}

	@Test
	public void cacheForClass() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { void method1(){} void method2() { method1(); } }");
		
		String layout = cacheFor(unit);
		Assert.assertEquals(
				"digraph Test {\n"
			+ "  \"method2()\"\n"
			+ "  \"method1()\"\n"
			+ "  \"method2()\" -> \"method1()\"\n"
			+ "}"
			+ "\n",
			layout);
	}
	
	@Test
	public void cacheNotFound() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { }");
		buildProject();
		
		DependencyAnalysis dependencyAnalysis = new DependencyAnalysis(unit.getParent());
		Collection<Node<IBinding>> graph = dependencyAnalysis.dependencyGraph(null);
		Assert.assertEquals(1, graph.size());
		
		DependencyAnalysisCache dependencyMap = new DependencyAnalysisCache();
		String layout = dependencyMap.getCacheFor(dependencyAnalysis.subject());
		Assert.assertNull(layout);
	}

	@Test
	public void cacheForNullElement() throws Exception {
		String layout = new DependencyAnalysisCache().getCacheFor(null);
		Assert.assertNull(layout);
	}

	private String cacheFor(IJavaElement element) throws CoreException, InvalidElement {
		buildProject();
		
		DependencyAnalysis dependencyAnalysis = new DependencyAnalysis(element);
		Collection<Node<IBinding>> graph = dependencyAnalysis.dependencyGraph(null);
		DependencyAnalysisCache dependencyMap = new DependencyAnalysisCache();
		dependencyMap.keep(dependencyAnalysis.subject(), graph);
		
		return dependencyMap.getCacheFor(dependencyAnalysis.subject());
	}
}
