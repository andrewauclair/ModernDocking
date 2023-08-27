/*
Copyright (c) 2022-2023 Andrew Auclair

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

import ModernDocking.Dockable;
import ModernDocking.Docking;
import ModernDocking.DockingInstance;
import ModernDocking.DockingProperty;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.internal.DockableProperties;
import ModernDocking.internal.DockingInternal;

import javax.xml.stream.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Helper class to save and load ApplicationLayouts to/from XML formatted files
 */
public class ApplicationLayoutXML {
	private static final String NL = "\n";

	public static void saveLayoutToFile(File file, ApplicationLayout layout) throws DockingLayoutException {
		saveLayoutToFile(Docking.getSingleInstance(), file, layout);
	}
	/**
	 * saves a docking layout to the given file
	 *
	 * @param file File to save the docking layout into
	 * @param layout The layout to save
	 * @throws DockingLayoutException Thrown if we failed to save the layout to the file
	 */
	public static void saveLayoutToFile(DockingInstance docking, File file, ApplicationLayout layout) throws DockingLayoutException {
		// create the file if it doens't exist
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			throw new DockingLayoutException(e);
		}

		// make sure all the required directories exist
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

		XMLOutputFactory factory = XMLOutputFactory.newInstance();

		try (OutputStream out = Files.newOutputStream(file.toPath())) {
			XMLStreamWriter writer = factory.createXMLStreamWriter(out);

			writer.writeStartDocument();
			writer.writeCharacters(NL);
			writer.writeStartElement("app-layout");

			WindowLayoutXML.saveLayoutToFile(writer, layout.getMainFrameLayout(), true);

			for (WindowLayout frameLayout : layout.getFloatingFrameLayouts()) {
				WindowLayoutXML.saveLayoutToFile(writer, frameLayout, false);
			}

			writer.writeStartElement("undocked");
			writer.writeCharacters(NL);

			for (Dockable dockable : docking.getDockables()) {
				if (!docking.isDocked(dockable)) {
					WindowLayoutXML.writeSimpleNodeToFile(writer, new DockingSimplePanelNode(docking, dockable.getPersistentID(), dockable.getClass().getCanonicalName(), DockableProperties.saveProperties(dockable)));
				}
			}

			writer.writeEndElement();
			writer.writeCharacters(NL);

			writer.writeEndElement();

			writer.writeEndDocument();

			writer.close();
		}
		catch (Exception e) {
			throw new DockingLayoutException(e);
		}
	}

	/**
	 * Load an ApplicationLayout from the specified file
	 *
	 * @param file File to load the ApplicationLayout from
	 * @return ApplicationLayout loaded from the file
	 * @throws DockingLayoutException Thrown if we failed to read from the file or something went wrong with loading the layout
	 */
	public static ApplicationLayout loadLayoutFromFile(File file) throws DockingLayoutException {
		return loadLayoutFromFile(file, Docking.getSingleInstance());
	}

	/**
	 * Load an ApplicationLayout from the specified file
	 *
	 * @param file File to load the ApplicationLayout from
	 * @return ApplicationLayout loaded from the file
	 * @throws DockingLayoutException Thrown if we failed to read from the file or something went wrong with loading the layout
	 */
	public static ApplicationLayout loadLayoutFromFile(File file, DockingInstance docking) throws DockingLayoutException {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		XMLStreamReader reader = null;

		try (InputStream in = Files.newInputStream(file.toPath())) {
			reader = factory.createXMLStreamReader(in);

			ApplicationLayout layout = new ApplicationLayout();

			while (reader.hasNext()) {
				int next = reader.nextTag();

				if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("layout")) {
					layout.addFrame(WindowLayoutXML.readLayoutFromReader(reader));
				}
				else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("undocked")) {
					readUndocked(reader, docking);
				}
				else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("app-layout")) {
					break;
				}
			}

			return layout;
		}
		catch (Exception e) {
			throw new DockingLayoutException(e);
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
	}

	private static void readUndocked(XMLStreamReader reader, DockingInstance docking) throws XMLStreamException {
		while (reader.hasNext()) {
			int next = reader.nextTag();

			if (next == XMLStreamConstants.START_ELEMENT) {
				if (reader.getLocalName().equals("simple")) {
					DockingSimplePanelNode node = WindowLayoutXML.readSimpleNodeFromFile(reader);

					DockableProperties.configureProperties(docking.getDockable(node.getPersistentID()), node.getProperties());
				}
			}
			else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("undocked")) {
				break;
			}
		}
	}
}
