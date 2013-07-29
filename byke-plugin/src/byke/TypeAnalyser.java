package byke;

import static byke.JavaType.FIELD;
import static byke.JavaType.METHOD;

import java.util.HashMap;
import java.util.Map;

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
	private Node<IBinding> variableBeingAssigned;
	
	private Map<ASTNode, Node<IBinding>> visitLater = new HashMap<ASTNode, Node<IBinding>>();


	TypeAnalyser(TypeDeclaration node, NodeAccumulator nodeAccumulator, ITypeBinding type) {
		this.nodeAccumulator = nodeAccumulator;
		this.type = type;

		for (Object decl : node.bodyDeclarations())
			((ASTNode)decl).accept(this);
		
		for(Map.Entry<ASTNode, Node<IBinding>> visit : visitLater.entrySet()) {
			visit.getKey().accept(this);
			
			if(visit.getKey() instanceof MethodDeclaration) {
				Node<IBinding> methodNodeGiven = methodNodeGiven(((MethodDeclaration)visit.getKey()).resolveBinding());
				visit.getValue().addProviders(methodNodeGiven.providers());
				nodeAccumulator.remove(methodNodeGiven);
			}
		}
		
		
		LocalVariableFolder.fold(this.nodeAccumulator);
	}

	@Override
	public boolean visit(TypeDeclaration ignored) { // Nested type?
		return false;
	}

	@Override
	public boolean visit(final MethodDeclaration method) {
		if (methodBeingVisited != null) {
			visitLater.put(method, methodBeingVisited);
			return false;
		}
		
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
	public void endVisit(VariableDeclarationFragment field) {
		variableBeingAssigned = null;
	}

	@Override
	public boolean visit(Initializer node) {
		if (methodBeingVisited != null) {
			visitLater.put(node, methodBeingVisited);
			return false;
		}
		
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
		methodBeingVisited = producer.produce();
		return true;
	}

	private void exitMethod() {
		methodBeingVisited = null;
	}

	@Override
	public boolean visit(VariableDeclarationFragment variable) {
		IVariableBinding b = variable.resolveBinding();
		variableBeingAssigned = variableNodeGiven(b);
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

		if (lhs instanceof FieldAccess)
			((FieldAccess)lhs).getExpression().accept(this); // Needs test

		if (lhs instanceof QualifiedName)
			((QualifiedName)lhs).getQualifier().accept(this); // Needs test
		
		addProvider(variableNodeGiven(lhs));

		handleRightHandSide(assignment);
		
		return false;
	}

	private void handleRightHandSide(Assignment assignment) {
		Node<IBinding> variableLhs = variableNodeGiven(assignment.getLeftHandSide());
		final Expression rhs = assignment.getRightHandSide();
		
		if (rhs instanceof SimpleName) {
			Node<IBinding> variableRhs = variableNodeGiven(rhs);
			variableLhs.addProviders(variableRhs.providers());
		}
		
		if (rhs instanceof MethodInvocation)
			variableLhs.addProvider(methodNodeGiven(rhs));
		
		assignment.getRightHandSide().accept(this);
	}

	private void addProviderMethod(IMethodBinding method) {
		if (method == null) return;
		if (method.getDeclaringClass().getErasure() != type) return;
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

	private Node<IBinding> methodNodeGiven(Expression expression) {
		IMethodBinding b = null;
		
		if (expression instanceof MethodInvocation)
			b = (IMethodBinding)(((MethodInvocation)expression).getName().resolveBinding());
		
		return methodNodeGiven(b);
	}
	
	private Node<IBinding> methodNodeGiven(IMethodBinding methodBinding) {
		return nodeAccumulator.produceNode(methodBinding, METHOD);
	}

	private Node<IBinding> variableNodeGiven(Expression expression) {
		IVariableBinding b = null;
		
		if (expression instanceof SimpleName)
			b = (IVariableBinding)(((SimpleName)expression).resolveBinding());

		if (expression instanceof FieldAccess)
			b = (IVariableBinding)(((FieldAccess)expression).resolveFieldBinding());

		if (expression instanceof QualifiedName)
			b = (IVariableBinding)(((QualifiedName)expression).resolveBinding());

		return variableNodeGiven(b);
	}
	
	private Node<IBinding> variableNodeGiven(IVariableBinding variableBinding) {
		return nodeAccumulator.produceNode(variableBinding, FIELD);
	}

	private void addProvider(Node<IBinding> provider) {
		if (methodBeingVisited != null) methodBeingVisited.addProvider(provider);
		if (variableBeingAssigned != null) variableBeingAssigned.addProvider(provider);
	}

}