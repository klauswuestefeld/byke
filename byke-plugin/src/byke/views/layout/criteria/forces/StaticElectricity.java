package byke.views.layout.criteria.forces;

public class StaticElectricity extends DistanceDefinedForce {

	public float intensityGiven(float nonZeroDistance) {
		return -3.2f / (float)(Math.pow(nonZeroDistance, 2.2));
	}

}
