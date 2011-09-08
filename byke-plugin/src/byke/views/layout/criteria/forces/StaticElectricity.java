package byke.views.layout.criteria.forces;

public class StaticElectricity extends DistanceDefinedForce {

	public float intensityGiven(float safeDistance) {
		return -3.2f / (float)(Math.pow(safeDistance, 2.2)); // TODO Play with this formula.
	}

}
