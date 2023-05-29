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

import ModernDocking.exception.DockingLayoutException;

import javax.xml.stream.*;
import java.io.*;
import java.nio.file.Files;

public class ApplicationLayoutXML {
	private static final String NL = "\n";

	// saves a docking layout to the given file, returns true if successful, false otherwise
	public static boolean saveLayoutToFile(File file, ApplicationLayout layout) throws DockingLayoutException {
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
			writer.writeCharacters(NL);

			WindowLayoutXML.saveLayoutToFile(writer, layout.getMainFrameLayout(), true);

			for (WindowLayout frameLayout : layout.getFloatingFrameLayouts()) {
				WindowLayoutXML.saveLayoutToFile(writer, frameLayout, false);
			}

			writer.writeEndElement();

			writer.writeEndDocument();

			writer.close();
		}
		catch (Exception e) {
			throw new DockingLayoutException(e);
		}
		return true;
	}

	public static ApplicationLayout loadLayoutFromFile(File file) throws DockingLayoutException {
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
}
