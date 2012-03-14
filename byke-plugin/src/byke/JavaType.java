package byke;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ui.ISharedImages;


public enum JavaType {

	PACKAGE(ISharedImages.IMG_OBJS_PACKAGE),
	CLASS(ISharedImages.IMG_OBJS_CLASS),
	ANNOTATION(ISharedImages.IMG_OBJS_ANNOTATION), 
	INTERFACE(ISharedImages.IMG_OBJS_INTERFACE),
	ENUM(ISharedImages.IMG_OBJS_ENUM),
	METHOD(ISharedImages.IMG_OBJS_DEFAULT),
	FIELD(ISharedImages.IMG_FIELD_DEFAULT);

	private final String iconResourceName;


	JavaType(String iconResourceName) {
		this.iconResourceName = iconResourceName;
	}

	
	public static JavaType valueOf(ITypeBinding binding) {
		assert binding != null;
		if (binding.isAnnotation()) return ANNOTATION;
		if (binding.isInterface()) return INTERFACE;
		if (binding.isEnum()) return ENUM;
		return CLASS;
	}

	
	public String getResourceName() {
		return iconResourceName;
	}

}