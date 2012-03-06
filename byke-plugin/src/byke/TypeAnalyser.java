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
	
	private final ITypeBinding type;
	private final NodeAccumulator nodeAccumulator;

	private Node<IBinding> methodBeingVisited;
	private Node<IBinding> variableBeingAssigned;
	
	
	TypeAnalyser(TypeDeclaration node, NodeAccumulator nodeAccumulator, ITypeBinding type) {
		this.nodeAccumulator = nodeAccumulator;
		this.type = type;
		
		for (Object decl : node.bodyDeclarations())
			((ASTNode)decl).accept(this);
		
		LocalVariableFolder.fold(this.nodeAccumulator);
	}


	@Override
	public boolean visit(TypeDeclaration ignored) { //Nested type?
		return false;
	}

	
	@Override
	public boolean visit(MethodDeclaration method) {
		enterMethod(methodNodeGiven(method.resolveBinding()));
		return true;
	}
	@Override
	public void endVisit(MethodDeclaration method) {
		exitMethod();
	}


	@Override
	public boolean visit(Initializer node) {
		enterMethod(nodeAccumulator.produceNode("{initializer}", METHOD));
		return true;
	}
	@Override
	public void endVisit(Initializer node) {
		exitMethod();
	}
	

	private void enterMethod(Node<IBinding> methodNode) {
		if (methodBeingVisited != null) throw new UnsupportedOperationException("Visiting method inside method.");
		methodBeingVisited = methodNode;
	}
	private void exitMethod() {
		methodBeingVisited = null;
	}


	@Override
	public boolean visit(VariableDeclarationFragment variable) {
		IVariableBinding b = variable.resolveBinding();
		enterVariableAssignment(b);
		return true;
	}
	@Override
	public void endVisit(VariableDeclarationFragment field) {
		variableBeingAssigned = null;
	}

	
	private void enterVariableAssignment(IVariableBinding variable) {
		if (variableBeingAssigned != null) throw new UnsupportedOperationException("Visiting field inside field.");
		variableBeingAssigned = variableNodeGiven(variable);
		addDependent(variableBeingAssigned);
	}

	
	@Override
	public boolean visit(MethodInvocation node) {
		addProviderMethod(node.resolveMethodBinding());
		return true;
	}

	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		addProviderMethod(node.resolveConstructorBinding());
		return true;
	}

	
	@Override
	public boolean visit(FieldAccess fieldAccess) {
		addProviderVariable(fieldAccess.resolveFieldBinding());
		return true;
	}

	
	@Override
	public boolean visit(SimpleName simpleName) {
		if (simpleName.resolveBinding() instanceof IVariableBinding)
			addProviderVariable((IVariableBinding)simpleName.resolveBinding());
		return true;
	}

	
	@Override
	public boolean visit(Assignment node) {
		Expression lhs = node.getLeftHandSide();
		
		if (lhs instanceof SimpleName) {
			IVariableBinding b = (IVariableBinding)(((SimpleName)lhs).resolveBinding());
			enterVariableAssignment(b);
		}
		
		if (lhs instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess)lhs;
			IVariableBinding b = fieldAccess.resolveFieldBinding();
			enterVariableAssignment(b);
			fieldAccess.getExpression().accept(this); //Needs test
		}

		if (lhs instanceof QualifiedName) {
			QualifiedName fieldAccess = (QualifiedName)lhs;
			IVariableBinding b = (IVariableBinding)fieldAccess.resolveBinding();
			enterVariableAssignment(b);
			fieldAccess.getQualifier().accept(this); //Needs test
		}
		
		node.getRightHandSide().accept(this); //Needs test
		return false;
	}
	@Override
	public void endVisit(Assignment node) {
		variableBeingAssigned = null;
	}
	
	
	private void addProviderMethod(IMethodBinding method) {
		if (method == null) return;
		if (method.getDeclaringClass() != type) return;
		addProvider(methodNodeGiven(method));
	}
	private void addProviderVariable(IVariableBinding variable) {
		if (variable == null) return;
		if (!isFromSubjectType(variable)) return;
		addProvider(variableNodeGiven(variable));
	}


	private boolean isFromSubjectType(IVariableBinding variable) {
		if (LocalVariableFolder.isLocalVariable(variable)) return true;
		return variable.getDeclaringClass() == type;
	}


	private Node<IBinding> methodNodeGiven(IMethodBinding methodBinding) {
		return nodeAccumulator.produceNode(methodBinding, METHOD);
	}
	private Node<IBinding> variableNodeGiven(IVariableBinding variableBinding) {
		return nodeAccumulator.produceNode(variableBinding, FIELD);
	}


	private void addProvider(Node<IBinding> provider) {
		if (methodBeingVisited != null)
			methodBeingVisited.addProvider(provider);
		if (variableBeingAssigned != null)
			variableBeingAssigned.addProvider(provider);
	}
	private void addDependent(Node<IBinding> dependent) {
		if (methodBeingVisited != null)
			dependent.addProvider(methodBeingVisited);
	}

	

	
	@Override
	public void preVisit(ASTNode node) {
//		System.out.println(node.getClass() + ": " + node);
		super.preVisit(node);
	}
	

}