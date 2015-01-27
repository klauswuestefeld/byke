package byke.views.layout.ui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

public class Color {

	public static org.eclipse.swt.graphics.Color RED = new org.eclipse.swt.graphics.Color(null, new RGB(255, 0, 0));
	public static org.eclipse.swt.graphics.Color BLACK = new org.eclipse.swt.graphics.Color(null, new RGB(0, 0, 0));
	
	private static org.eclipse.swt.graphics.Color LIGHT_SKY_BLUE = new org.eclipse.swt.graphics.Color(null, new RGB(135, 206, 250));
	private static org.eclipse.swt.graphics.Color LAVENDER = new org.eclipse.swt.graphics.Color(null, new RGB(230, 230, 250));
	private static org.eclipse.swt.graphics.Color PALE_GREEN = new org.eclipse.swt.graphics.Color(null, new RGB(152, 251, 152));
	private static org.eclipse.swt.graphics.Color LIGHT_YELLOW = new org.eclipse.swt.graphics.Color(null, new RGB(255, 255, 224));
	private static org.eclipse.swt.graphics.Color WHEAT = new org.eclipse.swt.graphics.Color(null, new RGB(245, 222, 179));
	private static org.eclipse.swt.graphics.Color LIGHT_SALMON = new org.eclipse.swt.graphics.Color(null, new RGB(255, 160, 122));
	private static org.eclipse.swt.graphics.Color LIGHT_CORAL = new org.eclipse.swt.graphics.Color(null, new RGB(240, 128, 128));
	private static org.eclipse.swt.graphics.Color LIGHT_PINK = new org.eclipse.swt.graphics.Color(null, new RGB(255, 182, 193));
	private static org.eclipse.swt.graphics.Color PLUM = new org.eclipse.swt.graphics.Color(null, new RGB(221, 160, 221));
	private static org.eclipse.swt.graphics.Color THISTLE = new org.eclipse.swt.graphics.Color(null, new RGB(216, 191, 216));
	
	
	private static List<org.eclipse.swt.graphics.Color> LIGHT_COLORS = Arrays.asList(
			LIGHT_SKY_BLUE, LAVENDER, PALE_GREEN, LIGHT_YELLOW,
			WHEAT, LIGHT_SALMON, LIGHT_CORAL, LIGHT_PINK, PLUM, THISTLE);
	
	private static Iterator<org.eclipse.swt.graphics.Color> ITERATOR_FOR_LIGHT_COLORS = LIGHT_COLORS.iterator();
	
	
	public static org.eclipse.swt.graphics.Color nextLightColor() {
		if(!ITERATOR_FOR_LIGHT_COLORS.hasNext())
			ITERATOR_FOR_LIGHT_COLORS = LIGHT_COLORS.iterator();
		
		return ITERATOR_FOR_LIGHT_COLORS.next();
	}
}
