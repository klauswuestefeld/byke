package byke.tests;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.dependencygraph.Node;
import byke.views.cache.DependencyAnalysisCache;
import byke.views.cache.NodeFigure;

public class DependencyAnalysisCacheTest extends CodeAnalysisTest {

	private DependencyAnalysisCache _subject;
	
	@Before
	public void setUp() {
		_subject = new DependencyAnalysisCache();
	}

	@Test
	public void fileContentForPackage() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { }");
	
		String layout = cacheFileFor(unit.getParent());
		Assert.assertEquals(""
				+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ns2:gexf xmlns:ns2=\"http://www.gexf.net/1.2draft\">\n"
				+ "    <graph defaultedgetype=\"directed\">\n"
				+ "        <nodes>\n"
				+ "            <node label=\"Test\" id=\"Test\"/>\n"
				+ "        </nodes>\n"
				+ "        <edges/>\n"
				+ "    </graph>\n"
				+ "</ns2:gexf>\n",
				layout);
	}

	@Test
	public void fileContentForClass() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { void method1(){} void method2() { method1(); } }");
		
		String layout = cacheFileFor(unit);
		assertEquals(""
				+ "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ns2:gexf xmlns:ns2=\"http://www.gexf.net/1.2draft\">\n"
				+ "    <graph defaultedgetype=\"directed\">\n"
				+ "        <nodes>\n"
				+ "            <node label=\"method2()\" id=\"method2()\"/>\n"
				+ "            <node label=\"method1()\" id=\"method1()\"/>\n"
				+ "        </nodes>\n"
				+ "        <edges>\n"
				+ "            <edge target=\"method1()\" source=\"method2()\"/>\n"
				+ "        </edges>\n"
				+ "    </graph>\n"
				+ "</ns2:gexf>\n",
				layout);
	}
	
	@Test
	public void fileNotFound() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { }");
		buildProject();
		
		DependencyAnalysis dependencyAnalysis = new DependencyAnalysis(unit.getParent());
		Collection<Node<IBinding>> graph = dependencyAnalysis.dependencyGraph(null);
		Assert.assertEquals(1, graph.size());
		
		String layout = _subject.getCacheFileFor(dependencyAnalysis.subject());
		Assert.assertTrue(layout.isEmpty());
	}
	
	
	@Test
	public void fileForNullElement() throws Exception {
		String layout = _subject.getCacheFileFor(null);
		Assert.assertTrue(layout.isEmpty());
	}

	
	@Test
	public void cacheForPackage() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { }");
		
		Collection<NodeFigure> nodes = cacheFor(unit.getParent());
		
		assertEquals(1, nodes.size());
		NodeFigure node = nodes.iterator().next();
		assertEquals("Test", node.name());
		assertTrue(node.providers().isEmpty());
	}
	
	@Test
	public void cacheForClass() throws Exception {
		ICompilationUnit unit = createCompilationUnit(
				"Test", 
				"class Test { void method1(){} void method2() { method1(); } }");
		
		Collection<NodeFigure> nodes = cacheFor(unit);
		
		NodeFigure method1 = nodeFor(nodes, "method1()");
		NodeFigure method2 = nodeFor(nodes, "method2()");
		
		assertEquals(2, nodes.size());
		assertEquals(0, method1.providers().size());
		assertEquals(1, method2.providers().size());
		assertTrue(method1.providers().isEmpty());
		assertTrue(method2.providers().contains(method1));
	}
	
	private NodeFigure nodeFor(Collection<NodeFigure> nodes, String name) {
		for(NodeFigure node : nodes)
			if(node.name().equals(name))
				return node;
		return null;
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
		
		Collection<NodeFigure> nodes = _subject.getCacheFor(dependencyAnalysis.subject());
		Assert.assertTrue(nodes.isEmpty());
	}
	
	
	@Test
	public void cacheForNullElement() throws Exception {
		Collection<NodeFigure> nodes = _subject.getCacheFor(null);
		Assert.assertTrue(nodes.isEmpty());
	}
	
	
	
		
	private String cacheFileFor(IJavaElement element) throws CoreException, InvalidElement {
		buildProject();
		
		DependencyAnalysis dependencyAnalysis = new DependencyAnalysis(element);
		Collection<Node<IBinding>> graph = dependencyAnalysis.dependencyGraph(null);
		_subject.keep(dependencyAnalysis.subject(), graph);
		
		return _subject.getCacheFileFor(dependencyAnalysis.subject());
	}

	private Collection<NodeFigure> cacheFor(IJavaElement element) throws CoreException, InvalidElement {
		buildProject();
		
		DependencyAnalysis dependencyAnalysis = new DependencyAnalysis(element);
		Collection<Node<IBinding>> graph = dependencyAnalysis.dependencyGraph(null);
		_subject.keep(dependencyAnalysis.subject(), graph);
		
		return _subject.getCacheFor(dependencyAnalysis.subject());
	}
}
