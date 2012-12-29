package fi.muzzy.towns.mods;

import java.lang.instrument.*;
import fi.muzzy.towns.modloader.*;
import javassist.*;

public class UIDebugger implements ModInterface, ClassFileTransformer {
	public void initializeMod(ModSystem system) {
		system.getTransformer().hookClassTransformation("xaos/i/j", this);
		system.getTransformer().hookClassTransformation("xaos/i/a", this);
	}
	
	public byte[] transform(ClassLoader loader, String className,
		Class clazz, java.security.ProtectionDomain domain, byte[] bytes)
	{
		if (className.equals("xaos/i/j")) {			
			// Add debug hook to click handler
			/*
			ClassPool pool = ClassPool.getDefault();
			CtClass cl = null;
			try {
				cl = pool.makeClass(new java.io.ByteArrayInputStream(bytes));
			
				CtMethod m = cl.getDeclaredMethod("a", new CtClass[]{CtClass.intType, CtClass.intType, CtClass.booleanType});
				m.insertAfter("if ($_ != 0) { System.out.println(\"UIDebugger: widget \" + $_ + \" hit; \" + $3); }");
				bytes = cl.toBytecode();
			} catch (Exception e) {
				System.err.println("Failed to install UIDebugger hook");
			} finally {
				if (cl != null) {
					cl.detach();
				}
			}
			*/
		} else if (className.equals("xaos/i/a")) {
			ClassPool pool = ClassPool.getDefault();
			CtClass cl = null;
			try {
				cl = pool.makeClass(new java.io.ByteArrayInputStream(bytes));
			
				CtMethod m = cl.getDeclaredMethod(
					"a", new CtClass[]{
						pool.get("java.lang.String"),
						pool.get("java.lang.String"),
						pool.get("java.lang.String"),
						pool.get("xaos.utils.k"),
						pool.get("xaos.m.b"),
						CtClass.intType
					}
				);
				m.insertBefore("System.out.println(\"UIDebugger: i.a.a(\"+$1+\",\"+$2+\",\"+$3+\",\"+$4+\",\"+$5+\",\"+$6+\")\");");
				bytes = cl.toBytecode();
			} catch (Exception e) {
				System.err.println("Failed to install UIDebugger hook: " + e);
			} finally {
				if (cl != null) {
					cl.detach();
				}
			}
		}
		
		
		return bytes;
	}
}
