package byke.views.layout;

import java.util.HashMap;
import java.util.Map;


public class CartesianLayout {

	private final Map<String, Coordinates> _coordinatesByName = new HashMap<String, Coordinates>();


	public void keep(String name, Coordinates coordinates) {
		_coordinatesByName.put(name, coordinates);
	}

	
	public Coordinates coordinatesFor(String name) {
		Coordinates ret = _coordinatesByName.get(name);
		return ret == null ? new Coordinates(0, 0) : ret;
	}

	
	public Iterable<String> nodeNames() {
		return _coordinatesByName.keySet();
	}

}
