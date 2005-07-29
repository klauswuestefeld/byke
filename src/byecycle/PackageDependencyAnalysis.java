//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.

package byecycle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import byecycle.dependencygraph.Node;
import byecycle.preferences.PreferenceConstants;

public class PackageDependencyAnalysis {
    @Deprecated
    public static final String PACKAGE = "package";

    @Deprecated
    public static final String CLASS = "class";

    @Deprecated
    public static final String INTERFACE = "interface";

    private final Map<String, Node<IBinding>> _nodes = new HashMap<String, Node<IBinding>>();

    private List<String> _excludedPackages;

    // private Set<String> _currentPackages = new HashSet<String>();

    public PackageDependencyAnalysis(ICompilationUnit[] compilationUnits,
            IProgressMonitor monitor) throws JavaModelException {

        if (null == monitor) {
            monitor = new NullProgressMonitor();
        }

        ASTParser parser = ASTParser.newParser(AST.JLS3);

        DependencyVisitor visitor = new DependencyVisitor();

        monitor.beginTask("dependency analysis", compilationUnits.length);

        /*
         * for (ICompilationUnit each : compilationUnits) { // READ ALL SELECTED
         * PACKAGE FIRST~! IPackageDeclaration[] packages =
         * each.getPackageDeclarations(); if (packages != null) { for
         * (IPackageDeclaration unit : packages) {
         * _currentPackages.add(unit.getElementName()); } } }
         */

        for (ICompilationUnit each : compilationUnits) {
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
        String key = getBindingKey(binding);
        Node<IBinding> node = _nodes.get(key);
        if (null == node) {
            node = new Node<IBinding>(nodeName, kind);
            node.payload(binding);
            _nodes.put(key, node);
        }
        return node;
    }

    private Node getNode(IBinding binding, String nodeName, JavaType kind) {
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
        return binding.getKey();
    }

    private boolean ignorePackage(String packageName) {
        return "java.lang".equals(packageName)
                || getExcludedPackages().contains(packageName);
    }

    // private boolean selectedPackage(ITypeBinding type) {
    // return _currentPackages.contains(type.getPackage().getName());
    // }

    private List<String> getExcludedPackages() {
        if (_excludedPackages == null) {
            _excludedPackages = Arrays.asList(ByecyclePlugin.getDefault()
                    .getPreferenceStore().getString(
                            PreferenceConstants.P_PACKAGE_EXCLUDES).split(
                            "\\s+"));
        }
        return _excludedPackages;
    }

    class DependencyVisitor extends ASTVisitor {

        private Node _currentNode;

        private String _currentPackageName;

        private boolean visit0(AbstractTypeDeclaration node) {
            Node saved = _currentNode;
            String savedPackage = _currentPackageName;

            ITypeBinding binding = node.resolveBinding();
            _currentNode = getNode2(binding);
            _currentPackageName = binding.getPackage().getName();

            // SpreadingOut -extends-> DistanceBasedForce -implements-> Force

            // System.out.println(binding.getSuperclass());
            addProvider(binding.getSuperclass());
            for (ITypeBinding superItf : binding.getInterfaces()) {
                addProvider(superItf);
            }

            visitList(node.bodyDeclarations());

            _currentNode = saved;
            _currentPackageName = savedPackage;
            return false;

        }

        public boolean visit(EnumDeclaration node) {
            return visit0(node);
        }

        public boolean visit(TypeDeclaration node) {
            return visit0(node);
        }

        private Node getNode2(ITypeBinding binding) {
            JavaType type = JavaType.valueOf(binding);
            return getNode(binding, binding.getQualifiedName(), type);
            // return getNode(binding, binding.getQualifiedName(), binding
            // .isInterface() ? INTERFACE : CLASS);
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

        private boolean isSelectedPackage(String packageName) {
            return packageName.equals(_currentPackageName);
        }

        private void addProvider(ITypeBinding type) {
            if (null == type)
                return;
            if (type.isArray())
                type = type.getElementType();
            if (type.isPrimitive() || type.isWildcardType())
                return;
            if (type.isTypeVariable()) {
                for (ITypeBinding subType : type.getTypeBounds()) {
                    addProvider(subType);
                }
                return;
            }
            if (type.getQualifiedName().equals(""))
                return; // TODO: Check why this happens.

            String packageName = type.getPackage().getName();

            if (isSelectedPackage(packageName)) {
                if (type.isParameterizedType()) { // if Map<K,V>
                    for (ITypeBinding subtype : type.getTypeArguments()) { // <K,V>
                        addProvider(subtype);
                    }
                    final ITypeBinding erasure = type.getErasure();
                    _currentNode.addProvider(getNode2(erasure));
                } else {
                    _currentNode.addProvider(getNode2(type));
                }
                return;
            }
            if (ignorePackage(packageName))
                return;
            _currentNode.addProvider(getNode(type.getPackage(), packageName,
                    JavaType.PACKAGE));
        }

    }
}