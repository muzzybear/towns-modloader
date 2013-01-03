package fi.muzzy.towns.modloader;

import java.lang.instrument.Instrumentation;
import java.io.File;

public class ModSystem {
	private static ModSystem _instance;
	private Transformer _transformer = new Transformer();
	private ModSystem() {}	
	
	public static void premain(String agentArgument, Instrumentation instrumentation) {
		System.out.println("Modloader initializing...");
		ModSystem sys = getInstance();
		instrumentation.addTransformer(sys.getTransformer());
		
		// TODO hook right before xaos.h.a.C() to load configs and initialize mods, this is the beginning of main loop
		
		// TODO actually load a list of mods from somewhere ... DataFile('codemods.xml') ?
		
		ModInterface mod = new fi.muzzy.towns.mods.ActionHotkeys();
		mod.initializeMod(sys);
	}
	
	public static ModSystem getInstance() {
		if (_instance == null)
			_instance = new ModSystem();
			
		return _instance;
	}
	
	public Transformer getTransformer() {
		return _transformer;
	}

	// Public API for mods to use
	
	/** getUserFile opens a File in Towns user directory */
	
	public File getUserFile(String filename) {
		return new File(xaos.h.a.a() + xaos.h.a.b() + filename);
	}

	/** getDataFile opens a File in Towns data directory */
	
	public File getDataFile(String filename) {
		return new File(xaos.Towns.a("DATA_FOLDER") + filename);
	}
}
