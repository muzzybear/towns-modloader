package fi.muzzy.towns.modloader;

import java.lang.instrument.*;
import java.util.HashMap;
import java.util.ArrayList;

public class Transformer implements ClassFileTransformer {
	private HashMap<String, SingleClassTransformer> transformers = new HashMap<String, SingleClassTransformer>();
	
	public byte[] transform(ClassLoader loader, String className,
		Class clazz, java.security.ProtectionDomain domain, byte[] bytes)
		throws IllegalClassFormatException
	{
		SingleClassTransformer transformer = transformers.get(className);
		if (transformer != null) {
			bytes = transformer.transform(loader, className, clazz, domain, bytes);
		}
		return bytes;
	}
	
	public SingleClassTransformer getSingleClassTransformer(String className) {
		// Convert class name into format used by instrumentation
		className = className.replace('.', '/');
		// Create transformer on demand
		SingleClassTransformer tmp = transformers.get(className);
		if (tmp == null) {
			tmp = new SingleClassTransformer();
			transformers.put(className, tmp);
		}
		return tmp;
	}
	
	public void hookClassTransformation(String className, ClassFileTransformer transformer) {
		getSingleClassTransformer(className).addTransformer(transformer);
	}
	
	/*
	 * target is a class and method signature, prepended by symbol indicating injection point
	 * for example, "<foo.bar(int)" injects code in the beginning of method bar of class foo
	 */
	
	public void queueInjection(String target, String code) {
		// Check for injection style symbol
		InjectionType itype;
		switch (target.charAt(0)) {
			case '<':
				itype = InjectionType.InjectBefore;
				break;
			case '>':
				itype = InjectionType.InjectAfter;
				break;
			case '=':
				itype = InjectionType.InjectReplace;
				break;
			default:
				//itype = InjectionType.InjectInvalid;
				throw new IllegalArgumentException("Target for injection must specify injection point");
		}
		
		String[] parts = splitTarget(target.substring(1));
		// TODO if class already loaded, fail
		String className = parts[0];
		SingleClassTransformer transformer = getSingleClassTransformer(className);
		
		String method = parts[1];
		transformer.queueInjection(itype, new MethodSignature(method), code);
	}
	
	/** splitTarget returns classname and method signature */
	
	private String[] splitTarget(String target) {
		// split by the the last "." before the first "("
		String[] parts = target.split("\\.(?=\\w+\\()",2);
		if (parts.length != 2) {
			throw new IllegalArgumentException("Target must be formatted like className.method(params)");
		}
		return parts;
	}
}
