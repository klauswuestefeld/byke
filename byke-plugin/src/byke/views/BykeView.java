//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.internal.ui.actions.SimpleSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.dependencygraph.Node;
import byke.views.layout.CartesianLayout;
import byke.views.layout.algorithm.LayoutAlgorithm;
import byke.views.layout.ui.GraphCanvas;


public class BykeView extends ViewPart implements IBykeView {

	private final class LayoutJob extends UIJob {
		
		private IJavaElement _elementBeingDisplayed;

		private LayoutAlgorithm<IBinding> _algorithm;

		private final Composite _parent2;
		private GraphCanvas<IBinding> _canvas;

		
		private LayoutJob(Composite parent) {
			super("Byke Diagram Layout");
			setSystem(true);
			setPriority(Job.DECORATE); // Low priority;

			this._parent2 = parent;
		}

		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled()) return Status.OK_STATUS;
			if (_paused) return Status.OK_STATUS;
			if (_parent2 == null || _parent2.isDisposed()) return Status.OK_STATUS;
			checkForNewGraph();
			if (_canvas == null || _canvas.isDisposed()) return Status.OK_STATUS;

			_timeLastLayoutJobStarted = System.nanoTime();
			_canvas.animationStep();
			boolean improved = _algorithm.improveLayoutForAWhile();
			this.schedule(nanosecondsToSleep() / 1000000);

			if (improved) {
				CartesianLayout bestSoFar = _algorithm.layoutMemento();
				_canvas.useLayout(bestSoFar);
				_layoutCache.keep(_elementBeingDisplayed, bestSoFar);
			}

