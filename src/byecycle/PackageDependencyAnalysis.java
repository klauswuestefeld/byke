package byecycle;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;

import byecycle.dependencygraph.Node;


public class PackageDependencyAnalysis {

	private final Map _nodes = new HashMap();
	
	private final Node _root;

	public PackageDependencyAnalysis(IPackageFragment pkg, IProgressMonitor monitor) throws JavaModelException {
		
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		
		_root = getNode(pkg.getElementName());

		ASTParser parser = ASTParser.newParser(AST.JLS2);

		DependencyVisitor visitor = new DependencyVisitor();
		ICompilationUnit[] units = pkg.getCompilationUnits();
		
		monitor.beginTask("package analysis", units.length);
		for (int i = 0; i < units.length; i++) {
			ICompilationUnit each = units[i];
			parser.setResolveBindings(true);
			parser.setSource(each);
			
			monitor.subTask(each.getElementName());
			
			CompilationUnit node = (CompilationUnit) parser.createAST(monitor); 
			node.accept(visitor);
			
			monitor.worked(1);
			if (monitor.isCanceled()) {
				break;
			}
		}
	}
	
	public Node root() {
		return _root;
	}
	
	public Map nodes() {
		return _nodes;
	}

	private Node getNode(String packageName) {
		Node node = (Node)_nodes.get(packageName);
		if (null == node) {
			node = new Node(packageName);
			_nodes.put(packageName, node);
		}
		return node;
	}

	class DependencyVisitor extends ASTVisitor {
		
		public boolean visit(SimpleType node) {
			addProvider(node.resolveBinding());
			return true;
		}

		public boolean visit(Expression node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		public boolean visit(MethodInvocation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		public boolean visit(ClassInstanceCreation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		private void addProvider(ITypeBinding type) {
			if (null == type)
				return;
			if (type.isArray())
				type = type.getElementType();
			if (type.isPrimitive())
				return;
			if (type.getQualifiedName().equals(""))
				return; //TODO: Check why this happens.
			_root.addProvider(getNode(type.getPackage().getName()));
		}

	}
}