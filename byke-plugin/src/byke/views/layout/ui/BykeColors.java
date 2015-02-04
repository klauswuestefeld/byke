package byke.views.layout.ui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class BykeColors {

	public static final Color WHITE = new Color(null, new RGB(255, 255, 255));
	public static final Color RED = new Color(null, new RGB(255, 0, 0));
	public static final Color BLACK = new Color(null, new RGB(0, 0, 0));
	
	private static final Color LIGHT_SKY_BLUE = new Color(null, new RGB(135, 206, 250));
	private static final Color LAVENDER = new Color(null, new RGB(230, 230, 250));
	private static final Color PALE_GREEN = new Color(null, new RGB(152, 251, 152));
	private static final Color LIGHT_YELLOW = new Color(null, new RGB(255, 255, 224));
	private static final Color WHEAT = new Color(null, new RGB(245, 222, 179));
	private static final Color LIGHT_SALMON = new Color(null, new RGB(255, 160, 122));
	private static final Color LIGHT_CORAL = new Color(null, new RGB(240, 128, 128));
	private static final Color LIGHT_PINK = new Color(null, new RGB(255, 182, 193));
	private static final Color PLUM = new Color(null, new RGB(221, 160, 221));
	private static final Color THISTLE = new Color(null, new RGB(216, 191, 216));
	
	private static final Color DARK_SLATE_GRAY = new Color(null, new RGB(49, 79, 79));
	private static final Color DARK_GREEN = new Color(null, new RGB(0, 100, 0));
	private static final Color DARK_VIOLET = new Color(null, new RGB(148, 0, 211));
	private static final Color NAVY = new Color(null, new RGB(0, 0, 128));
	
	
	private static List<Color> LIGHT_COLORS = Arrays.asList(
			LIGHT_SKY_BLUE, LAVENDER, PALE_GREEN, LIGHT_YELLOW,
			WHEAT, LIGHT_SALMON, LIGHT_CORAL, LIGHT_PINK, PLUM, THISTLE);
	
	private static Iterator<Color> ITERATOR_FOR_LIGHT_COLORS = LIGHT_COLORS.iterator();

	private static List<Color> DARK_COLORS = Arrays.asList(
			DARK_SLATE_GRAY, DARK_GREEN, DARK_VIOLET, NAVY);
	
	private static Iterator<Color> ITERATOR_FOR_DARK_COLORS = DARK_COLORS.iterator();
	
	
	public static Color nextLightColor() {
		if(!ITERATOR_FOR_LIGHT_COLORS.hasNext())
			ITERATOR_FOR_LIGHT_COLORS = LIGHT_COLORS.iterator();
		
		return ITERATOR_FOR_LIGHT_COLORS.next();
	}

	public static Color nextDarkColor() {
		if(!ITERATOR_FOR_DARK_COLORS.hasNext())
			ITERATOR_FOR_DARK_COLORS = DARK_COLORS.iterator();
		
		return ITERATOR_FOR_DARK_COLORS.next();
	}
}
