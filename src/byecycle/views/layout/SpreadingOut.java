package byecycle.views.layout;

public class SpreadingOut extends DistanceBasedForce {

    public float intensityGiven(float distance) {
        return -0.6f / (float)(Math.pow(Math.max(distance, 15), 2.5));  //TODO Play with this formula.
    	//return 0;
    }

}