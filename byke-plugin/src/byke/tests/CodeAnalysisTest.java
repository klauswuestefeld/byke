package byke.tests;


import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.dependencygraph.Node;
import byke.tests.workspaceutils.JavaProject;

public class CodeAnalysisTest extends Assert {
	
	private JavaProject project;

	
	@Before public void beforeCodeAnalysisTest() throws Exception { project = new JavaProject();}
	@After public void afterCodeAnalysisTest() throws Exception { project.dispose(); }
	
	
	protected ICompilationUnit createCompilationUnit(String className, String code) throws CoreException {
		return project.createCompilationUnit("foopackage", className + ".java", "package foopackage; " + code);
	}
	
	protected ICompilationUnit createCompilationUnit(String packageName, String className, String code) throws CoreException {
		return project.createCompilationUnit(packageName, className + ".java", "package "+packageName+"; " + code);
	}
	
	
	protected void assertDepends(IJavaElement toAnalyse, String dependentName, String... providers) throws CoreException, InvalidElement {
		//project.buildProject(null);
		project.joinAutoBuild();
		assertBuildOK();
		
		Collection<Node<IBinding>> graph = new DependencyAnalysis(toAnalyse).dependencyGraph(null);
		assertTrue("Graph should have more than one node: " + graph, graph.size() > 1);

		Node<IBinding> dependent = findNode(dependentName, graph);
		
		//TODO check provider's quantity
		for(String providerName : providers)
			doAssertions(dependent, findNode(providerName, graph));
		
		if(providers.length == 0)
			for(Node<IBinding> provider : graph)
				doAssertions(dependent, provider);
	}
	
	
	private void doAssertions(Node<IBinding> dependent, Node<IBinding> provider) {
		if (provider == dependent) return;
		assertFalse("Invalid provider detected.", provider.name().equals("invalid"));
		assertTrue("Should be provider: " + provider.name(), dependent.providers().contains(provider));
	}
	
	
	protected Node<IBinding> findNode(String suffix, Collection<Node<IBinding>> graph) {
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
