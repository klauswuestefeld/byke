package byke.views.layout;

import java.io.Serializable;


public class Coordinates implements Serializable {

	private static final long serialVersionUID = 1L;

	public final float _x;
	public final float _y;


	public Coordinates(float x, float y) {
		_x = x;
		_y = y;
	}

	public float getDistance(Coordinates other) {
		return (float)Math.hypot(_x - other._x, _y - other._y);
	}

	public Coordinates translatedBy(float dx, float dy) {
		return new Coordinates(_x + dx, _y + dy);
	}
}
