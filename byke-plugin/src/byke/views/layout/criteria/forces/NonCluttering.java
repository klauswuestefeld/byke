package byke.views.layout.criteria.forces;

import byke.views.layout.criteria.Constants;


public class NonCluttering extends DistanceDefinedForce {

	@Override
	protected float intensityGiven(float nonZeroDistance) {
		return nonZeroDistance > Constants.NON_CLUTTERING_DISTANCE
			? 0f
			: (Constants.NON_CLUTTERING_DISTANCE - nonZeroDistance) * Constants.NON_CLUTTERING;
	}

}
