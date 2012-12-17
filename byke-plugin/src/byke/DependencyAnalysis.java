//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.
package byke;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byke.dependencygraph.Node;


public class DependencyAnalysis implements NodeAccumulator {
	
	@SuppressWarnings("unchecked")
	private static final Node<IBinding>[] NODE_ARRAY = new Node[0];

	private final Map<String, Node<IBinding>> _nodesByKey = new HashMap<String, Node<IBinding>>();

	private IJavaElement _subject;


	public DependencyAnalysis(IJavaElement child) throws InvalidElement {
		_subject = enclosingSubject(child);
		if (_subject == null) throw new InvalidElement("No supported subject found for child: " + child);
	}
	
	
	private IJavaElement enclosingSubject(IJavaElement element) throws InvalidElement {
		if (element == null) return null;
		if (element instanceof IType) return element;
		if (element instanceof ICompilationUnit) return ((ICompilationUnit)element).findPrimaryType();
		if (element instanceof IMember)
			return enclosingTypeOf((IMember)element);

		return enclosingPackageOf(element);
	}


	private IJavaElement enclosingTypeOf(IMember element) {
		return element.getAncestor(IJavaElement.TYPE);
	}
	
	
	private IJavaElement enclosingPackageOf(IJavaElement element) throws InvalidElement {
		IPackageFragment result = (IPackageFragment)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		assertNotBinary(result); 
		return result;
	}


	private void assertNotBinary(IPackageFragment result) throws InvalidElement {
		try {
			if (result.getKind() == IPackageFragmentRoot.K_BINARY)
				throw new InvalidElement("Binary Package");
		} catch (JavaModelException e) {
			throw new InvalidElement(e);
		}
	}


	public Collection<Node<IBinding>> dependencyGraph(IProgressMonitor monitor) {
		try {
			populateNodes(monitor);
		} catch (Exception x) {
			x.printStackTrace();
		}
		
		Collection<Node<IBinding>> finalGraph = _nodesByKey.values();
		
		if(MergeClassPatterns.existsMergeClassValue())
			finalGraph = mergeDependencies();
		
		return finalGraph;
	}


	private Collection<Node<IBinding>> mergeDependencies() {
		List<Node<IBinding>> toRemove = new ArrayList<Node<IBinding>>();
		List<Pattern> patterns = MergeClassPatterns.getPatterns();
		for (Pattern pattern : patterns)
			for(Node<IBinding> node : _nodesByKey.values()) {
				Matcher matcher = pattern.matcher(node.name());
				if(matcher.find())
					for(Node<IBinding> nodeToMerge : _nodesByKey.values())
						if(nodeToMerge.name().equals(matcher.group(1))) {
							switchProvider(node, nodeToMerge);
							toRemove.add(node);
						}
			}

		_nodesByKey.values().removeAll(toRemove);
		return _nodesByKey.values();
	}

	
	private void switchProvider(Node<IBinding> node, Node<IBinding> nodeToSwitch) {
		for(Node<IBinding> nodeToMerge : _nodesByKey.values())
			if(nodeToMerge.providers().remove(node))
				nodeToMerge.addProvider(nodeToSwitch);
	}

	private void populateNodes(IProgressMonitor monitor) throws JavaModelException {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		
		ASTVisitor visitor = _subject instanceof IType
			? new TypeVisitor()
			: new PackageAnalyser(this, _subject.getElementName());

		ICompilationUnit[] compilationUnits = compilationUnits();
		monitor.beginTask("dependency analysis", compilationUnits.length);
		
		for (ICompilationUnit each : compilationUnits) {
			if (monitor.isCanceled()) break;
			populateNodes(monitor, parser, visitor, each);
		}
	}


	private void populateNodes(IProgressMonitor monitor, ASTParser parser, ASTVisitor visitor, ICompilationUnit each) {
		monitor.subTask(each.getElementName());

		parser.setResolveBindings(true);
		parser.setSource(each);
		CompilationUnit node = (CompilationUnit)parser.createAST(monitor);
		node.accept(visitor);
		monitor.worked(1);
	}


	private ICompilationUnit[] compilationUnits() throws JavaModelException {
		return _subject instanceof IPackageFragment
			? ((IPackageFragment)_subject).getCompilationUnits()
			: new ICompilationUnit[] { ((ICompilationUnit)_subject.getAncestor(IJavaElement.COMPILATION_UNIT)) };
	}


	@Override
	public Node<IBinding> produceNode(IBinding binding, JavaType kind) {
		String key = binding.getKey();
		return produceNode(key, binding, kind);
	}

	
	@Override
	public Node<IBinding> produceNode(String key, JavaType kind) {
		return produceNode(key, null, kind);
	}

	
	private Node<IBinding> produceNode(String key, IBinding binding, JavaType kind) {
		Node<IBinding> node = _nodesByKey.get(key);
		if (null == node) {
			String name = binding == null ? key : binding.getName();
			node = new Node<IBinding>(name, kind);
			node.payload(binding);
			_nodesByKey.put(key, node);
		}
		return node;
	}

	
	class TypeVisitor extends ASTVisitor {
		
		@Override
		public boolean visit(TypeDeclaration node) {
			ITypeBinding binding = node.resolveBinding();
			IJavaElement javaElement = binding.getJavaElement();
			if (!javaElement.getHandleIdentifier().equals(_subject.getHandleIdentifier()))
				return true;
			
			new TypeAnalyser(node, DependencyAnalysis.this, binding);
			return false;
		}
	}
	
		
	public IJavaElement subject() {
		return _subject;
	}


	@Override
	public void remove(Node<IBinding> node) {
		Node<IBinding> removed = _nodesByKey.remove(node.payload().getKey());
		if (removed != node) throw new IllegalStateException("Node to be removed not found: " + node.name());
	}


	@Override
	public Node<IBinding>[] nodes() {
		return _nodesByKey.values().toArray(NODE_ARRAY);
	}

}