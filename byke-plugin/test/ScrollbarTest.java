import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
*/
public class ScrollbarTest {

	public static void main(String args[]) {
		new ScrollbarTest();
	}

	private final Display _display = new Display();

	private ScrollbarTest() {
		Shell shell = new Shell(_display);
		shell.setText("Scrollbar Test");
		shell.setSize(300, 300);

		shell.setLayout(new FillLayout());
		
		FigureCanvas canvas = new FigureCanvas(shell);
		canvas.setScrollBarVisibility(FigureCanvas.ALWAYS);

		IFigure figure = new Figure();
		XYLayout layout = new XYLayout();
		figure.setLayoutManager(layout);
		
		Label label = new Label("Banana");
		figure.add(label);
		label.setSize(label.getPreferredSize());
		label.setLocation(new Point(30, 30));
		
	    canvas.setContents(figure);
		
		
		shell.open();
		shell.layout();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		while (!shell.isDisposed()) {
			while (!_display.readAndDispatch()) {
			}
		}
	}


}