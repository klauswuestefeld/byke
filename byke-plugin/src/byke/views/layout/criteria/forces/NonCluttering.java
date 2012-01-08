package byke.views.layout.criteria.forces;


public class NonCluttering extends DistanceDefinedForce {

	@Override
	protected float intensityGiven(float nonZeroDistance) {
		return nonZeroDistance < 70 ? 2 : 0;
	}

}
