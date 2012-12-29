package fi.muzzy.towns.modloader;

import java.lang.instrument.*;
import java.util.HashMap;
import java.util.ArrayList;

public class Transformer implements ClassFileTransformer {
	private HashMap<String, ArrayList> transformerList = new HashMap<String, ArrayList>();
	
	public byte[] transform(ClassLoader loader, String className,
		Class clazz, java.security.ProtectionDomain domain, byte[] bytes)
		throws IllegalClassFormatException
	{
		ArrayList<ClassFileTransformer> transformers = transformerList.get(className);
		for (ClassFileTransformer t : transformers) {
			bytes = t.transform(loader, className, clazz, domain, bytes);
		}
		return bytes;
	}
	
	public void hookClassTransformation(String className, ClassFileTransformer transformer) {
		ArrayList tmp = transformerList.get(className);
		if (tmp == null) {
			tmp = new ArrayList();
			transformerList.put(className, tmp);
		}
		tmp.add(transformer);
	}
}
