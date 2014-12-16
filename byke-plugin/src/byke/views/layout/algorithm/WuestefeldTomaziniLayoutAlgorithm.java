package byke.views.layout.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.layout.interfaces.LayoutContext;


public class WuestefeldTomaziniLayoutAlgorithm implements LayoutAlgorithm {

	private static final int TOP_MARGIN = 50;
	private static final int LAYER_HEIGHT = 80;
	private static final int MAX_SWEEPS = 30;
	private static final double MINIMUM_DISTANCE = 10;
	private LayoutContext _context;
	private Map<Integer, List<EntityLayout>> entitiesByLayer = new HashMap<Integer, List<EntityLayout>>();


	@Override
	public void applyLayout(boolean arg0) {
		reduceCrossings();
		spreadLayers();
	}

	private void spreadLayers() {
		for (EntityLayout entity : _context.getEntities())
			entity.setLocation(entity.getLocation().x, layer(entity) * LAYER_HEIGHT + TOP_MARGIN);
	}

	private void reduceCrossings() {
		for (int round = 0; round < MAX_SWEEPS; round++)
			for (int l = 0; l < getEntitiesByLayer().size(); l++) {
				positionAtEdgeBarycenters(l);
				spreadOut(l);
			}
	}

	private void spreadOut(int layer) {
		List<EntityLayout> entities = getEntitiesByLayer().get(layer);
		sortByXPosition(entities);
		System.out.println(entities);
		while (spreadOutALittle(entities)) {}
	}

	private boolean spreadOutALittle(List<EntityLayout> layerEntities) {
		boolean ret = false;
		for(int i = 0; i < layerEntities.size() - 1; i++)
			if (spreadOut(layerEntities.get(i), layerEntities.get(i + 1)))
				ret = true;
		return ret;
	}

	private void sortByXPosition(List<EntityLayout> entities) {
		Collections.sort(entities, new Comparator<EntityLayout>() { @Override public int compare(EntityLayout o1, EntityLayout o2) {
				int ret = (int)(o1.getLocation().x - o2.getLocation().x);
				return ret == 0
						? o1.toString().compareTo(o2.toString())
						: ret;
			}});
	}

	private boolean spreadOut(EntityLayout e1, EntityLayout e2) {
		System.out.println();
		System.out.println("" + e1 + e1.getLocation().x + " :: " + e2 + e2.getLocation().x);

		double e1Right = e1.getLocation().x + (e1.getSize().width/2);
		double e2Left  = e2.getLocation().x - (e2.getSize().width/2);
		double distance = e2Left - e1Right - MINIMUM_DISTANCE;

		if (distance >= 0) return false;
		
		e1.setLocation(e1.getLocation().x + (distance / 2)       , 0);
	  e2.setLocation(e2.getLocation().x - (distance / 2) + 1, 0); // +0.01 to avoid infinitesimal approximation to zero
	  return true;
	}


	private void positionAtEdgeBarycenters(int layer) {
		List<EntityLayout> entities = getEntitiesByLayer().get(layer);
		for (EntityLayout e : entities)
			positionAtEdgeBarycenters(e);
	}

	private void positionAtEdgeBarycenters(EntityLayout e) {
		List<EntityLayout> connections = new ArrayList<EntityLayout>();
		connections.addAll(Arrays.asList(e.getPredecessingEntities()));
		connections.addAll(Arrays.asList(e.getSuccessingEntities()));
		
		if (connections.isEmpty()) return;
		
		double barycenter = 0;
		for (EntityLayout c : connections)
			barycenter += c.getLocation().x;

		barycenter /= connections.size();
		
		e.setLocation(barycenter, 0);
	}

	private int layer(EntityLayout entity) {
		int ret = 0;
		EntityLayout[] predecessingEntities = entity.getPredecessingEntities();
		for (EntityLayout predecessingEntity : predecessingEntities) {
			int layer = layer(predecessingEntity);
			if (layer >= ret) ret = layer + 1;
		}

		return ret;
	}

	@Override
	public LayoutContext getLayoutContext() {
		return _context;
	}

	@Override
	public void setLayoutContext(LayoutContext context) {
		_context = context;
	}

	Map<Integer, List<EntityLayout>> getEntitiesByLayer() {
		if (entitiesByLayer.isEmpty())
			splitIntoLayers();
		return entitiesByLayer;
	}

	void splitIntoLayers() {
		for (EntityLayout entity : _context.getEntities()) {
			int layer = layer(entity);

			List<EntityLayout> list = entitiesByLayer.get(layer);
			if (list == null) {
				list = new ArrayList<EntityLayout>();
				entitiesByLayer.put(layer, list);
			}

			list.add(entity);
		}
	}


}
