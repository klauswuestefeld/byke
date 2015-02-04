package byke.views.layout.ui;

import java.util.Collection;

public class MnemonicColors {


	public static void colorFor(Collection<NonMovableNode> nodeFigures) {
		lightColorForAll(nodeFigures);
		cyclesAreRed(nodeFigures);
	}

	
	private static void cyclesAreRed(Collection<NonMovableNode> nodeFigures) {
		for(NonMovableNode node : nodeFigures)
			if(node.subGraph().hasCycle()) {
				node.setBackgroundColor(BykeColors.RED);
				node.setForegroundColor(BykeColors.WHITE);
			}
	}

	
	private static void lightColorForAll(Collection<NonMovableNode> nodeFigures) {
		for(NonMovableNode node : nodeFigures)
			node.setBackgroundColor(BykeColors.nextLightColor());
	}
}
