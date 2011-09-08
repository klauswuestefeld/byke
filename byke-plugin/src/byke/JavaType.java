package byke;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.ISharedImages;


/**
 * Enum class for 5 standard Java Element Type
 */
public enum JavaType {
	PACKAGE(ISharedImages.IMG_OBJS_PACKAGE), // 
	CLASS(ISharedImages.IMG_OBJS_CLASS), //
	ANNOTATION(ISharedImages.IMG_OBJS_ANNOTATION), // 
	INTERFACE(ISharedImages.IMG_OBJS_INTERFACE), // 
	ENUM(ISharedImages.IMG_OBJS_ENUM);//

	private final String _resourceName;


	JavaType(String str) {
		_resourceName = str;
	}

	public static JavaType valueOf(ITypeBinding binding) {
		assert binding != null;
		if (binding.isAnnotation()) { // FIX: ANNOTATION also isInterface, must detect before INTERFACE
			return ANNOTATION;
		} else if (binding.isInterface()) {
			return INTERFACE;
		} else if (binding.isEnum()) {
			return ENUM;
		} else {
			return CLASS;
		}
	}

	public String getResourceName() {
		return _resourceName;
	}

}