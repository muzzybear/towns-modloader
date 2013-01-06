package fi.muzzy.towns.modloader;

import java.lang.instrument.Instrumentation;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

public class ModSystem {
	private static ModSystem _instance;
	private Transformer _transformer = new Transformer();
	private Properties townsIni = new Properties();
	
	private ModSystem() {
		// Assume working directory is towns directory, xaos.jar does the same
		try {
			townsIni.load(new FileInputStream("towns.ini"));			
		} catch (Exception e) {
			throw new RuntimeException("Modloader failed to load towns.ini");
		}
	}
	
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
	
	private void loadMods() {
		// load mod cofig
		Document config;
		try {
			File configFile = getDataFile("codemods.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			config = dBuilder.parse(configFile);
		} catch (Exception e) {
			// TODO figure out better exception class
			throw new RuntimeException("Modloader failed to read codemods.xml");
		}
		
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		
		NodeList modNodes = config.getDocumentElement().getChildNodes();
		for (int i=0; i<modNodes.getLength(); i++) {
			Node modNode = modNodes.item(i);
			if (modNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			NamedNodeMap attrs = modNode.getAttributes();
			if (attrs.getNamedItem("name") != null) {
				System.out.println("Loading codemod: " + attrs.getNamedItem("name").getNodeValue());
			}
			NodeList modActionNodes = modNode.getChildNodes();
			for (int j=0; j<modActionNodes.getLength(); j++) {
				Node actionNode = modActionNodes.item(j);
				if (actionNode.getNodeType() != Node.ELEMENT_NODE)
					continue;
				NamedNodeMap actionAttrs = actionNode.getAttributes();
				String actionTag = actionNode.getNodeName().toLowerCase();
				
				if (actionTag.equals("class")) {
					String className = actionAttrs.getNamedItem("name").getNodeValue();
					// Obtain mod constructor
					java.lang.reflect.Constructor<? extends ModInterface> modConstructor;
					try {
						// Dancing with reflection to guarantee checked exceptions
						Class<?> clazz = classLoader.loadClass(className);
						Class<? extends ModInterface> modClass = clazz.asSubclass(ModInterface.class);
						modConstructor = modClass.getConstructor();
					} catch (Exception e) {
						throw new RuntimeException("Unable to load codemod class "+className+": "+e);
					}
					// Initialize the mod class
					try {
						ModInterface mod = modConstructor.newInstance();
						mod.initializeMod(this);
					} catch (Exception e) {
						throw new RuntimeException("Unable to initialize codemod "+className+": "+e);
					}
					
				} else if (actionTag.equals("inject")) {
					String target = actionAttrs.getNamedItem("target").getNodeValue();
					String code = actionNode.getTextContent();
					getTransformer().queueInjection(target, code);
				} else {
					throw new RuntimeException("Unexpected action tag in codemod config: "+actionTag);
				}
			}
		}
	}
	
	/* javaagent entrypoint */
	
	public static void premain(String agentArgument, Instrumentation instrumentation) {
		System.out.println("Modloader initializing core...");
		ModSystem sys = getInstance();
		Transformer transformer = sys.getTransformer();
		instrumentation.addTransformer(transformer);
		
		// hook right before Towns main loop to load configs and initialize mods
		transformer.queueInjection("<xaos.h.a.C()", "fi.muzzy.towns.modloader.ModSystem.getInstance().preMainloop();");
		// This is now done in config instead: disable steam api :)
		//transformer.queueInjection("=xaos.Towns.b(String)", "return false;");
		
		sys.loadMods();
	}
	
	public void preMainloop() {
		// TODO ... let mods know we hit mainloop
	}
	
	// Public API for mods to use during initialization

	/** getDataFile opens a File in Towns data directory */
	
	public File getDataFile(String filename) {
		return new File(townsIni.getProperty("DATA_FOLDER") + filename);
	}

		
	/* Public API for mods to use during runtime, but not during initialization
	 *
	 * Calling these methods during initialization will cause the game classes to be prematurely loaded, preventing injection
	 */
	
	/** getUserFile opens a File in Towns user directory */
	
	public File getUserFile(String filename) {
		return new File(xaos.h.a.a() + xaos.h.a.b() + filename);
	}

}
