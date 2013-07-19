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

import sneer.foundation.lang.Producer;
import byke.dependencygraph.Node;


class TypeAnalyser extends ASTVisitor {

	private final ITypeBinding type;
	private final NodeAccumulator nodeAccumulator;

	private Node<IBinding> methodBeingVisited;


	TypeAnalyser(TypeDeclaration node, NodeAccumulator nodeAccumulator, ITypeBinding type) {
		this.nodeAccumulator = nodeAccumulator;
		this.type = type;

		for (Object decl : node.bodyDeclarations())
			((ASTNode)decl).accept(this);

		LocalVariableFolder.fold(this.nodeAccumulator);
	}

	@Override
	public boolean visit(TypeDeclaration ignored) { // Nested type?
		return false;
	}

	@Override
	public boolean visit(final MethodDeclaration method) {
		return enterMethod(new Producer<Node<IBinding>>() {
			@Override
			public Node<IBinding> produce() {
				return methodNodeGiven(method.resolveBinding());
			}
		});
	}

	@Override
	public void endVisit(MethodDeclaration method) {
		exitMethod();
	}

	@Override
	public boolean visit(Initializer node) {
		return enterMethod(new Producer<Node<IBinding>>() {
			@Override
			public Node<IBinding> produce() {
				return nodeAccumulator.produceNode("<init>", METHOD);
			}
		});
	}

	@Override
	public void endVisit(Initializer node) {
		exitMethod();
	}

	private boolean enterMethod(Producer<Node<IBinding>> producer) {
		if (methodBeingVisited != null) {
			System.out.println("Method inside method '" + methodBeingVisited + "' will not be visited.");
			return false;
		}
		methodBeingVisited = producer.produce();
		return true;
	}

	private void exitMethod() {
		methodBeingVisited = null;
	}

	@Override
	public boolean visit(VariableDeclarationFragment variable) {
		addProvider(variableNodeGiven(variable.resolveBinding()));
		return true;
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
		if (simpleName.resolveBinding() instanceof IVariableBinding) addProviderVariable((IVariableBinding)simpleName.resolveBinding());
		return true;
	}

	@Override
	public boolean visit(Assignment assignment) {
		final Expression lhs = assignment.getLeftHandSide();
		boolean shouldVisit = false;

		if (lhs instanceof SimpleName) {
			IVariableBinding b = (IVariableBinding)(((SimpleName)lhs).resolveBinding());
			addProvider(variableNodeGiven(b));
			shouldVisit = true;
		}

		if (lhs instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess)lhs;
			IVariableBinding b = fieldAccess.resolveFieldBinding();
			addProvider(variableNodeGiven(b));
			shouldVisit = true;
			if (shouldVisit) fieldAccess.getExpression().accept(this); // Needs test
		}

		if (lhs instanceof QualifiedName) {
			QualifiedName fieldAccess = (QualifiedName)lhs;
			IVariableBinding b = (IVariableBinding)fieldAccess.resolveBinding();
			addProvider(variableNodeGiven(b));
			shouldVisit = true;
			if (shouldVisit) fieldAccess.getQualifier().accept(this); // Needs test
		}
		
		handleRightHandSide(assignment);

		if (shouldVisit) assignment.getRightHandSide().accept(this); // Needs test
		
		return false;
	}

	private void handleRightHandSide(Assignment assignment) {
		final Expression lhs = assignment.getLeftHandSide();
		final Expression rhs = assignment.getRightHandSide();
		
		if (rhs instanceof MethodInvocation) {
			IVariableBinding b = (IVariableBinding)(((SimpleName)lhs).resolveBinding());
			Node<IBinding> variable = variableNodeGiven(b);
			
			IMethodBinding methodBinding = (IMethodBinding)(((MethodInvocation)rhs).getName().resolveBinding());
			variable.addProvider(methodNodeGiven(methodBinding));
		}		
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
		if (methodBeingVisited != null) methodBeingVisited.addProvider(provider);
	}

	@Override
	public void preVisit(ASTNode node) {
		// System.out.println(node.getClass() + ": " + node);
		super.preVisit(node);
	}

}