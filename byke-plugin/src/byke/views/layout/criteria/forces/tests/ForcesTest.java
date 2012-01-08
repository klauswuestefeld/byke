package byke.views.layout.criteria.forces.tests;

import org.junit.Assert;
import org.junit.Test;

import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.forces.CenterAllignedForce;

public class ForcesTest extends Assert {

	@Test
	public void centerAllignedForce() {
		assertForces( 1, 0,0, 1,1, "x=-0.70710677 y=-0.70710677;");
		assertForces(-1, 0,0, 1,1, "x=0.70710677 y=0.70710677;");
		assertForces( 1, 0,0, 6,8, "x=-0.6 y=-0.8;");
		assertForces( 1, 6,8, 0,0, "x=0.6 y=0.8;");
		assertForces( 1, 6,0, 0,8, "x=0.6 y=-0.8;");
		assertForces( 1, 0,8, 6,0, "x=-0.6 y=0.8;");
		assertForces( 1, 0,6, 8,0, "x=-0.8 y=0.6;");
	}

	private void assertForces(final int intensity, int x1, int y1, int x2, int y2, String forces) {
		CenterAllignedForce subject = new CenterAllignedForce() { @Override protected float intensityGiven(NodeElement n1, NodeElement n2) {
			return intensity;
		}};
		MockElement e1 = new MockElement(x1, y1);
		MockElement e2 = new MockElement(x2, y2);
		subject.applyTo(e1, e2);
		assertEquals(forces, e1.forces);
	}
	
}
