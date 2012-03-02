package byke;

import static byke.JavaType.FIELD;
import static byke.JavaType.METHOD;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byke.dependencygraph.Node;

class TypeAnalyser extends ASTVisitor {
	private final DependencyAnalysis _dependencyAnalysis;
	private Node<IBinding> _currentNode;
	private final ITypeBinding _type;

	
	TypeAnalyser(DependencyAnalysis dependencyAnalysis, ITypeBinding binding) {
		_dependencyAnalysis = dependencyAnalysis;
		_type = binding;
	}

	
	@Override
	public boolean visit(TypeDeclaration node) {
		return false;
	}

	
	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding methodBinding = node.resolveBinding();
		_currentNode = methodNode(methodBinding);
		return true;
	}

	
	@Override
	public boolean visit(Initializer node) {
		_currentNode = _dependencyAnalysis.getNode("{initializer}", null, METHOD);
		return true;
	}


	@Override
	public boolean visit(MethodInvocation node) {
		addProvider(node.resolveMethodBinding());
		return true;
	}

	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		addProvider(node.resolveConstructorBinding());
		return true;
	}

	
	@Override
	public boolean visit(FieldAccess node) {
		addProvider(node.resolveFieldBinding());
		return true;
	}

	
	@Override
	public boolean visit(Assignment node) {
		Expression lhs = node.getLeftHandSide();
		
		if (lhs instanceof SimpleName) {
			IVariableBinding b = (IVariableBinding)(((SimpleName)lhs).resolveBinding());
			addDependent(b);
		}
		
		if (lhs instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess)lhs;
			IVariableBinding b = fieldAccess.resolveFieldBinding();
			addDependent(b);
			fieldAccess.getExpression().accept(this); //Needs test
		}

		if (lhs instanceof QualifiedName) {
			QualifiedName fieldAccess = (QualifiedName)lhs;
			IVariableBinding b = (IVariableBinding)fieldAccess.resolveBinding();
			addDependent(b);
			fieldAccess.getQualifier().accept(this); //Needs test
		}
		
		node.getRightHandSide().accept(this); //Needs test
		return false;
	}


	private void addDependent(IVariableBinding b) {
		System.out.println("Binding " + b.getClass() + " " + b);
		
		Node<IBinding> field = fieldNode(b);
		field.addProvider(_currentNode);
	}

	
	private void addProvider(IMethodBinding binding) {
		if (binding == null) return;
		if (binding.getDeclaringClass() != _type) return;
		addProvider(methodNode(binding));
	}


	private void addProvider(IVariableBinding binding) {
		if (binding == null) return;
		if (binding.getDeclaringClass() != _type) return;
		addProvider(fieldNode(binding));
	}


	private Node<IBinding> methodNode(IMethodBinding methodBinding) {
		return _dependencyAnalysis.getNode(methodBinding, METHOD);
	}
	private Node<IBinding> fieldNode(IVariableBinding fieldBinding) {
		return _dependencyAnalysis.getNode(fieldBinding, FIELD);
	}


	private void addProvider(Node<IBinding> provider) {
		if (_currentNode == null) {
			System.out.println("Current Node Null while adding provider: " + provider);
			return;
		}
		_currentNode.addProvider(provider);
	}

	
	@Override
	public void preVisit(ASTNode node) {
		System.out.println(node.getClass() + ": " + node);
		super.preVisit(node);
	}
	

}