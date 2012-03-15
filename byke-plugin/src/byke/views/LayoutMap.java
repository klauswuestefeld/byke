package byke.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import byke.views.layout.CartesianLayout;
import byke.views.layout.Coordinates;


public class LayoutMap {

	private static final String FILE_EXTENSION = "properties";

	private final WorkspaceJob saveJob = createSaveJob();
	private IJavaElement elementToSave;
	private CartesianLayout mementoToSave;


	synchronized
	public void keep(IJavaElement element, CartesianLayout memento) {
		elementToSave = element;
		mementoToSave = memento;
		saveJob.schedule(1000 * 3);
	}
	
	
	synchronized
	public CartesianLayout getLayoutFor(IJavaElement element) {
		return element.equals(elementToSave)
			? mementoToSave
			: read(element);
	}


	private CartesianLayout read(IJavaElement element) {
		try {
			IFile file = fileForReading(element);
			if (file == null) return null;

			InputStream contents = file.getContents();
			try {
				Properties properties = new Properties();
				properties.load(contents);
				return produceCartesianLayoutGiven(properties);
			} finally {
				contents.close();
			}
		} catch (CoreException e) {  // Normally caused by folder out of sync
			try {
				bykeFolderFor(element).refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e = e1;
			}
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	private CartesianLayout produceCartesianLayoutGiven(Properties properties) {
		CartesianLayout _cartesianLayout = new CartesianLayout();
		for (Map.Entry<Object, Object> e : properties.entrySet()) {
			String name = (String)e.getKey();
			String[] valueStr = ((String)e.getValue()).split(",", 2);
			Coordinates coordinates = new Coordinates(Integer.parseInt(valueStr[0]), Integer.parseInt(valueStr[1]));
			_cartesianLayout.keep(name, coordinates);
		}
		return _cartesianLayout;
	}

	
	private void performScheduledSaves() {
		IJavaElement element;
		CartesianLayout memento;
		synchronized (this) {
			element = elementToSave;
			memento = mementoToSave;
			elementToSave = null;
			mementoToSave = null;
		}

		save(element, memento);
	}

	
	private void save(IJavaElement element, CartesianLayout memento) {
		try {
			IFile file = createTimestampedFileToAvoidScmMergeConflicts(element);
			
			ByteArrayOutputStream serialization = new ByteArrayOutputStream();
			Properties prop = new Properties();
			for (String name : memento.nodeNames()) {
				Coordinates coordinates = memento.coordinatesFor(name);
				prop.setProperty(name, coordinates._x + "," + coordinates._y);
			}
			prop.store(serialization, "");
			file.create(new ByteArrayInputStream(serialization.toByteArray()), false, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	static private IFile fileForReading(IJavaElement element) throws CoreException, JavaModelException {
		IFolder cacheFolder = produceCacheFolder(element);
		final String baseName = baseNameFor(element);

		return matchingFile(cacheFolder, baseName);
	}

	
	private static IFile matchingFile(IFolder cacheFolder, String baseName) throws CoreException {
		for (IResource candidate : cacheFolder.members()) {
			if (!candidate.getName().startsWith(baseName)) continue;
			if (!FILE_EXTENSION.equals(candidate.getFileExtension())) continue;
			return (IFile)candidate;
		}

		return null;
	}

	
	static private IFile createTimestampedFileToAvoidScmMergeConflicts(IJavaElement element) throws CoreException, JavaModelException {
		IFolder cacheFolder = produceCacheFolder(element);
		String baseName = baseNameFor(element);

		deleteOldFiles(cacheFolder, baseName);

		String newName = baseName + System.currentTimeMillis() + "." + FILE_EXTENSION;
		return cacheFolder.getFile(newName);
	}

	
	private static void deleteOldFiles(IFolder cacheFolder, String baseName) throws CoreException {
		while (true) {
			IFile oldFile = matchingFile(cacheFolder, baseName);
			if (oldFile == null) return;
			oldFile.delete(false, false, null);
		}
	}

	
	static private String baseNameFor(IJavaElement element) throws JavaModelException {
		IPackageFragmentRoot root = getPackageFragmentRoot(element);
		if (root == null) return "";

		IResource correspondingResource;
		try {
			correspondingResource = root.getCorrespondingResource();
		} catch (JavaModelException ignored) {
			return "";
		}
		if (correspondingResource == null) return "";

		String rootNameIncludingSlashes = correspondingResource.getProjectRelativePath().toString();
		String validRootName = rootNameIncludingSlashes.replaceAll("/", "__");

		return validRootName + "__" + nameFor(element) + "__timestamp";
	}

	
	static private String nameFor(IJavaElement element) throws JavaModelException {
		if (element instanceof IPackageFragment) return nameForPackage((IPackageFragment)element);
		if (element instanceof IType) return nameForType((IType)element);
		throw new UnsupportedOperationException("Unable to save layout for " + element + " " + element.getClass());
	}

	
	private static String nameForType(IType element) {
		return element.getFullyQualifiedName();
	}


	private static String nameForPackage(IPackageFragment element) {
		return element.isDefaultPackage()
			? "(default package)"
			: element.getElementName();
	}


	static private IFolder produceCacheFolder(IJavaElement element) throws CoreException {
		IFolder bykeFolder = bykeFolderFor(element);
		produce(bykeFolder);

		IFolder result = bykeFolder.getFolder("layoutcache");
		produce(result);

		return result;
	}


	private static void produce(IFolder folder) throws CoreException {
		if (!folder.exists()) folder.create(false, true, null);
	}

	
	private static IFolder bykeFolderFor(IJavaElement element) {
		IProject project = element.getJavaProject().getProject();
		return project.getFolder(".byke");
	}

	
	/** @return a IPackageFragmentRoot representing a source folder, jar file, zip file or null if the package is directly in the root of an Eclipse project.*/
	static private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		if (element == null) return null;
		return element instanceof IPackageFragmentRoot
			? (IPackageFragmentRoot)element
			: getPackageFragmentRoot(element.getParent());
	}

	
	private WorkspaceJob createSaveJob() {
		WorkspaceJob job = new WorkspaceJob("Writing Byke layout cache") { @Override public IStatus runInWorkspace(IProgressMonitor monitor) {
			performScheduledSaves();
			return Status.OK_STATUS;
		}};
		job.setSystem(true);
		job.setPriority(Job.DECORATE); // Low priority.
		return job;
	}

}
