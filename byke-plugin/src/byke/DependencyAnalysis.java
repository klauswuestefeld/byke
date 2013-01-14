//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.
package byke;


import java.util.ArrayList;
import java.util.Arrays;
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
		assertValid(result); 
		return result;
	}


	private void assertValid(IPackageFragment fragment) throws InvalidElement {
		if (fragment == null) throw new InvalidElement("Null Package Fragment");
		try {
			if (fragment.getKind() == IPackageFragmentRoot.K_BINARY)
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
		
		if(MergeClassPatterns.existsMergeClassValue())
			mergeDependencies();
		
		mergeSubpackagesDependencies();
		
		return _nodesByKey.values();
	}


	private void mergeSubpackagesDependencies() {
		if(!(_subject instanceof IPackageFragment))
			return;
		
		removeSubjectNode();

		List<Node<IBinding>> toRemove = new ArrayList<Node<IBinding>>();

		for(Node<IBinding> node : _nodesByKey.values()) {
			if(node.kind().equals(JavaType.PACKAGE))
				continue;
			
			ITypeBinding payload = (ITypeBinding)node.payload();
			if(!payload.getPackage().equals( _subject))
				for(Node<IBinding> packageToMerge : _nodesByKey.values())
					if(packageToMerge.name().equals(payload.getPackage().getName())) {
						switchProvider(node, packageToMerge);
						packageToMerge.addProviders(node.providers());
						toRemove.add(node);
					}
		}
		
		_nodesByKey.values().removeAll(toRemove);
	}

	private void removeSubjectNode() {
		for(Node<IBinding> node : _nodesByKey.values()) 
			if(_subject.getElementName().equals(node.name())) { 
				_nodesByKey.values().remove(node);
				for(Node<IBinding> nodeToRemoveSubjectNode : _nodesByKey.values())
					nodeToRemoveSubjectNode.providers().remove(node);
				return;
			}
	}


	private void mergeDependencies() {
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

		List<ICompilationUnit> compilationUnits = compilationUnits();
		monitor.beginTask("dependency analysis", compilationUnits.size());
		
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


	public List<IPackageFragment> withSubpackages() throws JavaModelException {
		if(!(_subject instanceof IPackageFragment)) return null;
		
		List<IPackageFragment> ret = new ArrayList<IPackageFragment>();
		
		IJavaElement[] packages = ((IPackageFragmentRoot)_subject.getParent()).getChildren();
		String[] names = _subject.getElementName().split("\\.");
		int namesLength = names.length;
		for (int i= 0; i < packages.length; i++) {
			String[] otherNames = ((IPackageFragment) packages[i]).getElementName().split("\\.");
			if (otherNames.length < namesLength) 
				continue;
			
			for (int j = 0; j < namesLength; j++)
				if (names[j].equals(otherNames[j])) {
					ret.add((IPackageFragment)packages[i]);
					break;
				}
		}
		
		return ret;
	}

	
	private List<ICompilationUnit> compilationUnits() throws JavaModelException {
		
		List<IPackageFragment> subpackages = withSubpackages();
		if(subpackages == null)
			return Arrays.asList(((ICompilationUnit)_subject.getAncestor(IJavaElement.COMPILATION_UNIT)));

		List<ICompilationUnit> ret = new ArrayList<ICompilationUnit>();
	
		for(IPackageFragment subpackage : subpackages)
			ret.addAll(Arrays.asList(subpackage.getCompilationUnits()));

		return ret;

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