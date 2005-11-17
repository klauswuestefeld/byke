//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views.layout.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import byecycle.JavaType;
import byecycle.dependencygraph.Node;


public class NodeFigure<T> extends GraphFigure {

	public NodeFigure(Node<T> node) {
		_node = node;
	}

	private final Node<T> _node;
	//FIXME - tjennings - rather inefficient, some nodes have no dependencies and most
	//have far fewer than ArrayList's default capacity of ten.
	private final List<DependencyFigure> _dependencies = new ArrayList<DependencyFigure>();

	private Label label(String text, Image icon) {
		return icon == null ? new Label(" " + text, icon) : new Label(text, icon);
	}

	IFigure produceFigure() {
		IFigure result;
		
		String name = simplifiedName();
		final Image imageForNode = imageForNode(_node);
		if (name.length() < 20) {
			result = label(name, imageForNode);
		} else {
			result = new CompartmentFigure();
			int cut = (name.length() / 2) - 1;
			result.add(label(name.substring(0, cut), imageForNode));
			result.add(label(name.substring(cut), null));
		}
		result.setBorder(new LineBorder());
		result.setBackgroundColor(pastelColorDeterminedBy(name));
		result.setOpaque(true);
		IFigure tooltip = label(_node.name(), imageForNode);
		tooltip.setBorder(new LineBorder());
		tooltip.setBackgroundColor(pastelColorDeterminedBy(name));
		tooltip.setOpaque(true);
		result.setToolTip(tooltip);
		return result;
	}

	private String simplifiedName() {
		String result = _node.name();
		if (_node.kind2() == JavaType.PACKAGE) return result;
		return result.substring(result.lastIndexOf('.') + 1);
	}


	static class CompartmentFigure extends Figure {
		public CompartmentFigure() {
			ToolbarLayout layout = new ToolbarLayout();
			layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
			layout.setStretchMinorAxis(false);
			setLayoutManager(layout);
		}
	}

     // TODO: Change to somekind of ImageProvider pattern.
     // Difference provider for different type of <T>
	private static <T> Image imageForNode(Node<T> node) {
		String resourcename = node.kind2().getResourceName();
		try {
			return JavaUI.getSharedImages().getImage(resourcename);
		} catch (NoClassDefFoundError ignored) {
			return null;
		}
	}

	private static Color pastelColorDeterminedBy(String string) {
		Random random = new Random(string.hashCode() * 713);
		int r = 210 + random.nextInt(46);
		int g = 210 + random.nextInt(46);
		int b = 210 + random.nextInt(46);
		return new Color(null, r, g, b);
	}

	Node node() {
		return _node;
	}
	
	void add(DependencyFigure dependency) {
		_dependencies.add(dependency);
	}
	
	void highLightDependencies() {
		for (DependencyFigure dependant : _dependencies) {
			dependant.highlight();
		}
	}
	
	void unHighlightDependencies() {
		for (DependencyFigure dependant : _dependencies) {
			dependant.clearHighlight();
		}
	}

	void position(Point point) {
		this.figure().setLocation(point);
	}

	public Point position() {
		return this.figure().getBounds().getLocation();
	}

	String name() {
		return _node.name();
	}

}
