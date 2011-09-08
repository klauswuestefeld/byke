package byke.views.layout.algorithm.random;

import byke.dependencygraph.Node;
import byke.views.layout.criteria.NodeElement;
import byke.views.layout.criteria.StressMeter;


public class AveragingNode extends NodeElement {

	AveragingNode(Node<?> node, StressMeter stressMeter) {
		super(node, stressMeter);
	}
	
	private float _totalX = 0;
	private float _totalY = 0;
	private int count = 0;

	void takeAveragePosition(float ellapsedTime, float timeFrame) {
		//if (ellapsedTime < 300) return;
		
		count ++;
		
		//try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }
		
		_totalX += _pendingForceX * timeFrame;
		_totalY += _pendingForceY * timeFrame;
//		position((float)(_totalX * 30 / Math.sqrt(ellapsedTime)), (float)(_totalY * 30 / Math.sqrt(ellapsedTime)));
		position((float)(_totalX * 50000 / ellapsedTime), (float)(_totalY * 50000 / ellapsedTime));
//		position((float)(_totalX * 100 / count), (float)(_totalY * 100 / count));
		
		//position(_x + _totalX, _y + _totalY);
		//position(_totalX, _totalY);
	}

}
