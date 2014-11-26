package byke.views.cache;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import byke.views.cache.GEXFHelper;


public class DependencyAnalysisCache {

	private static final String FILE_EXTENSION = "gexf";


	synchronized public Collection<NodeFigure> keep(IJavaElement element, Collection<Node<IBinding>> graph) {
		return save(element, graph);
	}

	
	synchronized public String getCacheFileFor(IJavaElement element) {
		return read(element);
	}

	
	synchronized public Collection<NodeFigure> getCacheFor(IJavaElement element) {
		try {
			GEXFFile gexf = GEXFHelper.unmarshall(GEXFFile.class, read(element));
			for(EdgeFigure edge : gexf.graph().edges()) {
				NodeFigure source = nodeFor(gexf.graph(), edge.source());
				NodeFigure target = nodeFor(gexf.graph(), edge.target());
				source.addProvider(target);
			}
			
			return gexf.graph().nodes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	
	private NodeFigure nodeFor(GraphFigure graph, String nodeName) {
		for(NodeFigure node : graph.nodes())
			if(node.name().equals(nodeName))
				return node;
		return null;
	}


	private String read(IJavaElement element) {
		try {
			if(element == null)
				return "";
			
			IFile file = fileForReading(element);
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
		
		return "";
	}

	private String content(IFile file) throws CoreException, IOException {
		if(file == null)
			return "";
		
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

	
	private List<NodeFigure> save(IJavaElement element, Collection<Node<IBinding>> memento) {
		GEXFFile gexf = newGEXFFile(memento);
		
		try {
			StringWriter toSave = GEXFHelper.marshall(GEXFFile.class, gexf, new StringWriter());
			
			IFile file = createTimestampedFile(element);
			file.create(new ByteArrayInputStream(toSave.toString().getBytes(Charset.forName("UTF-8"))), false, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gexf.graph().nodes();
	}


	private GEXFFile newGEXFFile(Collection<Node<IBinding>> memento) {
		List<NodeFigure> nodes = createNodes(memento);
		List<EdgeFigure> edges = createEdges(memento);
		
		GraphFigure graph = new GraphFigure();
		graph.defaultEdgeType("directed");
		graph.nodes(nodes);
		graph.edges(edges);
		
		GEXFFile gexf = new GEXFFile();
		gexf.graph(graph);
		return gexf;
	}


	private List<EdgeFigure> createEdges(Collection<Node<IBinding>> memento) {
		List<EdgeFigure> edges = new ArrayList<EdgeFigure>();
		for (Node<IBinding> node : memento) {
			for(Node<IBinding> provider : node.providers()) {
				EdgeFigure edge = new EdgeFigure();
				edge.source(node.name());
				edge.target(provider.name());
				edges.add(edge);
			}
		}
		return edges;
	}


	private List<NodeFigure> createNodes(Collection<Node<IBinding>> memento) {
		List<NodeFigure> nodes = new ArrayList<NodeFigure>();
		for (Node<IBinding> node : memento) {
			NodeFigure figure = new NodeFigure();
			figure.id(node.name());
			figure.name(node.name());
			nodes.add(figure);
		}
		return nodes;
	}

	
	static private IFile fileForReading(IJavaElement element) throws CoreException, JavaModelException {
		IFolder cacheFolder = produceCacheFolder(element);
		final String baseName = baseNameFor(element);
		refreshBykeFolder(element);
		return matchingFile(cacheFolder, baseName + element.getResource().getModificationStamp());
	}

	
	private static void refreshBykeFolder(IJavaElement element) {
		try {
			bykeFolderFor(element).refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {}
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
		refreshBykeFolder(element);
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