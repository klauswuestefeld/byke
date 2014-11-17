package byke.views;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IBinding;

import byke.dependencygraph.Node;


public class DependencyAnalysisCache {

	private static final String FILE_EXTENSION = "dot";


	synchronized public void keep(IJavaElement element, Collection<Node<IBinding>> graph) {
		if(needSave(element))
			save(element, graph);
	}

	
	synchronized public String getCacheFor(IJavaElement element) {
		return read(element);
	}

	
	private boolean needSave(IJavaElement element) {
		try {
			if(fileForReading(element) == null)
				return true;
		} catch (JavaModelException e) {
			e.printStackTrace();
			return true;
		} catch (CoreException e) {
			e.printStackTrace();
			return true;
		}
		
		return false;
	}
	
	
	private String read(IJavaElement element) {
		try {
			IFile file = fileForReading(element);
			if (file == null) return null;
			return content(file);
		} catch (CoreException e) { // Normally caused by folder out of sync
			try {
				bykeFolderFor(element).refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e = e1;
			}
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private String content(IFile file) throws CoreException, IOException {
		InputStream contents = file.getContents();
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(contents));
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			contents.close();
		}
	}

	
	private void save(IJavaElement element, Collection<Node<IBinding>> memento) {
		try {
			IFile file = createTimestampedFile(element);
			String toSave = header(element);
			toSave += body(memento);
			toSave += footer();
			file.create(new ByteArrayInputStream(toSave.getBytes(Charset.forName("UTF-8"))), false, null);
		} catch (Exception e) {
			try {
				bykeFolderFor(element).refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e = e1;
			}
			e.printStackTrace();
		}
	}

	private String footer() {
		return "}";
	}

	private String body(Collection<Node<IBinding>> memento) {
		String toSave = "";
		for (Node<IBinding> node : memento)
			toSave += "  \"" + node.name() + "\"\n";
		for (Node<IBinding> node : memento)
			for (Node<IBinding> provider : node.providers())
				toSave += "  \"" + node.name() + "\" -> \"" + provider.name() + "\"\n";
		return toSave;
	}

	private String header(IJavaElement element) {
		return "digraph " + element.getElementName() + " {\n";
	}

	
	static private IFile fileForReading(IJavaElement element) throws CoreException, JavaModelException {
		IFolder cacheFolder = produceCacheFolder(element);
		final String baseName = baseNameFor(element);
		return matchingFile(cacheFolder, baseName + element.getResource().getModificationStamp());
	}

	private static IFile matchingFile(IFolder cacheFolder, String baseName) throws CoreException {
		for (IResource candidate : cacheFolder.members()) {
			if (!candidate.getName().startsWith(baseName)) continue;
			if (!FILE_EXTENSION.equals(candidate.getFileExtension())) continue;
			return (IFile)candidate;
		}
		return null;
	}

	static private IFile createTimestampedFile(IJavaElement element) throws CoreException, JavaModelException {
		IFolder cacheFolder = produceCacheFolder(element);
		String baseName = baseNameFor(element);
		deleteOldFiles(cacheFolder, baseName);
		String newName = baseName + element.getResource().getModificationStamp() + "." + FILE_EXTENSION;
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

	/** @return a IPackageFragmentRoot representing a source folder, jar file, zip file or null if the package is directly in the root of an Eclipse project. */

	static private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		if (element == null) return null;
		return element instanceof IPackageFragmentRoot
		? (IPackageFragmentRoot)element
		: getPackageFragmentRoot(element.getParent());
	}

}