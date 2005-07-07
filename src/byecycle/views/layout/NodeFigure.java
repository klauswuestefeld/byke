//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
//Contributions by Julio César do Nascimento.

package byecycle.views.layout;

import java.io.InputStream;
import java.util.Random;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import byecycle.dependencygraph.Node;

public class NodeFigure extends GraphElement {
    
	private static final int MARGIN_PIXELS = 2;
	private static final float VISCOSITY = 0.85f;  //TODO Play with this. :)
	private static final float IMPETUS = 900;
	
	private static final Random RANDOM = new Random();
    
	NodeFigure(Node node, StressMeter stressMeter) {
        _node = node;
        _stressMeter = stressMeter;
    }

  	private Label label(String text, Image icon) {
		return icon == null
			? new Label(" " + text, icon)
			: new Label(      text, icon);
	}

	IFigure produceFigure() {
		IFigure result;
		String text = text(_node);
		Color color = randomPastelColor();
		if (text.length() < 20) {
			result = label(text, imageForNode(_node));
		} else {
			result = new CompartmentFigure(color);
			int cut = (text.length() / 2) - 1;
			result.add(label(text.substring(0,cut), imageForNode(_node)));
			result.add(label(text.substring(cut), null));
		}

		result.setBorder(new LineBorder());
		result.setBackgroundColor(color);
		result.setOpaque(true);
        return result;
	}

	
	static class CompartmentFigure extends Figure {
	  public CompartmentFigure(Color color) {
		    ToolbarLayout layout = new ToolbarLayout();
		    layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		    layout.setStretchMinorAxis(false);
		    setLayoutManager(layout);
		  }
	}
	
	private static String text(Node node) {
        String result = node.name();
	    if (node.kind().equals("package")) return result;
	    return result.substring(result.lastIndexOf('.') + 1);
    }

    private static Image imageForNode(Node node) {
		InputStream resource = NodeFigure.class.getResourceAsStream("icons/" + node.kind() + ".gif");
		return null == resource ? null : new Image(Display.getCurrent(), resource);
	}

	private Color randomPastelColor() {
        int r = 210 + RANDOM.nextInt(46);
        int g = 210 + RANDOM.nextInt(46);
        int b = 210 + RANDOM.nextInt(46);
        return new Color(null, r, g, b);
    }

    private final Node _node;

	private float _currentX;
	        float _currentY;

	private float _targetX;
	private float _targetY;

	private float _candidateX;
    private float _candidateY;
    private float _forceComponentX;
    private float _forceComponentY;
	private Rectangle _aura;
    
	private final StressMeter _stressMeter;
	
    Node node() {
        return _node;
    }
	
	public float candidateX() {
		return _candidateX;
	}
	
	public float candidateY() {
		return _candidateY;
	}
	
	public Rectangle intersection(NodeFigure other) {
		Point c1 = candidatePosition();
		Point c2 = other.candidatePosition(); 
	
		Rectangle r1 = new Rectangle(c1, figure().getBounds().getSize()); //TODO Optimize. Create only once and reuse.
		Rectangle r2 = new Rectangle(c2, other.figure().getBounds().getSize());
		
    	return r1.intersect(r2); //TODO: Use size of r1.getIntersection(r2) to determine the force.
    }

	public void up(float thrust) {
        addForceComponents(0, -thrust);
    }

    public void down(float thrust) {
        addForceComponents(0, thrust);
    }
    
	public Point candidatePosition() {
		return new Point(_candidateX, _candidateY);
	}

	public void addForceComponents(float x, float y) {
        _forceComponentX += x * IMPETUS;
        _forceComponentY += y * IMPETUS;
        _stressMeter.addStress((float)Math.sqrt((x*x) + (y*y)));
    }

    private static float dampen(float value) {
        return Math.max(Math.min(value * VISCOSITY, 20), -20);
    }

    /** "Give: To yield to physical force." Dictionary.com */
    void give() {
		_candidateX += _forceComponentX;
    	_candidateY += _forceComponentY;

    	_forceComponentX = dampen(_forceComponentX);
		_forceComponentY = dampen(_forceComponentY);

		stayAround();
	}

	boolean isMoving() {
        return Math.abs(_forceComponentX) > 0.2 || Math.abs(_forceComponentY) > 0.2;
        //FIXME: Nudge is not called when nodes are pressed against the margin. Keep previous coordinates to compare instead of using the component forces.
    }

    void nudgeNudge() {
        addForceComponents(nudge(), nudge());
        System.out.println("Nudge: " + nudge());
    }

    private float nudge() {
		return (RANDOM.nextFloat() - 0.5f) * 0.1f;
	}

	private void stayAround() {
    	addForceComponents( (-_candidateX * 0.0000002f), (-_candidateY * 0.0000002f));
    	
    	Rectangle availableSpace = figure().getParent().getClientArea();
    	Rectangle me = figure().getBounds();
    	
    	int maxX = availableSpace.width - me.width - MARGIN_PIXELS;
    	int maxY = availableSpace.height - me.height - MARGIN_PIXELS;
    	
        if (_candidateX < MARGIN_PIXELS) _candidateX = MARGIN_PIXELS;
        if (_candidateX >   maxX) _candidateX = maxX;

        if (_candidateY < MARGIN_PIXELS) _candidateY = MARGIN_PIXELS;
        if (_candidateY >   maxY) _candidateY = maxY;
    }

    void position(float x, float y) {
        _currentX = x;
        _currentY = y;

        _targetX = x;
		_targetY = y;

        _candidateX = x;
        _candidateY = y;
    }

	void lockOnTarget() {
		_targetX = _candidateX;
		_targetY = _candidateY;
	}

	void pursueTarget(XYLayout layout) {
		float dX = Math.max(Math.min(_targetX - _currentX, 3), -3);
		float dY = Math.max(Math.min(_targetY - _currentY, 3), -3);
		
		_currentX += dX;
		_currentY += dY;
		
		layout.setConstraint(this.figure(), new Rectangle(Math.round(_currentX), Math.round(_currentY), -1, -1));
	}

	boolean onTarget() {
		return _currentX ==_targetX && _currentY == _targetY;
	}

	public boolean dependsOn(NodeFigure other) {
		return _node.dependsDirectlyOn(other.node());
	}

	public Rectangle aura() {
		int auraThickness = 10;
		Point candidatePosition = candidatePosition();
		
		if (_aura == null) {
			_aura = new Rectangle(candidatePosition, figure().getBounds().getSize());
			_aura.x -= auraThickness;
			_aura.y -= auraThickness;
			_aura.width += auraThickness;
			_aura.height += auraThickness;
		}
		else {
			_aura.x = candidatePosition.x - auraThickness;
			_aura.y = candidatePosition.y - auraThickness;
		}
		return _aura;
        
	}

}
