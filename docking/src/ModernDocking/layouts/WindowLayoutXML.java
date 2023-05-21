/*
Copyright (c) 2022 Andrew Auclair

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package ModernDocking.layouts;

import javax.xml.stream.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowLayoutXML {
	private static final String NL = "\n";

	// saves a docking layout to the given file, returns true if successful, false otherwise
	public static boolean saveLayoutToFile(File file, WindowLayout layout) {
		file.getParentFile().mkdirs();

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		try {
			writer = factory.createXMLStreamWriter(new FileOutputStream(file));
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		try {
			writer.writeStartDocument();

			saveLayoutToFile(writer, layout, false);

			writer.writeEndDocument();
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				writer.close();
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	static void saveLayoutToFile(XMLStreamWriter writer, WindowLayout layout, boolean isMainFrame) throws XMLStreamException {
		writer.writeCharacters(NL);
		writer.writeStartElement("layout");
		writer.writeAttribute("main-frame", String.valueOf(isMainFrame));
		writer.writeAttribute("location", layout.getLocation().x + "," + layout.getLocation().y);
		writer.writeAttribute("size", layout.getSize().width + "," + layout.getSize().height);
		writer.writeAttribute("state", String.valueOf(layout.getState()));

		if (layout.getMaximizedDockable() != null) {
			writer.writeAttribute("max-dockable", layout.getMaximizedDockable());
		}
		writer.writeCharacters(NL);

		writer.writeStartElement("westToolbar");
		writer.writeCharacters(NL);

		for (String id : layout.getWestUnpinnedToolbarIDs()) {
			writer.writeStartElement("dockable");
			writer.writeAttribute("id", id);
			writer.writeEndElement();
			writer.writeCharacters(NL);
		}

		writer.writeEndElement();
		writer.writeCharacters(NL);
		writer.writeStartElement("eastToolbar");
		writer.writeCharacters(NL);

		for (String id : layout.getEastUnpinnedToolbarIDs()) {
			writer.writeStartElement("dockable");
			writer.writeAttribute("id", id);
			writer.writeEndElement();
			writer.writeCharacters(NL);
		}

		writer.writeEndElement();
		writer.writeCharacters(NL);
		writer.writeStartElement("southToolbar");
		writer.writeCharacters(NL);

		for (String id : layout.getSouthUnpinnedToolbarIDs()) {
			writer.writeStartElement("dockable");
			writer.writeAttribute("id", id);
			writer.writeEndElement();
			writer.writeCharacters(NL);
		}

		writer.writeEndElement();
		writer.writeCharacters(NL);

		writeNodeToFile(writer, layout.getRootNode());

		writer.writeEndElement();
		writer.writeCharacters(NL);
	}

	private static void writeNodeToFile(XMLStreamWriter writer, DockingLayoutNode node) throws XMLStreamException {
		if (node instanceof DockingSimplePanelNode) {
			writeSimpleNodeToFile(writer, (DockingSimplePanelNode) node);
		}
		else if (node instanceof DockingSplitPanelNode) {
			writeSplitNodeToFile(writer, (DockingSplitPanelNode) node);
		}
		else if (node instanceof DockingTabPanelNode) {
			writeTabbedNodeToFile(writer, (DockingTabPanelNode) node);
		}
	}

	private static void writeSimpleNodeToFile(XMLStreamWriter writer, DockingSimplePanelNode node) throws XMLStreamException {
		writer.writeStartElement("simple");
		writer.writeAttribute("persistentID", node.getPersistentID());
		writer.writeCharacters(NL);

		writer.writeStartElement("properties");

		Map<String, String> properties = node.getProperties();

		for (String key : properties.keySet()) {
			String value = properties.get(key);

			writer.writeAttribute(key, value);
//			writer.writeCharacters(NL);
		}

		writer.writeEndElement();
		writer.writeCharacters(NL);

		writer.writeCharacters(NL);
		writer.writeEndElement();
		writer.writeCharacters(NL);
	}

	private static void writeSplitNodeToFile(XMLStreamWriter writer, DockingSplitPanelNode node) throws XMLStreamException {
		writer.writeStartElement("split");
		writer.writeAttribute("orientation", String.valueOf(node.getOrientation()));
		writer.writeAttribute("divider-proportion", String.valueOf(node.getDividerProportion()));
		writer.writeCharacters(NL);

		writer.writeStartElement("left");
		writer.writeCharacters(NL);
		writeNodeToFile(writer, node.getLeft());
		writer.writeEndElement();
		writer.writeCharacters(NL);

		writer.writeStartElement("right");
		writer.writeCharacters(NL);
		writeNodeToFile(writer, node.getRight());
		writer.writeEndElement();
		writer.writeCharacters(NL);

		writer.writeEndElement();
		writer.writeCharacters(NL);
	}

	private static void writeTabbedNodeToFile(XMLStreamWriter writer, DockingTabPanelNode node) throws XMLStreamException {
		writer.writeStartElement("tabbed");
		writer.writeCharacters(NL);

		writer.writeStartElement("selectedTab");
		writer.writeAttribute("persistentID", node.getSelectedTabID());
		writer.writeCharacters(NL);
		writer.writeEndElement();
		writer.writeCharacters(NL);

		for (String persistentID : node.getPersistentIDs()) {
			writer.writeStartElement("tab");
			writer.writeAttribute("persistentID", persistentID);
			writer.writeCharacters(NL);
			writer.writeEndElement();
			writer.writeCharacters(NL);
		}

		writer.writeEndElement();
		writer.writeCharacters(NL);
	}

	public static WindowLayout loadLayoutFromFile(File file) {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader;
		try {
			reader = factory.createXMLStreamReader(Files.newInputStream(file.toPath()));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		WindowLayout layout = null;

		try {
			while (reader.hasNext()) {
				int next = reader.nextTag();

				if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("layout")) {
					layout = readLayoutFromReader(reader);
					break;
				}
			}
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
		finally {
			try {
				reader.close();
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		return layout;
	}

	static WindowLayout readLayoutFromReader(XMLStreamReader reader) throws XMLStreamException {
		boolean isMainFrame = Boolean.parseBoolean(reader.getAttributeValue(0));
		String locStr = reader.getAttributeValue(1);
		String sizeStr = reader.getAttributeValue(2);
		int state = Integer.parseInt(reader.getAttributeValue(3));
		String maximizedDockable = reader.getAttributeValue(4);

		Point location = new Point(Integer.parseInt(locStr.substring(0, locStr.indexOf(","))), Integer.parseInt(locStr.substring(locStr.indexOf(",") + 1)));
		Dimension size = new Dimension(Integer.parseInt(sizeStr.substring(0, sizeStr.indexOf(","))), Integer.parseInt(sizeStr.substring(sizeStr.indexOf(",") + 1)));

		List<String> westToolbar = readToolbarFromFile(reader, "westToolbar");
		List<String> eastToolbar = readToolbarFromFile(reader, "eastToolbar");
		List<String> southToolbar = readToolbarFromFile(reader, "southToolbar");

		WindowLayout layout = new WindowLayout(isMainFrame, location, size, state, readNodeFromFile(reader, "layout"));

		layout.setWestUnpinnedToolbarIDs(westToolbar);
		layout.setEastUnpinnedToolbarIDs(eastToolbar);
		layout.setSouthUnpinnedToolbarIDs(southToolbar);

		layout.setMaximizedDockable(maximizedDockable);

		return layout;
	}

	private static List<String> readToolbarFromFile(XMLStreamReader reader, String name) throws XMLStreamException {
		List<String> ids = new ArrayList<>();

		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT) {
				if (reader.getLocalName().equals("dockable")) {
					ids.add(reader.getAttributeValue(0));
				}
			}
			else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(name)) {
				break;
			}
		}
		return ids;
	}

	private static DockingLayoutNode readNodeFromFile(XMLStreamReader reader, String name) throws XMLStreamException {
		DockingLayoutNode node = null;
		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT) {
				if (reader.getLocalName().equals("simple")) {
					node = readSimpleNodeFromFile(reader);
				}
				else if (reader.getLocalName().equals("split")) {
					node = readSplitNodeFromFile(reader);
				}
				else if (reader.getLocalName().equals("tabbed")) {
					node = readTabNodeFromFile(reader);
				}
			}
			else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(name)) {
				break;
			}
		}
		return node;
	}

	private static DockingSimplePanelNode readSimpleNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
		String persistentID = reader.getAttributeValue(0);


		return new DockingSimplePanelNode(persistentID, readProperties(reader));
	}

	private static Map<String, String> readProperties(XMLStreamReader reader) throws XMLStreamException {
		Map<String, String> properties = new HashMap<>();

		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT) {
				if (reader.getLocalName().equals("properties")) {
					for (int i = 0; i < reader.getAttributeCount(); i++) {
						properties.put(String.valueOf(reader.getAttributeName(i)), reader.getAttributeValue(i));
					}
//					return properties;
				}
			}
			else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("properties")) {
				break;
			}
		}
		return properties;
	}

	private static DockingSplitPanelNode readSplitNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
		DockingLayoutNode left = null;
		DockingLayoutNode right = null;

		int orientation = Integer.parseInt(reader.getAttributeValue(0));
		double dividerProportion = Double.parseDouble(reader.getAttributeValue(1));

		if (dividerProportion < 0.0) {
			dividerProportion = 0.0;
		}
		else if (dividerProportion > 1.0) {
			dividerProportion = 1.0;
		}

		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT) {
				if (reader.getLocalName().equals("left")) {
					left = readNodeFromFile(reader, "left");
				}
				else if (reader.getLocalName().equals("right")) {
					right = readNodeFromFile(reader, "right");
				}
			}
			else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("split")) {
				break;
			}
		}
		return new DockingSplitPanelNode(left, right, orientation, dividerProportion);
	}

	private static DockingTabPanelNode readTabNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
		DockingTabPanelNode node = new DockingTabPanelNode("");

		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("selectedTab")) {
				String persistentID = reader.getAttributeValue(0);
				node = new DockingTabPanelNode(persistentID);
			}
			else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("tab")) {
				String persistentID = reader.getAttributeValue(0);
				node.addTab(persistentID);
			}
			else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("tabbed")) {
				break;
			}
		}
		return node;
	}
}
