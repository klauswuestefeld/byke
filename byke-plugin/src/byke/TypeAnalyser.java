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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import byke.dependencygraph.Node;

class TypeAnalyser extends ASTVisitor {
	
	private final NodeProducer _nodeProducer;
	private Node<IBinding> _currentMethod;
	private Node<IBinding> _currentField;
	private final ITypeBinding _type;

	
	TypeAnalyser(NodeProducer nodeProducer, ITypeBinding binding) {
		_nodeProducer = nodeProducer;
		_type = binding;
	}

	
	@Override
	public boolean visit(TypeDeclaration ignored) { //Nested type?
		return false;
	}

	
	@Override
	public boolean visit(MethodDeclaration method) {
		setCurrentMethod(methodGiven(method.resolveBinding()));
		return true;
	}
	@Override
	public void endVisit(MethodDeclaration method) {
		_currentMethod = null;
	}

	
	@Override
	public boolean visit(Initializer node) {
		setCurrentMethod(_nodeProducer.produceNode("{initializer}", METHOD));
		return true;
	}
	@Override
	public void endVisit(Initializer node) {
		_currentMethod = null;
	}
	
	
	private void setCurrentMethod(Node<IBinding> methodNode) {
		if (_currentMethod != null) throw new UnsupportedOperationException("Visiting method inside method.");
		_currentMethod = methodNode;
	}


	@Override
	public boolean visit(VariableDeclarationFragment field) {
		if (_currentField != null) throw new UnsupportedOperationException("Visiting field inside field.");
		_currentField = fieldGiven(field.resolveBinding());
		return true;
	}
	@Override
	public void endVisit(VariableDeclarationFragment field) {
		_currentField = null;
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
	public boolean visit(FieldAccess fieldAccess) {
		addProvider(fieldAccess.resolveFieldBinding());
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
		
		Node<IBinding> field = fieldGiven(b);
		field.addProvider(_currentMethod);
	}

	
	private void addProvider(IMethodBinding binding) {
		if (binding == null) return;
		if (binding.getDeclaringClass() != _type) return;
		addProvider(methodGiven(binding));
	}


	private void addProvider(IVariableBinding binding) {
		if (binding == null) return;
		if (binding.getDeclaringClass() != _type) return;
		addProvider(fieldGiven(binding));
	}


	private Node<IBinding> methodGiven(IMethodBinding methodBinding) {
		return _nodeProducer.produceNode(methodBinding, METHOD);
	}
	private Node<IBinding> fieldGiven(IVariableBinding fieldBinding) {
		return _nodeProducer.produceNode(fieldBinding, FIELD);
	}


	private void addProvider(Node<IBinding> provider) {
		if (_currentMethod != null)
			_currentMethod.addProvider(provider);
		if (_currentField != null)
			_currentField.addProvider(provider);
	}

	
	@Override
	public void preVisit(ASTNode node) {
		System.out.println(node.getClass() + ": " + node);
		super.preVisit(node);
	}
	

}