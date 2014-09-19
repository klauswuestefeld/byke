package byke.views.layout.ui;

import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.AlgorithmHelper;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.layout.interfaces.LayoutContext;


public class CircularLayoutAlgorithm implements LayoutAlgorithm {

	private LayoutContext _context;


	@Override
	public void applyLayout(boolean arg0) {
		EntityLayout[] entities = _context.getEntities();
		Rectangle bounds = _context.getBounds();

		computeRadialPositions(entities, bounds);

//		int insets = 4;
//		bounds.setX(bounds.getX() + insets);
//		bounds.setY(bounds.getY() + insets);
//		bounds.setWidth(bounds.getWidth() - 2 * insets);
//		bounds.setHeight(bounds.getHeight() - 2 * insets);
		AlgorithmHelper.fitWithinBounds(entities, bounds, false);
	}

	private void computeRadialPositions(EntityLayout[] entities, Rectangle bounds) {
		Point centerPoint = new Point(bounds.getWidth() / 2, bounds.getHeight() / 2);
		double angle = 0;
		int distance = 10;

		for (EntityLayout entity : entities) {
			Point result = new Point(0, 0);
			result.y = centerPoint.y + (int)Math.round(distance * Math.sin(angle));
			result.x = centerPoint.x + (int)Math.round(distance * Math.cos(angle));
			
			angle += (2 * Math.PI / entities.length);
			
			entity.setLocation(result.x, result.y);
		}
	}

	@Override
	public LayoutContext getLayoutContext() {
		return _context;
	}

	@Override
	public void setLayoutContext(LayoutContext context) {
		_context = context;
	}
}
