package byke.views.layout;

public class FloatRectangle {

	public float _x; // Left
	public float _y; // Top (NOT BOTTOM!).
	public float _width;
	public float _height;


	public FloatRectangle intersection(FloatRectangle other) {
		FloatRectangle result = new FloatRectangle();

		float left = Math.max(this.left(), other.left());
		float right = Math.min(this.right(), other.right());
		float top = Math.max(this.top(), other.top());
		float bottom = Math.min(this.bottom(), other.bottom());

		if (left > right || top > bottom) return result;

		result._x = left;
		result._y = top;
		result._width = (right - left);
		result._height = (bottom - top);
		
		return result;
	}

	public float area() {
		return Math.abs(_width * _height);
	}

	private float left() {
		return _x;
	}

	private float right() {
		return _x + _width;
	}

	private float top() {
		return _y;
	}

	private float bottom() {
		return _y + _height;
	}

}
