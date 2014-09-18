package byke.views.layout.ui;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.IContainer;

import byke.dependencygraph.Node;

public class NonMovableNode<T> extends GraphNode {

	private static final double MIN_HEIGHT = 25;
	private static final double MIN_WIDTH = 80;

	public NonMovableNode(IContainer graphModel, int style, Object data) {
		super(graphModel, style);
		setData(data);
		setText(data.toString().replaceAll(Pattern.quote("["), "").replaceAll(Pattern.quote("]"), ""));
		setSize(MIN_WIDTH * ((Collection<?>)data).size(), MIN_HEIGHT * ((Collection<?>)data).size());
	}

	public Collection<Node<T>> internalNodes() {
		return (Collection<Node<T>>)getData();
	}
}
