//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PluginActionContributionItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import byecycle.PackageDependencyAnalysis;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.GraphCanvas;

public class ByecycleView extends ViewPart implements ISelectionProvider {

    private static final int ONE_MILLISECOND = 1000000;

    private static final int TEN_SECONDS = 10 * 1000000000;

    public static final int ACTIVACITY = 1;

    public static final String PERSPECTIVE_ID = "byecycle.views.ByecycleView";

    private GraphCanvas _canvas;

    private IViewSite _site;

    private Set<ISelectionChangedListener> _selectionListeners = new HashSet<ISelectionChangedListener>();

    private ISelection _selection;

    private long _timePackageWasSelected;

    private long _timeLastLayoutJobStarted;

    private UIJob _job;

    private boolean _pause;

    /**
     * The constructor.
     */
    public ByecycleView() {
    }

    private void setPaused(boolean pause) {
        if (pause == _pause)
            return;
        _pause = pause;
        firePropertyChange(ACTIVACITY);
        ActionContributionItem toolbar = (ActionContributionItem) _site
                .getActionBars().getToolBarManager().find(
                        "byecycle.toggleActiveAction");
        toolbar.getAction().setChecked(pause);
    }

    public void toggleActive(boolean pause) {
        if (pause == _pause)
            return;
        long currentTime = System.nanoTime();
        if (_pause) {
            setPaused(false);
            _timeLastLayoutJobStarted = currentTime;
            if (_job.getState() == UIJob.SLEEPING) {
                _job.wakeUp();
            } else {
                scheduleImproveLayoutJob();
            }
        } else {
            _job.sleep();
            setPaused(true);
        }
    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        _site = site;
        // _site.getPage().addSelectionListener(this);

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
                if (canvas == null || canvas.isDisposed()
                        || monitor.isCanceled())
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
    }

    @Override
    public void dispose() {
        // _site.getPage().removeSelectionListener(this);
        _job.cancel();
        super.dispose();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite parent) {
        _canvas = new GraphCanvas(parent, new GraphCanvas.Listener() {
            public void nodeSelected(Node node) {
                setSelection(node);
            }
        });
    }

    private void scheduleImproveLayoutJob() {
        _job.schedule();
    }

    private long nanosecondsToSleep() {
        long currentTime = System.nanoTime();

        long timeSincePackageWasSelected = currentTime
                - _timePackageWasSelected;
        if (timeSincePackageWasSelected < TEN_SECONDS)
            return 0; // Go fast in the first ten seconds.

        long timeLastLayoutJobTook = currentTime - _timeLastLayoutJobStarted;
        if (timeLastLayoutJobTook < 0)
            timeLastLayoutJobTook = 0; // This can happen due to rounding from
        // nanos to millis.

        long timeToSleep = timeLastLayoutJobTook * 2; // The more things run
        // in parallel with
        // byecycle, the less
        // greedy byecycle will
        // be. Byecycle is proud
        // to be a very good
        // citizen. :)
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

    @Deprecated
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        selectionChanged(selection);
    }

    public void selectionChanged(ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        if (selection.equals(_selection)) {
            return;
        }

        _selection = selection;
        IStructuredSelection structured = (IStructuredSelection) selection;
        Object selected = structured.getFirstElement();

        if (selection.isEmpty())
            return;

        try {
            setPaused(true);
            List<ICompilationUnit> elements = new ArrayList<ICompilationUnit>();
            for (Iterator i = structured.iterator(); i.hasNext();) {
                Object item = i.next();
                if (item instanceof IPackageFragment) {
                    IPackageFragment selectedPackage = (IPackageFragment) item;
                    elements.addAll(Arrays.asList(selectedPackage
                            .getCompilationUnits()));
                } else if (item instanceof ICompilationUnit) {
                    ICompilationUnit compilationUnit = (ICompilationUnit) item;
                    elements.add(compilationUnit);
                } else if (item instanceof IType) {
                    IType type = (IType) item;
                    elements.add(type.getCompilationUnit());
                } else if (item instanceof IPackageFragmentRoot) {
                    // TODO: verify and debug this
                    IPackageFragmentRoot src = (IPackageFragmentRoot) selected;
                    for (IJavaElement unit : src.getChildren()) {
                        if (!(unit instanceof IPackageFragment))
                            continue;
                        elements.addAll(Arrays.asList(((IPackageFragment) unit)
                                .getCompilationUnits()));
                    }
                }
                // TODO: can we handle this?
                // } else if (selected instanceof IJavaProject) {
                // IJavaProject javaproject = }(IJavaProject) selected;
                // }
            }
            if (!elements.isEmpty()) {
                if (selected instanceof IPackageFragment) {
                    IPackageFragment selectedPackage = (IPackageFragment) selected;
                    setPaused(false);
                    analyze(selectedPackage.getElementName(), elements
                            .toArray(new ICompilationUnit[elements.size()]));
                    scheduleImproveLayoutJob();
                } else if (selected instanceof ICompilationUnit) {
                    ICompilationUnit compilationUnit = (ICompilationUnit) selected;
                    setPaused(false);
                    analyze(compilationUnit.getElementName(), elements
                            .toArray(new ICompilationUnit[elements.size()]));
                    scheduleImproveLayoutJob();
                } else if (selected instanceof IType) {
                    IType type = (IType) selected;
                    setPaused(false);
                    analyze(type.getElementName(), elements
                            .toArray(new ICompilationUnit[elements.size()]));
                    scheduleImproveLayoutJob();
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }

    }

    private void analyze(final String elementName,
            final ICompilationUnit[] compilationUnits) {
        Job job = new Job("'" + elementName + "' analysis") {
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    final Collection<Node<IBinding>> nodes = new PackageDependencyAnalysis(
                            compilationUnits, monitor).dependencyGraph();
                    if (!monitor.isCanceled()) {
                        // dumpGraph(graph);
                        UIJob job = new UIJob("package analysis display") {
                            public IStatus runInUIThread(
                                    IProgressMonitor monitor) {
                                try {
                                    _canvas.setGraph((Collection<Node>) nodes);
                                    _timePackageWasSelected = System.nanoTime();
                                } catch (Exception x) {
                                    x.printStackTrace();
                                }
                                return Status.OK_STATUS;
                            }
                        };
                        job.schedule();
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void dumpGraph(Node[] graph) {
        System.out.println("*********");
        for (int i = 0; i < graph.length; ++i) {
            System.out.println(graph[i].name());
        }
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        _selectionListeners.add(listener);
    }

    public ISelection getSelection() {
        return _selection;
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        _selectionListeners.remove(listener);
    }

    void setSelection(Node selection) {
        if (null == selection) {
            // drill up
            IStructuredSelection structured = (IStructuredSelection) selection;
            IJavaElement element = (IJavaElement) structured.getFirstElement();
            setSelection(new StructuredSelection(element.getParent()));
        } else {
            // drill down
            Node<IBinding> typedNode = (Node<IBinding>) selection;
            IBinding binding = typedNode.payload();
            IJavaElement element = binding.getJavaElement();
            if (null != element) {
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

    public boolean isPaused() {
        return _pause;
    }

}