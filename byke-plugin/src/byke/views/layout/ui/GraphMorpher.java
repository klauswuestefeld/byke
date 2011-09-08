package byke.views.layout.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.draw2d.geometry.Point;

import byke.views.layout.CartesianLayout;
import byke.views.layout.Coordinates;


public class GraphMorpher {

	private final List<NodeMorpher> _nodeMorphers = new LinkedList<NodeMorpher>();


	public <T> GraphMorpher(Collection<NodeFigure<T>> nodes, CartesianLayout targets) {
		for (NodeFigure<T> node : nodes)
			addNodeMorpherFor(node, targets);
	}

	private void addNodeMorpherFor(NodeFigure<?> node, CartesianLayout targets) {
		Coordinates myTarget = targets.coordinatesFor(node.name());
		_nodeMorphers.add(new NodeMorpher(node, myTarget));
	}

	void morphingStep(int size) {
		Iterator<NodeMorpher> it = _nodeMorphers.iterator();
		while (it.hasNext()) {
			NodeMorpher morpher = it.next();
			morpher.morphingStep(size);
			if (morpher.onTarget()) it.remove();
		}
	}

	public boolean done() {
		return _nodeMorphers.isEmpty();
	}


	private static class NodeMorpher {

		private final NodeFigure<?> _figure;

		private final int _targetX;
		private final int _targetY;

		private int _currentX;
		private int _currentY;


		NodeMorpher(NodeFigure<?> figure, Coordinates target) {
			_figure = figure;

			_currentX = _figure.position().x;
			_currentY = _figure.position().y;

			_targetX = Math.round(target._x);
			_targetY = Math.round(target._y);
		}

		void morphingStep(int size) {
			int dX = Math.max(Math.min(_targetX - _currentX, size), -size);
			int dY = Math.max(Math.min(_targetY - _currentY, size), -size);

			_currentX += dX;
			_currentY += dY;

			_figure.position(new Point(_currentX, _currentY));
		}

		public boolean onTarget() {
			return _currentX == _targetX && _currentY == _targetY;
		}
	}

}
