package byke.views.layout.criteria.forces;


public class SaveSpace extends DistanceDefinedForce {

	@Override
	protected float intensityGiven(float nonZeroDistance) {
		return -nonZeroDistance;
	}

}
