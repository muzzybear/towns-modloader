package fi.muzzy.towns.modloader;

import java.lang.instrument.*;
import javassist.*;

import java.util.*;

class SingleClassTransformer implements ClassFileTransformer {
	private ArrayList<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();
	private HashMap<MethodSignature, ArrayList<String>> injectBefore = new HashMap<MethodSignature, ArrayList<String>>();
	private HashMap<MethodSignature, ArrayList<String>> injectAfter = new HashMap<MethodSignature, ArrayList<String>>();
	private HashMap<MethodSignature, String> injectReplace = new HashMap<MethodSignature, String>();
	
	public byte[] transform(ClassLoader loader, String className,
		Class clazz, java.security.ProtectionDomain domain, byte[] bytes)
		throws IllegalClassFormatException
	{
		CtClass cl = null;
		try {
			ClassPool pool = ClassPool.getDefault();
			cl = pool.makeClass(new java.io.ByteArrayInputStream(bytes));

			// determine methods to be processed
			Set<MethodSignature> signatures = new HashSet<MethodSignature>();
			signatures.addAll(injectBefore.keySet());
			signatures.addAll(injectAfter.keySet());
			signatures.addAll(injectReplace.keySet());
			
			// first run injection queue
			for (MethodSignature sig : signatures) {
				//System.out.println("Working through injections for class "+className+", method "+sig.getName());
				// turn params into CtClass
				String[] params = sig.getParams();
				CtClass[] ctParams = new CtClass[params.length];
				for (int i=0; i<params.length; i++) {
					CtClass ct;
					String p = params[i];
					// Special types for easier notation
					// TODO maybe find a way to lookup from java.lang directly in default case?
					if (p.equals("String")) ct = pool.get("java.lang.String"); 
					// Basic types
					else if (p.equals("boolean")) ct = CtClass.booleanType; 
					else if (p.equals("byte")) ct = CtClass.byteType; 
					else if (p.equals("char")) ct = CtClass.charType; 
					else if (p.equals("double")) ct = CtClass.doubleType; 
					else if (p.equals("float")) ct = CtClass.floatType; 
					else if (p.equals("int")) ct = CtClass.intType; 
					else if (p.equals("long")) ct = CtClass.longType; 
					else if (p.equals("short")) ct = CtClass.shortType; 
					else if (p.equals("void")) ct = CtClass.voidType; 
					else ct = pool.get(params[i]);
					ctParams[i] = ct;
				}
				// lookup method and inject code
				CtMethod method = cl.getDeclaredMethod(sig.getName(), ctParams);
				// Replacement injection
				if (injectReplace.containsKey(sig)) {
					method.setBody(injectReplace.get(sig));
				}
				// TODO priority queue to determine injection order?
				if (injectBefore.containsKey(sig)) {
					for (String code : injectBefore.get(sig)) {
						method.insertBefore(code);
					}
				}
				if (injectAfter.containsKey(sig)) {
					for (String code : injectAfter.get(sig)) {
						method.insertAfter(code);
					}
				}
			}

			bytes = cl.toBytecode();
		} catch (Exception e) {
			System.out.println("Failed to inject code in "+className+": " + e);
			e.printStackTrace();
		} finally {
			if (cl != null) {
				cl.detach();
			}
		}
		
		// Run custom class transformers
		for (ClassFileTransformer t : transformers) {
			bytes = t.transform(loader, className, clazz, domain, bytes);
		}
		return bytes;
	}
	
	public void addTransformer(ClassFileTransformer transformer) {
		transformers.add(transformer);
	}
	
	public void queueInjection(InjectionType injectionType, MethodSignature method, String code) {
		if (injectionType == InjectionType.InjectBefore) {
			ArrayList<String> injectionList = injectBefore.get(method);
			if (injectionList == null) {
				injectionList = new ArrayList<String>();
				injectBefore.put(method, injectionList);
			}
			injectionList.add(code);
		} else if (injectionType == InjectionType.InjectAfter) {
			ArrayList<String> injectionList = injectAfter.get(method);
			if (injectionList == null) {
				injectionList = new ArrayList<String>();
				injectAfter.put(method, injectionList);
			}
			injectionList.add(code);		
		} else if (injectionType == InjectionType.InjectReplace) {
			// TODO check that there is only one replacement
			injectReplace.put(method, code);
		}
	}
}
