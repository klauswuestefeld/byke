package byke;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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

class PackageVisitor extends ASTVisitor {
	
	private final NodeProducer nodeProducer;

	private Node<IBinding> _currentNode;

	private String _currentPackageName;
	private String _topLevelPackage;

	
	public PackageVisitor(DependencyAnalysis dependencyAnalysis, String topLevelPackage) {
		nodeProducer = dependencyAnalysis;
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
		if (ExclusionPatterns.ignoreClass(binding.getQualifiedName()))
			return false;
		
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
		return nodeProducer.produceNode(binding, type);
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
			_currentNode.addProvider(nodeProducer.produceNode(type.getPackage(), JavaType.PACKAGE));
			return;
		}
		if (type.isParameterizedType()) { // if Map<K,V>
			for (ITypeBinding subtype : type.getTypeArguments()) { // <K,V>
				if (ExclusionPatterns.ignoreClass(subtype.getQualifiedName())) continue;
				addProvider(subtype);
			}
			final ITypeBinding erasure = type.getErasure();
			if (ExclusionPatterns.ignoreClass(erasure.getQualifiedName())) return;
			_currentNode.addProvider(getNode2(erasure));
		} else {
			if (ExclusionPatterns.ignoreClass(type.getQualifiedName())) return;
			_currentNode.addProvider(getNode2(type));
		}
	}
	
}