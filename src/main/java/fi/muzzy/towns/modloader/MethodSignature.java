package fi.muzzy.towns.modloader;

import java.util.regex.*;

public class MethodSignature implements Comparable<MethodSignature> {
	String name;
	String[] params;
	
	MethodSignature (String signature) {
		// ...
		Matcher matcher = Pattern.compile("^(\\w+)\\(\\s*([a-zA-Z_0-9.]+(?:\\s*,\\s*[a-zA-Z_0-9.]+)*)?\\s*\\)$").matcher(signature);

		// Match against method signature with 
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Bad method declaration format");
		}

		name = matcher.group(1);
		String rawParams = matcher.group(2);
		if (rawParams == null) {
			params = new String[0];
		} else {
			params = rawParams.split("\\s*,\\s*");
		}
	}
	
	public String getName() { return name; }
	public String[] getParams() { return params; }
	
	public int compareTo(MethodSignature o) {
		// First sort by name
		int compareNames = name.compareTo(o.name);
		if (compareNames != 0) {
			return compareNames;
		}
		// Second, sort by number of parameters
		if (params.length != o.params.length) {
			return params.length - o.params.length;
		}
		// Third, sort by per parameter names
		for (int i=0; i<params.length; i++) {
			int tmp = params[i].compareTo(o.params[i]);
			if (tmp != 0) {
				return tmp;
			}
		}
		// All equal
		return 0;
	}
}
