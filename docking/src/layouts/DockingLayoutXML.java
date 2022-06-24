package layouts;

import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DockingLayoutXML {
	private static final String NL = "\n";

	// saves a docking layout to the given file, returns true if successful, false otherwise
	public static boolean saveLayoutToFile(File file, DockingLayout layout) {
		file.getParentFile().mkdirs();

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = null;
		try {
			writer = factory.createXMLStreamWriter(new FileOutputStream(file));
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		try {
			writer.writeStartDocument();
			writer.writeCharacters(NL);
			writer.writeStartElement("layout");
			writer.writeCharacters(NL);

			writeNodeToFile(writer, layout.getRootNode());

			writer.writeEndElement();

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
		writer.writeAttribute("persistentID", node.persistentID());
		writer.writeCharacters(NL);
		writer.writeEndElement();
		writer.writeCharacters(NL);
	}

	private static void writeSplitNodeToFile(XMLStreamWriter writer, DockingSplitPanelNode node) throws XMLStreamException {
		writer.writeStartElement("split");
		writer.writeAttribute("orientation", String.valueOf(node.getOrientation()));
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

	public static DockingLayout loadLayoutFromFile(File file) {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader;
		try {
			reader = factory.createXMLStreamReader(new FileInputStream(file));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		DockingLayout layout = null;

		try {
			while (reader.hasNext()) {
				int next = reader.nextTag();

				if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("layout")) {
					layout = new DockingLayout(readNodeFromFile(reader, "layout"));
					break;
				}
			}
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
			layout = null;
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
		return new DockingSimplePanelNode(persistentID);
	}

	private static DockingSplitPanelNode readSplitNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
		DockingLayoutNode left = null;
		DockingLayoutNode right = null;

		int orientation = Integer.parseInt(reader.getAttributeValue(0));

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
		return new DockingSplitPanelNode(left, right, orientation);
	}

	private static DockingTabPanelNode readTabNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
		DockingTabPanelNode node = new DockingTabPanelNode();

		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("tab")) {
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
