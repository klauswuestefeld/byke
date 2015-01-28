package byke.views.layout.algorithm;

import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.layout.interfaces.LayoutContext;


public class CircularLayoutAlgorithm implements LayoutAlgorithm {

	private static final float EXPAND_RATIO = 1.05f;
	private static final int MINIMUM_DISTANCE = 5;
	
	private LayoutContext _context;

	@Override
	public void applyLayout(boolean arg0) {
		EntityLayout[] entities = _context.getEntities();
		
		int radius = 20;
		while(existsOverlapping(entities)) {
			layoutInCircle(entities, radius);
			radius *= EXPAND_RATIO;
		}
		
		AlgorithmUtils.moveToTheBeginning(entities);
	}


	private boolean existsOverlapping(EntityLayout[] entities) {
		for (EntityLayout entity : entities)
			if(isOverlapping(entity, entities))
					return true;
		
		return false;
	}

	private boolean isOverlapping(EntityLayout entity, EntityLayout[] entities) {
		for (EntityLayout other : entities) {
			if(entity.equals(other))
				continue;
			
			Rectangle entityBounds = new Rectangle(entity.getLocation(), entity.getSize());
			entityBounds.setSize(entityBounds.getWidth() + MINIMUM_DISTANCE, entityBounds.getHeight() + MINIMUM_DISTANCE);
			Rectangle anotherEntityBounds = new Rectangle(other.getLocation(), other.getSize());
			anotherEntityBounds.setSize(anotherEntityBounds.getWidth() + MINIMUM_DISTANCE, anotherEntityBounds.getHeight() + MINIMUM_DISTANCE);
			
			if(entityBounds.touches(anotherEntityBounds))
				return true;
		}
		return false;
	}

	private void layoutInCircle(EntityLayout[] entities, int radius) {
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
