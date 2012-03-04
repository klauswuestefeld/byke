//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.
package byke;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byke.dependencygraph.Node;


public class DependencyAnalysis implements NodeProducer {
	private final Map<String, Node<IBinding>> _nodes = new HashMap<String, Node<IBinding>>();

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
		} catch (JavaModelException x) {
			x.printStackTrace();
		}
		return _nodes.values();
	}

	
	private void populateNodes(IProgressMonitor monitor) throws JavaModelException {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		ASTVisitor visitor = _subject instanceof IType
			? new TypeVisitor()
			: new PackageVisitor(this, _subject.getElementName());

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
		String name = binding.getName();
		return produceNode(name, binding, kind);
	}

	
	@Override
	public Node<IBinding> produceNode(String name, JavaType kind) {
		return produceNode(name, null, kind);
	}

	
	private Node<IBinding> produceNode(String name, IBinding binding, JavaType kind) {
		Node<IBinding> node = _nodes.get(name);
		if (null == node) {
			node = new Node<IBinding>(name, kind);
			node.payload(binding);
			_nodes.put(name, node);
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
			TypeAnalyser typeVisitor = new TypeAnalyser(DependencyAnalysis.this, binding);
			for (Object decl : node.bodyDeclarations())
				((ASTNode)decl).accept(typeVisitor);
			return false;
		}
	}
	
		
	public IJavaElement subject() {
		return _subject;
	}

}