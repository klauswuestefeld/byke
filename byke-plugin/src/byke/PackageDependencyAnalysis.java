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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byke.dependencygraph.Node;
import byke.preferences.PreferenceConstants;


public class PackageDependencyAnalysis {
	private final Map<String, Node<IBinding>> _nodes = new HashMap<String, Node<IBinding>>();

	private List<Pattern> _excludedClassPattern;


	public PackageDependencyAnalysis(String topLevelPackage, ICompilationUnit[] compilationUnits, IProgressMonitor monitor) throws JavaModelException {
		if (monitor == null) monitor = new NullProgressMonitor();

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		DependencyVisitor visitor = new DependencyVisitor(topLevelPackage);

		monitor.beginTask("dependency analysis", compilationUnits.length);

		for (ICompilationUnit each : compilationUnits) {
			parser.setResolveBindings(true);
			parser.setSource(each);

			monitor.subTask(each.getElementName());

			CompilationUnit node = (CompilationUnit)parser.createAST(monitor);
			node.accept(visitor);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				break;
			}
		}
	}

	public Collection<Node<IBinding>> dependencyGraph() {
		return _nodes.values();
	}

	private Node<IBinding> getNode(IBinding binding, String nodeName, JavaType kind) {
		String key = getBindingKey(binding);
		Node<IBinding> node = _nodes.get(key);
		if (null == node) {
			node = new Node<IBinding>(nodeName, kind);
			node.payload(binding);
			_nodes.put(key, node);
		}
		return node;
	}

	private String getBindingKey(IBinding binding) {
		// return binding.getKey();
		return binding.getJavaElement().getElementName();
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
		for (Pattern pattern : getClassExcludePattern()) {
			if (pattern.matcher(qualifiedClassName).matches()) {
				return true;
			}
		}
		return false;
	}


	class DependencyVisitor extends ASTVisitor {
		private Node<IBinding> _currentNode;

		private String _currentPackageName;
		private String _topLevelPackage;

		public DependencyVisitor(String topLevelPackage) {
			_topLevelPackage = topLevelPackage;
		}

		@SuppressWarnings("unchecked")
		private boolean visit0(AbstractTypeDeclaration node) {
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
			return visit0(node);
		}

		@Override
		public boolean visit(EnumDeclaration node) {
			return visit0(node);
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			return visit0(node);
		}

		private Node<IBinding> getNode2(ITypeBinding binding) {
			
			ITypeBinding declaringClass = binding.getDeclaringClass();
			if (null != declaringClass) return getNode2(declaringClass);
			
			JavaType type = JavaType.valueOf(binding);
			return getNode(binding, binding.getQualifiedName(), type);
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
			System.out.println(node.getClass() + " " + node);
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
		public boolean visit(MethodInvocation node) {
			addProvider(node.resolveTypeBinding());
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
			addProvider(binding.getDeclaringClass());
		}

		
		private boolean isSelectedPackage(String packageName) {
			return packageName.equals(_currentPackageName);
		}

		private void addProvider(ITypeBinding type) {
			if (null == type) return;
			if (type.isArray()) type = type.getElementType();
			if (type.isPrimitive() || type.isWildcardType()) return;
			if (type.isTypeVariable()) {
				for (ITypeBinding subType : type.getTypeBounds()) {
					addProvider(subType);
				}
				return;
			}
			if (type.getQualifiedName().equals("")) return; // TODO: Check why this happens.

			String packageName = type.getPackage().getName();
			if (!packageName.startsWith(_topLevelPackage)) return;

			if (isSelectedPackage(packageName)) {
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
				return;
			}
			_currentNode.addProvider(getNode(type.getPackage(), packageName, JavaType.PACKAGE));
		}
	}
}