			return Status.OK_STATUS;
		}

		private void checkForNewGraph() {
			if (_selectedGraph == null) return;

			Collection<Node<IBinding>> myGraph;
			synchronized (_graphChangeMonitor) {
				_elementBeingDisplayed = _selectedElement;
				myGraph = _selectedGraph;
				_selectedGraph = null;
			}
			CartesianLayout bestSoFar = _layoutCache.getLayoutFor(_elementBeingDisplayed);
			if (bestSoFar == null) bestSoFar = new CartesianLayout();

			newCanvas((Collection<Node<IBinding>>)myGraph, bestSoFar);
			newAlgorithm((Collection<Node<IBinding>>)myGraph, bestSoFar);
		}
		
		
		private void newCanvas(Collection<Node<IBinding>> graph, CartesianLayout initialLayout) {
			if (_canvas != null) _canvas.dispose();

			_canvas = new GraphCanvas<IBinding>(_parent2, graph, initialLayout, new GraphCanvas.Listener<IBinding>() {
				@Override
				public void nodeSelected(Node<IBinding> node) {
					selectNode(node);
				}
			});
			_parent2.layout();
		}
		
		
		private void newAlgorithm(Collection<Node<IBinding>> graph, CartesianLayout initialLayout) {
			_algorithm = new LayoutAlgorithm<IBinding>(graph, initialLayout, _canvas);
		}

		void togglePaused(boolean pause) {
			_paused = pause;
			if (!_paused) _layoutJob.schedule();
		}

	}

	private boolean _paused;
	
	private static final int ONE_MILLISECOND = 1000000;
	private static final int TEN_SECONDS = 10 * 1000000000;

	private IViewSite _site;

	private final Object _graphChangeMonitor = new Object();
	private IJavaElement _selectedElement;
	private Collection<Node<IBinding>> _selectedGraph;

	private Composite _parent;
	private LayoutJob _layoutJob;
	private long _timeLastLayoutJobStarted;
	private final LayoutMap _layoutCache = new LayoutMap();

	private ISelectionProvider _selectionProvider = new SimpleSelectionProvider();


	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		_site = site;
		_site.getPage().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		_site.getPage().removeSelectionListener(this);
		super.dispose();
	}

	
	@Override
	public void createPartControl(Composite parent) {
		getSite().setSelectionProvider(_selectionProvider);
		_parent = parent;
		_layoutJob = new LayoutJob(_parent);
	}

	@Override
	public void selectionChanged(IWorkbenchPart ignored, ISelection selection) {
		showDependencies(selection);
	}

	@Override
	public void showDependencies(ISelection selection) {
		showJavaDependencies(asJavaElement(selection));
	}

	private void showJavaDependencies(IJavaElement javaElement) {
		if (javaElement == null) return;
		
		DependencyAnalysis a;
		try {
			a = new DependencyAnalysis(javaElement);
		} catch (InvalidElement e) {
			System.err.println(e.getMessage());
			return;
		}
		if (a.subject() == _selectedElement) return;

		
		synchronized (_graphChangeMonitor) {
			_selectedElement = a.subject();
			_selectedGraph = null;
		}

		setContentDescription(_selectedElement.getElementName());
		generateGraph(a);
	}

	private void generateGraph(final DependencyAnalysis analysis) {
		(new Job("'" + analysis.subject().getElementName() + "' analysis") { @Override protected IStatus run(IProgressMonitor monitor) {
			try {
				Collection<Node<IBinding>> nextGraph = analysis.dependencyGraph(monitor);

				synchronized (_graphChangeMonitor) {
					if (analysis.subject() != _selectedElement) return Status.OK_STATUS;
					_selectedGraph = nextGraph;
				}
				_layoutJob.schedule();
			} catch (Exception x) {
				return UIJob.errorStatus(x);
			}
			return Status.OK_STATUS;
		}}).schedule();
	}

	private void selectNode(Node<IBinding> selection) {
		if (null == selection) {
			drillUp();
		} else {
			drillDown(selection);
		}
	}

	private void drillDown(Node<IBinding> selection) {
		IBinding binding = selection.payload();
		IJavaElement element = binding.getJavaElement();
		setSelection(element);
	}


	private void drillUp() {
		setSelection(drillUpTarget());
	}

	
	private void setSelection(IJavaElement element) {
		if (element == null) return;
		StructuredSelection selection = new StructuredSelection(element);
		getSite().getSelectionProvider().setSelection(selection);

		setSelection("org.eclipse.ui.navigator.ProjectExplorer", selection);
		setSelection("org.eclipse.jdt.ui.PackageExplorer", selection);
	}


	private void setSelection(String viewId, StructuredSelection selection) {
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewId);
		if (view != null)
			view.getSite().getSelectionProvider().setSelection(selection);
	}


	private IJavaElement drillUpTarget() {
		IJavaElement current = _selectedElement.getParent();
		return current instanceof ICompilationUnit
			? current.getParent()
			: current;
	}

	@Override
	public void togglePaused(boolean pause) {
		_layoutJob.togglePaused(pause);
	}


	private IJavaElement asJavaElement(ISelection candidate) {
		if (!(candidate instanceof IStructuredSelection)) return null;

		Object firstElement = ((IStructuredSelection)candidate).getFirstElement();
		if (!(firstElement instanceof IJavaElement)) return null;

		return (IJavaElement)firstElement;
	}

	private long nanosecondsToSleep() {
		long currentTime = System.nanoTime();

		long timeLastLayoutJobTook = currentTime - _timeLastLayoutJobStarted;
		if (timeLastLayoutJobTook < 0) timeLastLayoutJobTook = 0; // This can happen due to rounding from nanos to millis.

		long timeToSleep = timeLastLayoutJobTook * 4; // The more things run in parallel with byke, the less greedy byke will be. Byke is proud to be a very good citizen. :)
		if (timeToSleep > TEN_SECONDS) timeToSleep = TEN_SECONDS;
		if (timeToSleep < ONE_MILLISECOND) timeToSleep = ONE_MILLISECOND;

		_timeLastLayoutJobStarted = currentTime + timeToSleep;

		return timeToSleep;
	}

	@Override
	public void setFocus() {
		_parent.setFocus();
	}

}