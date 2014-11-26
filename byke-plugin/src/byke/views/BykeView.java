//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views;

import java.io.PrintWriter;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.views.cache.DependencyAnalysisCache;
import byke.views.cache.NodeFigure;
import byke.views.layout.ui.NonMovableGraph;


public class BykeView extends ViewPart implements IBykeView {

	private final class LayoutJob extends UIJob {
		
		private final Composite _parent2;
		
		private IJavaElement _elementBeingDisplayed;
		
		private LayoutJob(Composite parent) {
			super("Byke Diagram Layout");
			setSystem(true);
			setPriority(Job.DECORATE); // Low priority;

			this._parent2 = parent;
		}

		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled()) return Status.OK_STATUS;
			if (_parent2 == null || _parent2.isDisposed()) return Status.OK_STATUS;
			checkForNewGraph();

			return Status.OK_STATUS;
		}

		
		private void checkForNewGraph() {
			if (_selectedGraph == null) return;

			Collection<NodeFigure> myGraph;
			synchronized (_graphChangeMonitor) {
				myGraph = _selectedGraph;
				if(_elementBeingDisplayed == null || !_elementBeingDisplayed.equals(_selectedElement)) {
					_elementBeingDisplayed = _selectedElement;
					newGraph(myGraph);
				}
				_selectedGraph = null;
			}

		}
		
		
		private void newGraph(Collection<NodeFigure> graph) {
			disposeGraphs();
			new NonMovableGraph(_parent2, graph);
			_parent2.layout();
		}


		private void disposeGraphs() {
			for(Control control : _parent2.getChildren())
				control.dispose();
		}

	}

	private IViewSite _site;

	private final Object _graphChangeMonitor = new Object();
	private IJavaElement _selectedElement;
	private Collection<NodeFigure> _selectedGraph;
	
	private final DependencyAnalysisCache _cache = new DependencyAnalysisCache();

	public Composite _parent;
	private LayoutJob _layoutJob;


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
			System.err.println("" + javaElement.getElementName() + ": " + e.getMessage());
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
				Collection<NodeFigure> nextGraph = _cache.getCacheFor(analysis.subject());
				if(nextGraph.isEmpty())
					nextGraph = _cache.keep(analysis.subject(), analysis.dependencyGraph(monitor));
				
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


	private IJavaElement asJavaElement(ISelection candidate) {
		if (!(candidate instanceof IStructuredSelection)) return null;

		Object firstElement = ((IStructuredSelection)candidate).getFirstElement();
		if (!(firstElement instanceof IJavaElement)) return null;

		return (IJavaElement)firstElement;
	}

	@Override
	public void setFocus() {
		_parent.setFocus();
	}


	public void exportDependencyAnalysis() {
		if(_selectedElement == null)
			return;
		
    String selectedFileName = askForFile();
    
    if(selectedFileName == null)
    	return;
    
    createFile(selectedFileName);
	}


	private void createFile(String fileName) {
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.print(_cache.getCacheFileFor(_selectedElement));
			writer.close();
		} catch (Exception e) {
			showErrorMessage(e.getMessage());
		}
	}


	private String askForFile() {
		FileDialog fileDialog = new FileDialog(_parent.getShell());
    fileDialog.setText("Create file");
    fileDialog.setFilterExtensions(new String[] { "*.gexf" });
    fileDialog.setFilterNames(new String[] { "GEXF (Graph Exchange XML Format)" });
    String selected = fileDialog.open();
		return selected;
	}


	private void showErrorMessage(String message) {
		MessageBox messageDialog = new MessageBox(_parent.getShell(), SWT.ERROR);
    messageDialog.setText("Error");
    messageDialog.setMessage(message);	
    messageDialog.open();
	}

}