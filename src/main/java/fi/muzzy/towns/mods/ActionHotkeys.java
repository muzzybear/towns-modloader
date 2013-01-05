package fi.muzzy.towns.mods;

import fi.muzzy.towns.modloader.*;

import java.util.HashMap;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

public class ActionHotkeys implements ModInterface {

	public void initializeMod(ModSystem system) {
		loadMenu(system.getDataFile("menu.xml"));
		loadMenu(system.getDataFile("menu_right.xml"));
		loadMenu(system.getDataFile("menu_production.xml"));
		
		system.getTransformer().queueInjection(
			"<xaos.h.b.k(int)",
			"{"+
			"  String[] foo = fi.muzzy.towns.mods.ActionHotkeys.getMenuAction($1);"+
			"  if (foo != null) xaos.i.a.a(foo[0], foo[1], null, null, null, 0);"+
			"}"
		);
	}

	private static HashMap<Integer, String[]> menu = new HashMap<Integer, String[]>();

	private static void loadMenu(File xmlFile) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
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
			System.err.println("Failed to load menu for ActionHotkeys mod: "+e);
		}
	}

	public static String[] getMenuAction(int keycode) {
		return menu.get(keycode);
	}
}
