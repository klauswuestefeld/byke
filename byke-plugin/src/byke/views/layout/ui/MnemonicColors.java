package byke.views.layout.ui;

import java.util.Collection;
import java.util.Random;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;


public class MnemonicColors {

	public static void colorFor(Collection<NonMovableNode> nodeFigures) {
		lightColorForAll(nodeFigures);
		cyclesAreRed(nodeFigures);
	}

	private static void cyclesAreRed(Collection<NonMovableNode> nodeFigures) {
		for (NonMovableNode node : nodeFigures)
			if (node.subGraph().hasCycle()) {
				node.setBackgroundColor(ColorConstants.red);
				node.setForegroundColor(ColorConstants.white);
			}
	}

	private static void lightColorForAll(Collection<NonMovableNode> nodeFigures) {
		for (NonMovableNode node : nodeFigures)
			node.setBackgroundColor(pastelColorDeterminedBy(node.getText()));
	}


	private static Color pastelColorDeterminedBy(String name) {
		double hue = new Random(name.hashCode() * 713).nextDouble();
		return colorFor(hue, 0.2, 0.99);
	}
	
	
	private static Color colorFor(double hue, double saturation, double value) {
		int hue_i = (int)(hue * 6);
	  double f = hue * 6 - hue_i;
	  double p = value * (1 - saturation);
	  double q = value * (1 - f * saturation);
	  double t = value * (1 - (1 - f) * saturation);
	  
	  double r = 0;
	  double g = 0;
	  double b = 0;
		
	  if (hue_i==0) {
	  	r = value;
	  	g = t; 
	  	b = p;
	  }
	  if (hue_i==1) {
	  	r = q;
	  	g = value;
	  	b = p;
	  }
	  if (hue_i==2) {
	  	r = p;
	  	g = value;
	  	b = t;	
	  }
	  if (hue_i==3) {
	  	r = p;
	  	g = q;
	  	b = value; 
	  }
	  if (hue_i==4) {
	  	r = t;
	  	g = p;
	  	b = value;
	  }
	  if (hue_i==5) {
	  	r = value;
	  	g = p;
	  	b = q; 
	  }
	  
	  return new Color(null, (int)(r*256), (int)(g*256), (int)(b*256));
	}
}
