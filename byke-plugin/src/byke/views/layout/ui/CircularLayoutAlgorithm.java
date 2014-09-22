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
		Rectangle bounds = computeBoundsWithoutOverlapping(entities);
		computeRadialPositions(entities);
		AlgorithmHelper.fitWithinBounds(entities, bounds, false);
	}

	private Rectangle computeBoundsWithoutOverlapping(EntityLayout[] entities) {
		computeRadialPositions(entities);

		Rectangle bounds = AlgorithmHelper.getLayoutBounds(entities, false);
		while(existsOverlapping(entities)) {
			AlgorithmHelper.fitWithinBounds(entities, bounds, false);
			bounds.setWidth(bounds.getWidth() + 10);
			bounds.setHeight(bounds.getHeight() + 10);
		}
		return bounds;
	}

	private boolean existsOverlapping(EntityLayout[] entities) {
		for (EntityLayout entity : entities)
			if(isOverlapping(entity, entities))
					return true;
		
		return false;
	}

	private boolean isOverlapping(EntityLayout entity, EntityLayout[] entities) {
		for (EntityLayout anotherEntity : entities) {
			if(entity.equals(anotherEntity))
				continue;
			
			Rectangle entityBounds = new Rectangle(entity.getLocation(), entity.getSize());
			entityBounds.setSize(entityBounds.getWidth() + 50, entityBounds.getHeight() + 50);
			Rectangle anotherEntityBounds = new Rectangle(anotherEntity.getLocation(), anotherEntity.getSize());
			anotherEntityBounds.setSize(anotherEntityBounds.getWidth() + 50, anotherEntityBounds.getHeight() + 50);
			
			if(entityBounds.touches(anotherEntityBounds))
				return true;
		}
		return false;
	}

	private void computeRadialPositions(EntityLayout[] entities) {
		double angle = 0;
		int distance = 100;

		for (EntityLayout entity : entities) {
			Point result = new Point(0, 0);
			result.y = Math.round(distance * Math.sin(angle));
			result.x = Math.round(distance * Math.cos(angle));
			
			angle += 2 * Math.PI / entities.length;
			
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
