package fi.muzzy.towns.modloader;

import java.lang.instrument.Instrumentation;

public class ModSystem {
	private static ModSystem _instance;
	private Transformer _transformer = new Transformer();
	private ModSystem() {}	
	
	public static void premain(String agentArgument, Instrumentation instrumentation) {
		System.out.println("Modloader initializing...");
		ModSystem sys = getInstance();
		instrumentation.addTransformer(sys.getTransformer());
		
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

	
}
