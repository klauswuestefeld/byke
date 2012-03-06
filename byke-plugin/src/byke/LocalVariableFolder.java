package byke;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import byke.dependencygraph.Node;

class LocalVariableFolder {

	static void fold(NodeAccumulator nodeAccumulator) {
		for (Node<IBinding> node : nodeAccumulator.nodes())
			if (isLocalVariable(node))
				nodeAccumulator.remove(node);
			else
				new LocalVariableFold(node);
	}

	
	static boolean isLocalVariable(Node<IBinding> node) {
		return isLocalVariable(node.payload());
	}


	static boolean isLocalVariable(IBinding binding) {
		if (!(binding instanceof IVariableBinding)) return false;
		return ((IVariableBinding)binding).getDeclaringClass() == null;
	}

	
	static private class LocalVariableFold {

		private final Node<IBinding> node;
		private Set<Node<IBinding>> foldedProviders = new HashSet<Node<IBinding>>();
		
		private LocalVariableFold(Node<IBinding> node) {
			this.node = node;
			foldLocalProviders(node);
		}

		private void foldLocalProviders(Node<IBinding> toFold) {
			System.out.println("Field: " + toFold.payload());
			
			if (foldedProviders.contains(toFold)) return;
			foldedProviders.add(toFold);
			
			for (Node<IBinding> provider : toFold.providers().toArray(new Node[]{}))
				if (isLocalVariable(provider)) {
					node.removeProvider(provider);
					foldLocalProviders(provider);
				} else
					node.addProvider(provider);
		}
		
	}
	
}
