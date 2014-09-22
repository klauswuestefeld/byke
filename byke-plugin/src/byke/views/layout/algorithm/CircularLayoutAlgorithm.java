package byke.views.layout.algorithm;

import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.layout.interfaces.LayoutContext;


public class CircularLayoutAlgorithm implements LayoutAlgorithm {

	private static final int EXPAND_RATIO = 50;
	private static final int MINIMUM_DISTANCE = 50;
	
	private LayoutContext _context;

	@Override
	public void applyLayout(boolean arg0) {
		EntityLayout[] entities = _context.getEntities();
		
		int radius = 100;
		while(existsOverlapping(entities)) {
			computeRadialPositions(entities, radius);
			radius += EXPAND_RATIO;
		}
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
			entityBounds.setSize(entityBounds.getWidth() + MINIMUM_DISTANCE, entityBounds.getHeight() + MINIMUM_DISTANCE);
			Rectangle anotherEntityBounds = new Rectangle(anotherEntity.getLocation(), anotherEntity.getSize());
			anotherEntityBounds.setSize(anotherEntityBounds.getWidth() + MINIMUM_DISTANCE, anotherEntityBounds.getHeight() + MINIMUM_DISTANCE);
			
			if(entityBounds.touches(anotherEntityBounds))
				return true;
		}
		return false;
	}

	private void computeRadialPositions(EntityLayout[] entities, int radius) {
		double angle = 0;

		for (EntityLayout entity : entities) {
			Point result = new Point(0, 0);
			result.y = Math.round(radius * Math.sin(angle));
			result.x = Math.round(radius * Math.cos(angle));
			
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
