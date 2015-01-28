package byke.views.layout.algorithm;

import org.eclipse.gef4.layout.interfaces.EntityLayout;

public enum AlgorithmUtils {;

	public static void moveToTheBeginning(EntityLayout[] entities) {
		double yOffset = yOffset(entities);
		double xOffset = xOffset(entities);
		
		for(EntityLayout entity : entities)
			entity.setLocation(entity.getLocation().x() + xOffset, entity.getLocation().y() + yOffset);
	}
	
	
	private static double xOffset(EntityLayout[] entities) {
		double mostLeftNode = 0;
		for(EntityLayout entity : entities) {
			if(entity.getLocation().x() > 0)
				continue;
			
			mostLeftNode = Math.min(mostLeftNode, entity.getLocation().x() - entity.getSize().getWidth() / 2);
		}
		return mostLeftNode * -1;
	}
	
	
	private static double yOffset(EntityLayout[] entities) {
		double mostTopNode = 0;
		for(EntityLayout entity : entities) {
			if(entity.getLocation().y() > 0)
				continue;
			
			mostTopNode = Math.min(mostTopNode, entity.getLocation().y() - entity.getSize().getHeight() / 2);
		}
		return mostTopNode * -1;
	}

}
