//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.
package byke;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byke.dependencygraph.Node;
import byke.preferences.PreferenceConstants;


public class DependencyAnalysis {
	private final Map<String, Node<IBinding>> _nodes = new HashMap<String, Node<IBinding>>();

	private List<Pattern> _excludedClassPattern;

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
			: new PackageVisitor(_subject.getElementName());

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


	Node<IBinding> getNode(IBinding binding, JavaType kind) {
		String name = binding.getName();
		return getNode(name, binding, kind);
	}


	Node<IBinding> getNode(String name, IBinding binding, JavaType kind) {
		Node<IBinding> node = _nodes.get(name);
		if (null == node) {
			node = new Node<IBinding>(name, kind);
			node.payload(binding);
			_nodes.put(name, node);
		}
		return node;
	}

	
	private List<Pattern> getClassExcludePattern() {
		if (_excludedClassPattern == null) {
			_excludedClassPattern = new ArrayList<Pattern>();
			for (String str : BykePlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PATTERN_EXCLUDES).split("\\s+")) {
				_excludedClassPattern.add(Pattern.compile(str));
			}
		}
		return _excludedClassPattern;
	}

	private boolean ignoreClass(String qualifiedClassName) {
		for (Pattern pattern : getClassExcludePattern())
			if (pattern.matcher(qualifiedClassName).matches())
				return true;

		return false;
	}


	class PackageVisitor extends ASTVisitor {
		private Node<IBinding> _currentNode;

		private String _currentPackageName;
		private String _topLevelPackage;

		public PackageVisitor(String topLevelPackage) {
			_topLevelPackage = topLevelPackage;
		}

		
		
		@Override
		public boolean visit(ImportDeclaration node) {
			return false; //We don't have a current node yet.
		}



		@SuppressWarnings("unchecked")
		private boolean visitType(AbstractTypeDeclaration node) {
			Node<IBinding> saved = _currentNode;
			String savedPackage = _currentPackageName;
			ITypeBinding binding = node.resolveBinding();
			if (ignoreClass(binding.getQualifiedName())) return false;
			_currentNode = getNode2(binding);
			_currentPackageName = binding.getPackage().getName();
			addProvider(binding.getSuperclass());
			for (ITypeBinding superItf : binding.getInterfaces()) {
				addProvider(superItf);
			}
			visitList(node.bodyDeclarations());
			_currentNode = saved;
			_currentPackageName = savedPackage;
			return false;
		}

		@Override
		public boolean visit(AnnotationTypeDeclaration node) {
			return visitType(node);
		}

		@Override
		public boolean visit(EnumDeclaration node) {
			return visitType(node);
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			return visitType(node);
		}

		private Node<IBinding> getNode2(ITypeBinding binding) {
			
			ITypeBinding declaringClass = binding.getDeclaringClass();
			if (null != declaringClass) return getNode2(declaringClass);
			
			JavaType type = JavaType.valueOf(binding);
			return getNode(binding, type);
		}

		private void visitList(List<ASTNode> l) {
			for (Iterator<ASTNode> iter = l.iterator(); iter.hasNext();) {
				ASTNode child = iter.next();
				child.accept(this);
			}
		}

		@Override
		public boolean visit(org.eclipse.jdt.core.dom.QualifiedType node) {
			addProvider(node.resolveBinding());
			return true;
		}

		
		
		@Override
		public void preVisit(ASTNode node) {
			super.preVisit(node);
		}

		@Override
		public boolean visit(QualifiedName node) {
			IBinding b = node.resolveBinding();
			if (b instanceof IVariableBinding)
				addProviderOf((IVariableBinding)b);
			return true;
		}

		@Override
		public boolean visit(SimpleType node) {
			addProvider(node.resolveBinding());
			return true;
		}

		@Override
		public boolean visit(SimpleName node) {
			IBinding b = node.resolveBinding();
			if (b instanceof ITypeBinding)
				addProvider((ITypeBinding)b);
			else if (b instanceof IVariableBinding)
				addProviderOf((IVariableBinding)b);
			return true;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding binding = node.resolveMethodBinding();
			if (binding == null)
				return true;
			addProvider(binding.getDeclaringClass());
			return true;
		}

		@Override
		public boolean visit(ClassInstanceCreation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		@Override
		public boolean visit(FieldAccess node) {
			addProviderOf(node.resolveFieldBinding());
			return true;
		}

		
		private void addProviderOf(IVariableBinding binding) {
			if (binding == null) return;
			addProvider(binding.getDeclaringClass());
		}

		
		private void addProvider(ITypeBinding type) {
			if (null == type) return;
			if (type.isArray()) type = type.getElementType();
			if (type.isPrimitive() || type.isWildcardType()) return;
			if (type.isTypeVariable()) {
				for (ITypeBinding subType : type.getTypeBounds())
					addProvider(subType);
				return;
			}
			if (type.getQualifiedName().equals("")) return; // TODO: Check why this happens.

			String packageName = type.getPackage().getName();
			if (!packageName.startsWith(_topLevelPackage)) return;

			if (!packageName.equals(_currentPackageName)) {
				_currentNode.addProvider(getNode(type.getPackage(), JavaType.PACKAGE));
				return;
			}
			if (type.isParameterizedType()) { // if Map<K,V>
				for (ITypeBinding subtype : type.getTypeArguments()) { // <K,V>
					if (ignoreClass(subtype.getQualifiedName())) continue;
					addProvider(subtype);
				}
				final ITypeBinding erasure = type.getErasure();
				if (ignoreClass(erasure.getQualifiedName())) return;
				_currentNode.addProvider(getNode2(erasure));
			} else {
				if (ignoreClass(type.getQualifiedName())) return;
				_currentNode.addProvider(getNode2(type));
			}
		}
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