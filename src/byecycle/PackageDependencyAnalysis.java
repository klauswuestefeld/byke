//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.

package byecycle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byecycle.dependencygraph.Node;
import byecycle.preferences.PreferenceConstants;

public class PackageDependencyAnalysis {

	public static final String PACKAGE = "package";

	public static final String CLASS = "class";

	public static final String INTERFACE = "interface";

	private final Map<String, Node<IBinding>> _nodes = new HashMap<String, Node<IBinding>>();
	
	private List<String> _excludedPackages;

	public PackageDependencyAnalysis(ICompilationUnit[] compilationUnits,
			IProgressMonitor monitor) throws JavaModelException {

		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}

		ASTParser parser = ASTParser.newParser(AST.JLS3);

		DependencyVisitor visitor = new DependencyVisitor();

		monitor.beginTask("dependency analysis", compilationUnits.length);
		for (int i = 0; i < compilationUnits.length; i++) {
			ICompilationUnit each = compilationUnits[i];
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

	public Collection<Node<IBinding>> dependencyGraph() {
		return _nodes.values();
	}

	private Node getNode(IBinding binding, String nodeName, String kind) {
		Node<IBinding> node = _nodes.get(getBindingKey(binding));
		if (null == node) {
			node = new Node<IBinding>(nodeName, kind);
			node.payload(binding);
			_nodes.put(getBindingKey(binding), node);
		}
		return node;
	}

	private String getBindingKey(IBinding binding) {
		return binding.getKey();
	}

	private boolean ignorePackage(String packageName) {
		return getExcludedPackages().contains(packageName);
	}

	private List<String> getExcludedPackages() {
		if (_excludedPackages == null) {
			_excludedPackages = Arrays.asList(ByecyclePlugin.getDefault()
					.getPreferenceStore().getString(
							PreferenceConstants.P_PACKAGE_EXCLUDES).split(" "));
		}
		return _excludedPackages;
	}

	class DependencyVisitor extends ASTVisitor {

		Node _currentNode;

		private String _currentPackageName;

		public boolean visit(TypeDeclaration node) {
			Node saved = _currentNode;
			String savedPackage = _currentPackageName;

			ITypeBinding binding = node.resolveBinding();
			_currentNode = getNode2(binding);
			_currentPackageName = binding.getPackage().getName();

			// SpreadingOut -extends-> DistanceBasedForce -implements-> Force

			System.out.println(binding.getSuperclass());
			addProvider(binding.getSuperclass());
			for (ITypeBinding superItf : binding.getInterfaces()) {
				addProvider(superItf);
			}

			visitList(node.bodyDeclarations());

			_currentNode = saved;
			_currentPackageName = savedPackage;
			return false;
		}

		private Node getNode2(ITypeBinding binding) {
			return getNode(binding, binding.getQualifiedName(), binding
					.isInterface() ? INTERFACE : CLASS);
		}

		private void visitList(List l) {
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				ASTNode child = (ASTNode) iter.next();
				child.accept(this);
			}
		}

		public boolean visit(org.eclipse.jdt.core.dom.QualifiedType node) {
			addProvider(node.resolveBinding());
			return true;
		}

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
				return; // TODO: Check why this happens.

			String packageName = type.getPackage().getName();
			if (ignorePackage(_currentPackageName)) {
				_currentNode.addProvider(getNode2(type));
				return;
			}
			if (packageName.equals("java.lang"))
				return;
			_currentNode.addProvider(getNode(type.getPackage(), packageName,
					PACKAGE));
		}

	}
}