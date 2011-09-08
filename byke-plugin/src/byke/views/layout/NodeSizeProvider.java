package byke.views.layout;

import byke.dependencygraph.Node;


public interface NodeSizeProvider {

	public FloatRectangle sizeGiven(Node<?> node);

}
