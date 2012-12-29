package fi.muzzy.towns.mods;

import java.lang.instrument.*;
import fi.muzzy.towns.modloader.*;
import javassist.*;

import java.util.HashMap;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

public class ActionHotkeys implements ModInterface, ClassFileTransformer {

	public void initializeMod(ModSystem system) {
		system.getTransformer().hookClassTransformation("xaos/h/b", this);
	}

	private static HashMap<Integer, String[]> menu;

	private static void loadMenu(String menuXmlPath) {
		menu = new HashMap<Integer, String[]>();

		// load menu.xml and parse hotkeys
		try {
			File fXmlFile = new File(menuXmlPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			NodeList nodes = doc.getElementsByTagName("item");
			for (int i=0; i<nodes.getLength(); i++) {
				NamedNodeMap attrs = nodes.item(i).getAttributes();
				if (attrs.getNamedItem("hotkey") != null) {
					String code = attrs.getNamedItem("code").getNodeValue();
					String param = attrs.getNamedItem("parameter") != null ? attrs.getNamedItem("parameter").getNodeValue() : null;
					int hotkey = new Integer(attrs.getNamedItem("hotkey").getNodeValue()).intValue();
					menu.put(hotkey, new String[]{code, param});
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to load menu.xml for ActionHotkeys mod");
		}
	}

	public static String[] getMenuAction(String menuXmlPath, int keycode) {
		if (menu == null)
			loadMenu(menuXmlPath);

		return menu.get(keycode);
	}

	public byte[] transform(ClassLoader loader, String className,
		Class clazz, java.security.ProtectionDomain domain, byte[] bytes)
	{
		// TODO: hook some function where config is loaded and don't try to load it everytime on keypress
		if (className.equals("xaos/h/b")) {
			ClassPool pool = ClassPool.getDefault();
			CtClass cl = null;
			try {
				cl = pool.makeClass(new java.io.ByteArrayInputStream(bytes));

				CtMethod m = cl.getDeclaredMethod("k", new CtClass[]{CtClass.intType});
				m.insertBefore(
					"if ($1 >= 2 && $1 <= 11) {"+
					"  String[] foo = fi.muzzy.towns.mods.ActionHotkeys.getMenuAction(xaos.Towns.a(\"DATA_FOLDER\")+\"menu.xml\", $1);"+
					"  if (foo != null) xaos.i.a.a(foo[0], foo[1], null, null, null, 0);"+
					"}"
				);
				bytes = cl.toBytecode();
			} catch (Exception e) {
				System.err.println("Failed to install ActionHotkeys hook: "+e);
			} finally {
				if (cl != null) {
					cl.detach();
				}
			}

		}

		return bytes;
	}

/*	public static void keypress(int keycode) {
		if (keycode >= 2 && keycode <= 11) {
			// numeric keys 1-0
			System.out.println("Keypress caught: "+keycode);
		}
	}
*/
}
