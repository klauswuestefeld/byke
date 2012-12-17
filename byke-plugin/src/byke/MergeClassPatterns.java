package byke;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import byke.preferences.PreferenceConstants;


public class MergeClassPatterns {
	
	static boolean existsMergeClassValue() {
		return !getPreference(PreferenceConstants.P_PATTERN_MERGE_CLASS).isEmpty();
	}

	static List<Pattern> getPatterns() {
		String regexBase = "(\\w*)";
		List<Pattern> ret = new ArrayList<Pattern>();
		
		for (String str : getPreference(PreferenceConstants.P_PATTERN_MERGE_CLASS).split("\\s+")) {
			String mergePattern = str.replace("*", "").trim();
			ret.add(Pattern.compile(str.startsWith("*") ? regexBase + mergePattern + "$" : "^" + mergePattern + regexBase));
		}
		return ret;
	}

	private static String getPreference(String param) {
		return BykePlugin.getDefault().getPreferenceStore().getString(param);
	}

}
