package byke.views.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class CartesianLayout implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, Coordinates> _coordinatesByName = new HashMap<String, Coordinates>();


	public void keep(String name, Coordinates coordinates) {
		_coordinatesByName.put(name, coordinates);
	}

	public Coordinates coordinatesFor(String name) {
		Coordinates result = _coordinatesByName.get(name);
		return result == null ? new Coordinates(0, 0) : result;
	}

	public Iterable<String> nodeNames() {
		return _coordinatesByName.keySet();
	}

}
