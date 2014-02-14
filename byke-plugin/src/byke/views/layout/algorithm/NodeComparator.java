package byke.views.layout.algorithm;

import java.util.Comparator;

import byke.views.layout.criteria.NodeElement;

public class NodeComparator implements Comparator<NodeElement> {

	@Override
	public int compare(NodeElement o1, NodeElement o2) {
		return o1.name().compareTo(o2.name());
	}

}
