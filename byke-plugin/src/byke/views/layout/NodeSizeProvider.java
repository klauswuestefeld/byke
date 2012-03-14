package byke.views.layout;

import org.eclipse.draw2d.geometry.Rectangle;

import byke.dependencygraph.Node;


public interface NodeSizeProvider {

	public Rectangle sizeGiven(Node<?> node);

}
