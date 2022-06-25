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
package layouts;

import javax.xml.stream.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FullAppLayoutXML {
	private static final String NL = "\n";

	// saves a docking layout to the given file, returns true if successful, false otherwise
	public static boolean saveLayoutToFile(File file, FullAppLayout layout) {
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

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
			writer.writeCharacters(NL);
			writer.writeStartElement("app-layout");
			writer.writeCharacters(NL);

			DockingLayoutXML.saveLayoutToFile(writer, layout.getMainFrameLayout(), true);

			for (DockingLayout frameLayout : layout.getFloatingFrameLayouts()) {
				DockingLayoutXML.saveLayoutToFile(writer, frameLayout, false);
			}

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

	public static FullAppLayout loadLayoutFromFile(File file) {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader;
		try {
			reader = factory.createXMLStreamReader(new FileInputStream(file));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		FullAppLayout layout = new FullAppLayout();

		try {
			while (reader.hasNext()) {
				int next = reader.nextTag();

				if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("layout")) {
					layout.addFrame(DockingLayoutXML.readLayoutFromReader(reader));
				}
				else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("app-layout")) {
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
}
