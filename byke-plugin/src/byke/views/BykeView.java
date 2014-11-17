//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byke.views;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import byke.DependencyAnalysis;
import byke.InvalidElement;
import byke.dependencygraph.Node;
import byke.views.layout.ui.NonMovableGraph;
import byke.views.layout.ui.NonMovableNode;
import byke.views.layout.ui.NonMovableSubGraph;


public class BykeView extends ViewPart implements IBykeView {

	private final class LayoutJob extends UIJob {
		
		private final Composite _parent2;
		
		private IJavaElement _elementBeingDisplayed;
		
		private NonMovableGraph<IBinding> _nonMovableGraph;
		private NonMovableSubGraph<IBinding> _nonMovableSubGraph;
		
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

			Collection<Node<IBinding>> myGraph;
			synchronized (_graphChangeMonitor) {
				myGraph = _selectedGraph;
				if(_elementBeingDisplayed == null || !_elementBeingDisplayed.equals(_selectedElement)) {
					disposeGraphs();
					_elementBeingDisplayed = _selectedElement;
					newGraph((Collection<Node<IBinding>>)myGraph);
				}
				_selectedGraph = null;
			}

		}
		
		
		private void newGraph(Collection<Node<IBinding>> graph) {
			if(_nonMovableGraph != null)
				_nonMovableGraph.dispose();
			_nonMovableGraph = new NonMovableGraph<IBinding>(_parent2, graph);
			_nonMovableGraph.addMouseListener(graphMouseClick());
			
			_parent2.layout();
		}

		
		private MouseListener graphMouseClick() {
			return new MouseListener() {

				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					List<GraphItem> selection = ((NonMovableGraph<IBinding>)e.getSource()).getSelection();
					if(selection.isEmpty() || !(selection.get(0) instanceof NonMovableNode))
						return;
					
					Collection<Node<IBinding>> nodes = ((NonMovableNode<IBinding>)selection.get(0)).internalNodes();
					if(nodes.size() < 2)
						return;
					
					_nonMovableSubGraph = new NonMovableSubGraph<IBinding>(_parent2, nodes);
					_nonMovableSubGraph.addMouseListener(subGraphMouseListener());
					_nonMovableGraph.dispose();
					_parent2.layout();
					
				}
			};
		}

		
		private MouseListener subGraphMouseListener() {
			return new MouseListener() {
				@Override
				public void mouseUp(MouseEvent e) {}
				
				@Override
				public void mouseDown(MouseEvent e) {}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					if(!_nonMovableSubGraph.isDisposed())
						_nonMovableSubGraph.dispose();
					Collection<Node<IBinding>> nodes = new HashSet<Node<IBinding>>(_nonMovableGraph.nodes());
					_nonMovableGraph = new NonMovableGraph<IBinding>(_parent2, nodes);
					_nonMovableGraph.addMouseListener(graphMouseClick());
					_parent2.layout();
				}

			};
		}


		public void disposeGraphs() {
			if(_nonMovableGraph != null)
				_nonMovableGraph.dispose();
			if(_nonMovableSubGraph != null)
				_nonMovableSubGraph.dispose();
		}
	}

	private IViewSite _site;

	private final Object _graphChangeMonitor = new Object();
	private IJavaElement _selectedElement;
	private Collection<Node<IBinding>> _selectedGraph;

	private Composite _parent;
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
				Collection<Node<IBinding>> nextGraph = analysis.dependencyGraph(monitor);
				new DependencyAnalysisCache().keep(analysis.subject(), nextGraph);
				
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
			writer.print(new DependencyAnalysisCache().getCacheFor(_selectedElement));
			writer.close();
		} catch (Exception e) {
			showErrorMessage(e.getMessage());
		}
	}


	private String askForFile() {
		FileDialog fileDialog = new FileDialog(_parent.getShell());
    fileDialog.setText("Create file");
    fileDialog.setFilterExtensions(new String[] { "*.dot" });
    fileDialog.setFilterNames(new String[] { "DOT Language(*.dot)" });
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