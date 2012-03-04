package byke;

import org.eclipse.jdt.core.dom.IBinding;

import byke.dependencygraph.Node;


public interface NodeAccumulator {

	Node<IBinding> produceNode(IBinding binding, JavaType kind);

	Node<IBinding> produceNode(String name, JavaType kind);

}