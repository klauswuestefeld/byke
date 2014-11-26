package byke.views.layout.ui;

import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.IContainer;

import byke.views.cache.NodeFigure;

public class NonMovableNode extends GraphNode {

	public NonMovableNode(IContainer graphModel, int style, Object data, String name) {
		super(graphModel, style);
		setData(data);
		
		setText(insert(name, "\n", 40));
	}

	
	public NodeFigure subGraph() {
		return (NodeFigure)getData();
	}

	
//	private static String insert(String text, String insert, int period) {
//    Pattern p = Pattern.compile("(.{" + period + "})", Pattern.DOTALL);
//    Matcher m = p.matcher(text);
//    return m.replaceAll("$1" + insert);
//	}
	
	private static String insert(String text, String insert, int period) {
	    StringBuilder builder = new StringBuilder(text.length() + insert.length() * (text.length() / period) + 1);

	    int index = 0;
	    String prefix = "";
	    while (index < text.length()) {
	        builder.append(prefix);
	        prefix = insert;
	        builder.append(text.substring(index, Math.min(index + period, text.length())));
	        index += period;
	    }
	    return builder.toString();
	}
	
}
