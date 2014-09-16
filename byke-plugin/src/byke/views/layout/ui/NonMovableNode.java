package byke.views.layout.ui;

import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.IContainer;

public class NonMovableNode extends GraphNode {

	public NonMovableNode(IContainer graphModel, int style, Object data) {
		super(graphModel, style);
		setData(data);
		setText(data.toString());
	}

}
