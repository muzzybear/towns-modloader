package fi.muzzy.towns.modloader;

import java.lang.instrument.Instrumentation;
import java.io.File;
import java.util.*;

public class ModSystem {
	private static ModSystem _instance;
	private Transformer _transformer = new Transformer();
	private ModSystem() {}
	
	/* singleton accessor */
	
	public static ModSystem getInstance() {
		if (_instance == null)
			_instance = new ModSystem();
			
		return _instance;
	}
	
	/* member accessors */
	
	public Transformer getTransformer() {
		return _transformer;
	}
	
	/* javaagent entrypoint */
	
	public static void premain(String agentArgument, Instrumentation instrumentation) {
		System.out.println("Modloader initializing core...");
		ModSystem sys = getInstance();
		Transformer transformer = sys.getTransformer();
		instrumentation.addTransformer(transformer);
		
		// hook right before Towns main loop to load configs and initialize mods
		transformer.queueInjection("<xaos.h.a.C()", "fi.muzzy.towns.modloader.ModSystem.getInstance().preMainloop();");
		// disable steam api :)
		transformer.queueInjection("=xaos.Towns.b(String)", "return false;");
		
		// TODO actually load a list of mods from somewhere ... DataFile('codemods.xml') ?
		// TODO user folder cant be resolved here but data folder should be okay
		ModInterface mod = new fi.muzzy.towns.mods.ActionHotkeys();
		mod.initializeMod(sys);
	}
	
	public void preMainloop() {
		// TODO ... let mods know we hit mainloop
	}
	
	// Public API for mods to use during initialization
	
	
	// Public API for mods to use during runtime, but not during initialization
	
	/** getUserFile opens a File in Towns user directory */
	
	public File getUserFile(String filename) {
		return new File(xaos.h.a.a() + xaos.h.a.b() + filename);
	}

	/** getDataFile opens a File in Towns data directory */
	
	public File getDataFile(String filename) {
		return new File(xaos.Towns.a("DATA_FOLDER") + filename);
	}
}
