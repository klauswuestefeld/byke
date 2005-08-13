//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.
package byecycle.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import byecycle.PackageDependencyAnalysis;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class ByecycleView extends ViewPart implements ISelectionListener, IByecycleView {
	private static final int ONE_MILLISECOND = 1000000;

	private static final int TEN_SECONDS = 10 * 1000000000;

	private GraphCanvas _canvas;

	private IViewSite _site;

	private Set<ISelectionChangedListener> _selectionListeners = new HashSet<ISelectionChangedListener>();

	private ISelection _selection;

	private ISelection _currentSelection;

	private long _timeLastLayoutJobStarted;

	private UIJob _job;

	private boolean _pause;

	// TODO: Debug me
	final IPartListener2 partListener = new IPartListener2() {
		private byte state = 0; // 0 = visible , 1= was active & hidden, 2=was paused & hidden

		public void partActivated(IWorkbenchPartReference partRef) {
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		public void partOpened(IWorkbenchPartReference partRef) {
		}

		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == ByecycleView.this) {
				if (state == 0) {
					if (_pause) {
						state = 2;
					} else {
						state = 1;
						_pause = true; // pause redraw, but keep toolbar icon
					}
				}
			}
		}

		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == ByecycleView.this) {
				if (state == 1) {
					toggleActive(false); // resume
				}
				state = 0;
			}
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}
	};

	/**
	 * The constructor.
	 */
	public ByecycleView() {
	}

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		_site = site;
		_site.getPage().addSelectionListener(this);
		_site.setSelectionProvider(this);
		_job = new UIJob("package analysis display") {
			@Override
			public boolean shouldSchedule() {
				return !_pause;
			}

			@Override
			public boolean shouldRun() {
				return !_pause;
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				GraphCanvas canvas = _canvas;
				if (canvas == null || canvas.isDisposed() || monitor.isCanceled())
					return Status.OK_STATUS;
				try {
					_canvas.tryToImproveLayout();
					this.schedule(nanosecondsToSleep() / 1000000);
				} catch (Exception rx) {
					return UIJob.errorStatus(rx);
				}
				return Status.OK_STATUS;
			}
		};
		_job.setSystem(true);
		_site.getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	@Override
	public void dispose() {
		_site.getPage().removeSelectionListener(this);
		_site.getWorkbenchWindow().getPartService().removePartListener(partListener);
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		_canvas = new GraphCanvas(parent, new GraphCanvas.Listener() {
			public void nodeSelected(Node node) {
				setSelection(node);
			}
		});
	}

	private void scheduleImproveLayoutJob() {
		if (_job.getState() == UIJob.SLEEPING) {
			_job.wakeUp();
		} else {
			_job.schedule();
		}
	}

	private long nanosecondsToSleep() {
		long currentTime = System.nanoTime();
		long timeLastLayoutJobTook = currentTime - _timeLastLayoutJobStarted;
		if (timeLastLayoutJobTook < 0)
			timeLastLayoutJobTook = 0; // This can happen due to rounding from nanos to millis.
		long timeToSleep = timeLastLayoutJobTook * 4; // The more things run in parallel with byecycle, the less greedy byecycle
		// will be. Byecycle is proud to be a very good citizen. :)
		if (timeToSleep > TEN_SECONDS)
			timeToSleep = TEN_SECONDS;
		if (timeToSleep < ONE_MILLISECOND)
			timeToSleep = ONE_MILLISECOND;
		_timeLastLayoutJobStarted = currentTime + timeToSleep;
		return timeToSleep;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		// viewer.getControl().setFocus();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		if (part == this) {
			if (!_pause) {
				selectionChanged(selection);
			}
			return;
		}
		if (_currentSelection == selection)
			return;
		_currentSelection = selection;
		if (!_pause) {
			selectionChanged(selection);
		}
	}

	public void selectionChanged(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		_selection = selection;
		refresh();
	}

	private ICompilationUnit[] _compilationUnits = null;

	private void refresh() {
		IStructuredSelection structured = (IStructuredSelection) _selection;
		Object selected = structured.getFirstElement();
		try {
			ICompilationUnit[] compilationUnits = null;
			String name = null;
			if (selected instanceof IPackageFragment) {
				IPackageFragment selectedPackage = (IPackageFragment) selected;
				compilationUnits = selectedPackage.getCompilationUnits();
				name = selectedPackage.getElementName();
			} else if (selected instanceof ICompilationUnit) {
				ICompilationUnit compilationUnit = (ICompilationUnit) selected;
				compilationUnits = new ICompilationUnit[] { compilationUnit };
				name = compilationUnit.getElementName();
			} else if (selected instanceof IType) {
				IType type = (IType) selected;
				compilationUnits = new ICompilationUnit[] { type.getCompilationUnit() };
				name = type.getElementName();
			}
			if (name != null) {
				if (!Arrays.deepEquals(_compilationUnits, compilationUnits)) {
					_compilationUnits = compilationUnits;
					analyze(name, compilationUnits);
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private void analyze(final String elementName, final ICompilationUnit[] compilationUnits) {
		_timeLastLayoutJobStarted = System.nanoTime();
		Job job = new Job("'" + elementName + "' analysis") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					_pause = true;
					final Collection<Node<IBinding>> nodes = new PackageDependencyAnalysis(compilationUnits, monitor)
							.dependencyGraph();
					if (!monitor.isCanceled()) {
						// dumpGraph(graph);
						UIJob job = new UIJob("package analysis display") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								try {
									setPaused(false);
									scheduleImproveLayoutJob();
									_canvas.setGraph((Collection<Node>) nodes);
								} catch (Exception x) {
									UIJob.errorStatus(x);
								}
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				} catch (Exception x) {
					UIJob.errorStatus(x);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		_selectionListeners.add(listener);
	}

	public ISelection getSelection() {
		return _selection;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		_selectionListeners.remove(listener);
	}

	void setSelection(Node selection) {
		if (null == selection) {
			// drill up
			IStructuredSelection structured = (IStructuredSelection) _selection; // Fix incorrect reference
			IJavaElement element = (IJavaElement) structured.getFirstElement();
			final IJavaElement parent = element.getParent();
			if (parent instanceof PackageFragment) {
				setPaused(false);
				setSelection(new StructuredSelection(parent));
			}
		} else {
			// drill down
			Node<IBinding> typedNode = (Node<IBinding>) selection;
			IBinding binding = typedNode.payload();
			IJavaElement element = binding.getJavaElement();
			if (null != element) {
				setPaused(false);
				setSelection(new StructuredSelection(element));
			}
		}
	}

	public void setSelection(ISelection selection) {
		if (selection.equals(_selection)) {
			return;
		}
		fireSelectionChanged(selection);
	}

	private void fireSelectionChanged(ISelection selection) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener listener : _selectionListeners) {
			listener.selectionChanged(event);
		}
	}

	private void setPaused(boolean pause) {
		if (pause == _pause)
			return;
		_pause = pause;
		firePropertyChange(ACTIVACITY);
	}

	public void toggleActive(boolean pause) {
		if (pause == _pause)
			return;
		if (_pause) {
			if (_currentSelection != null && !_currentSelection.equals(_selection)) {
				selectionChanged(_currentSelection);
			} else {
				setPaused(false);
				scheduleImproveLayoutJob();
			}
		} else {
			_job.sleep();
			setPaused(true);
		}
	}

	public boolean isPaused() {
		return _pause;
	}
}