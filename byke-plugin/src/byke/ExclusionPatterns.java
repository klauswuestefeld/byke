//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira and Kent Beck.
//This is free software. See the license distributed along with this file.
package byke;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import byke.preferences.PreferenceConstants;


public class ExclusionPatterns {

	static private List<Pattern> _excludedClassPatterns = initExcludedClassPatterns();

	
	static boolean ignoreClass(String qualifiedClassName) {
		for (Pattern pattern : _excludedClassPatterns)
			if (pattern.matcher(qualifiedClassName).matches())
				return true;

		return false;
	}

	
	static private List<Pattern> initExcludedClassPatterns() {
		List<Pattern> ret = new ArrayList<Pattern>();
		for (String str : BykePlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PATTERN_EXCLUDES).split("\\s+"))
			ret.add(Pattern.compile(str));
		return ret;
	}
	
}