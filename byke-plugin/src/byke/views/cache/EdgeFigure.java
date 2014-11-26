package byke.views.cache;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "edge")
public class EdgeFigure {

	@XmlAttribute(name = "source")
	private String _source;
	@XmlAttribute(name = "target")
	private String _target;

	
	public String source() {
		return _source;
	}
	
	
	public void source(String source) {
		_source = source;
	}
	
	
	public String target() {
		return _target;
	}
	
	
	public void target(String target) {
		_target = target;
	}
}
