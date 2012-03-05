package byke;

import org.eclipse.jdt.core.dom.IBinding;

import byke.dependencygraph.Node;

public class LocalVariableNode extends Node<IBinding> {

	LocalVariableNode() {
		super("local variable");
	}
	
}
