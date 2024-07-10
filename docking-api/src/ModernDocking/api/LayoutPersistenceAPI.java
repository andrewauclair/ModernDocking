/*
Copyright (c) 2023-2024 Andrew Auclair

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
package ModernDocking.api;

import ModernDocking.Dockable;
import ModernDocking.Property;
import ModernDocking.exception.DockableRegistrationFailureException;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.internal.DockableProperties;
import ModernDocking.internal.DockableWrapper;
import ModernDocking.internal.DockingInternal;
import ModernDocking.layouts.*;

import javax.xml.stream.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutPersistenceAPI {
    private static final String NL = "\n";
    private final DockingAPI docking;

    protected LayoutPersistenceAPI(DockingAPI docking) {
        this.docking = docking;
    }

    /**
     * saves a docking layout to the given file
     *
     * @param file File to save the docking layout into
     * @param layout The layout to save
     * @throws DockingLayoutException Thrown if we failed to save the layout to the file
     */
    public void saveLayoutToFile(File file, ApplicationLayout layout) throws DockingLayoutException {
        // create the file if it doens't exist
        try {
            file.createNewFile();
        }
        catch (IOException e) {
            throw new DockingLayoutException(file, DockingLayoutException.FailureType.SAVE, e);
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

            saveLayoutToFile(writer, layout.getMainFrameLayout(), true);

            for (WindowLayout frameLayout : layout.getFloatingFrameLayouts()) {
                saveLayoutToFile(writer, frameLayout, false);
            }

            writer.writeStartElement("undocked");
            writer.writeCharacters(NL);

            for (Dockable dockable : DockingInternal.get(docking).getDockables()) {
                if (!docking.isDocked(dockable)) {
                    DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

                    writeSimpleNodeToFile(writer, new DockingSimplePanelNode(docking, dockable.getPersistentID(), dockable.getClass().getCanonicalName(), DockableProperties.saveProperties(wrapper)));
                }
            }

            writer.writeEndElement();
            writer.writeCharacters(NL);

            writer.writeEndElement();

            writer.writeEndDocument();

            writer.close();
        }
        catch (Exception e) {
            throw new DockingLayoutException(file, DockingLayoutException.FailureType.SAVE, e);
        }
    }

    /**
     * Load an ApplicationLayout from the specified file
     *
     * @param file File to load the ApplicationLayout from
     * @return ApplicationLayout loaded from the file
     * @throws DockingLayoutException Thrown if we failed to read from the file or something went wrong with loading the layout
     */
    public ApplicationLayout loadApplicationLayoutFromFile(File file) throws DockingLayoutException {
        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLStreamReader reader = null;

        try (InputStream in = Files.newInputStream(file.toPath())) {
            reader = factory.createXMLStreamReader(in);

            ApplicationLayout layout = new ApplicationLayout();

            while (reader.hasNext()) {
                int next = reader.nextTag();

                if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("layout")) {
                    layout.addFrame(readLayoutFromReader(reader));
                }
                else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("undocked")) {
                    readUndocked(reader);
                }
                else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("app-layout")) {
                    break;
                }
            }

            return layout;
        }
        catch (Exception e) {
            throw new DockingLayoutException(file, DockingLayoutException.FailureType.LOAD, e);
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

    // read undocked dockables from the file and configure their properties on the actual dockable already loaded in memory
    // if the dockable does not exist, we simply ignore it and the properties disappear.
    private void readUndocked(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("simple")) {

                    DockingSimplePanelNode node = readSimpleNodeFromFile(reader);

                    try {
                        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(DockingInternal.get(docking).getDockable(node.getPersistentID()));

                        DockableProperties.configureProperties(wrapper, node.getProperties());
                    }
                    catch (DockableRegistrationFailureException ignored) {
                    }
                }
            }
            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("undocked")) {
                break;
            }
        }
    }

    public boolean saveWindowLayoutToFile(File file, WindowLayout layout) {
        file.getParentFile().mkdirs();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer;
        try {
            writer = factory.createXMLStreamWriter(Files.newOutputStream(file.toPath()));
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

    private void saveLayoutToFile(XMLStreamWriter writer, WindowLayout layout, boolean isMainFrame) throws XMLStreamException {
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

        for (String id : layout.getWestAutoHideToolbarIDs()) {
            writer.writeStartElement("dockable");
            writer.writeAttribute("id", id);
            writer.writeEndElement();
            writer.writeCharacters(NL);
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);
        writer.writeStartElement("eastToolbar");
        writer.writeCharacters(NL);

        for (String id : layout.getEastAutoHideToolbarIDs()) {
            writer.writeStartElement("dockable");
            writer.writeAttribute("id", id);
            writer.writeEndElement();
            writer.writeCharacters(NL);
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);
        writer.writeStartElement("southToolbar");
        writer.writeCharacters(NL);

        for (String id : layout.getSouthAutoHideToolbarIDs()) {
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

    private void writeNodeToFile(XMLStreamWriter writer, DockingLayoutNode node) throws XMLStreamException {
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

    private void writeSimpleNodeToFile(XMLStreamWriter writer, DockingSimplePanelNode node) throws XMLStreamException {
        writer.writeStartElement("simple");
        writer.writeAttribute("persistentID", node.getPersistentID());
        writer.writeAttribute("class-name", DockingInternal.get(docking).getDockable(node.getPersistentID()).getClass().getCanonicalName());
        writer.writeCharacters(NL);

        writer.writeStartElement("properties");

        Map<String, Property> properties = node.getProperties();

        for (String key : properties.keySet()) {
            Property value = properties.get(key);

            if (value != null && !value.isNull()) {
                writer.writeStartElement("property");
                writer.writeAttribute("name", value.getName());
                writer.writeAttribute("type", value.getType().getSimpleName());
                writer.writeAttribute("value", value.toString());
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);

        writer.writeEndElement();
        writer.writeCharacters(NL);
    }

    private void writeSplitNodeToFile(XMLStreamWriter writer, DockingSplitPanelNode node) throws XMLStreamException {
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

    private void writeTabbedNodeToFile(XMLStreamWriter writer, DockingTabPanelNode node) throws XMLStreamException {
        writer.writeStartElement("tabbed");
        writer.writeCharacters(NL);

        writer.writeStartElement("selectedTab");
        writer.writeAttribute("persistentID", node.getSelectedTabID());
        writer.writeCharacters(NL);
        writer.writeEndElement();
        writer.writeCharacters(NL);

        for (DockingSimplePanelNode simpleNode : node.getPersistentIDs()) {
            writer.writeStartElement("tab");
            writer.writeAttribute("persistentID", simpleNode.getPersistentID());
            writer.writeCharacters(NL);

            writer.writeStartElement("properties");

            Map<String, Property> properties = simpleNode.getProperties();

            for (String key : properties.keySet()) {
                Property value = properties.get(key);

                if (value != null) {
                    writer.writeStartElement("property");
                    writer.writeAttribute("name", value.getName());
                    writer.writeAttribute("type", value.getType().getSimpleName());
                    writer.writeAttribute("value", value.toString());
                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
            writer.writeCharacters(NL);

            writer.writeEndElement();
            writer.writeCharacters(NL);
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);
    }

    /**
     * Load a WindowLayout from an XML file
     *
     * @param file File to load WindowLayout from
     * @return The loaded WindowLayout
     */
    public WindowLayout loadWindowLayoutFromFile(File file) {
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

    private WindowLayout readLayoutFromReader(XMLStreamReader reader) throws XMLStreamException {
        boolean isMainFrame = Boolean.parseBoolean(reader.getAttributeValue(0));
        String locStr = reader.getAttributeValue(1);
        String sizeStr = reader.getAttributeValue(2);
        int state = Integer.parseInt(reader.getAttributeValue(3));
        String maximizedDockable = reader.getAttributeCount() >= 5 ? reader.getAttributeValue(4) : null;

        Point location = new Point(Integer.parseInt(locStr.substring(0, locStr.indexOf(","))), Integer.parseInt(locStr.substring(locStr.indexOf(",") + 1)));
        Dimension size = new Dimension(Integer.parseInt(sizeStr.substring(0, sizeStr.indexOf(","))), Integer.parseInt(sizeStr.substring(sizeStr.indexOf(",") + 1)));

        java.util.List<String> westToolbar = readToolbarFromFile(reader, "westToolbar");
        java.util.List<String> eastToolbar = readToolbarFromFile(reader, "eastToolbar");
        java.util.List<String> southToolbar = readToolbarFromFile(reader, "southToolbar");

        WindowLayout layout = new WindowLayout(isMainFrame, location, size, state, readNodeFromFile(reader, "layout"));

        layout.setWestAutoHideToolbarIDs(westToolbar);
        layout.setEastAutoHideToolbarIDs(eastToolbar);
        layout.setSouthAutoHideToolbarIDs(southToolbar);

        layout.setMaximizedDockable(maximizedDockable);

        return layout;
    }

    private java.util.List<String> readToolbarFromFile(XMLStreamReader reader, String name) throws XMLStreamException {
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

    private DockingLayoutNode readNodeFromFile(XMLStreamReader reader, String name) throws XMLStreamException {
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

    private DockingSimplePanelNode readSimpleNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
        String persistentID = reader.getAttributeValue(0);
        String className = reader.getAttributeValue(1);

        return new DockingSimplePanelNode(docking, persistentID, className, readProperties(reader));
    }

    // expects that we haven't already read the starting element for <properties>
    private Map<String, Property> readProperties(XMLStreamReader reader) throws XMLStreamException {
        Map<String, Property> properties = new HashMap<>();

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("properties")) {
                    // old style of properties from before 0.12.0
                    if (reader.getAttributeCount() != 0) {
                        DockableProperties.setLoadingLegacyFile(true);

                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            Property prop = DockableProperties.parseProperty(reader.getAttributeLocalName(i), "String", reader.getAttributeValue(i));
                            properties.put(String.valueOf(reader.getAttributeName(i)), prop);
                        }
                    }
                    else {
                        DockableProperties.setLoadingLegacyFile(false);

                        while (reader.hasNext()) {
                            next = reader.nextTag();

                            if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("property")) {
                                String property = null;
                                String type = null;
                                String value = null;

                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String attributeLocalName = reader.getAttributeLocalName(i);

                                    switch (attributeLocalName) {
                                        case "name":
                                            property = reader.getAttributeValue(i);
                                            break;
                                        case "type":
                                            type = reader.getAttributeValue(i);
                                            break;
                                        case "value":
                                            value = reader.getAttributeValue(i);
                                            break;
                                    }
                                }
                                if (property != null && type != null && value != null) {
                                    Property parsedProperty = DockableProperties.parseProperty(property, type, value);
                                    properties.put(parsedProperty.getName(), parsedProperty);
                                }
                            }
                            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("properties")) {
                                break;
                            }
                        }
                    }
                }
            }
            if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("properties")) {
                break;
            }
        }
        return properties;
    }

    private DockingSplitPanelNode readSplitNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
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
        return new DockingSplitPanelNode(docking, left, right, orientation, dividerProportion);
    }

    private DockingTabPanelNode readTabNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
        DockingTabPanelNode node = null;

        String currentPersistentID = "";

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("selectedTab")) {
                String persistentID = reader.getAttributeValue(0);
                node = new DockingTabPanelNode(docking, persistentID);
            }
            else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("tab")) {
                currentPersistentID = reader.getAttributeValue(0);

                if (node != null) {
                    node.addTab(currentPersistentID);
                }
            }
            else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("properties")) {
                Map<String, Property> properties = new HashMap<>();

                // old style of properties from before 0.12.0
                if (reader.getAttributeCount() != 0) {
                    DockableProperties.setLoadingLegacyFile(true);

                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        Property prop = DockableProperties.parseProperty(reader.getAttributeLocalName(i), "String", reader.getAttributeValue(i));
                        properties.put(String.valueOf(reader.getAttributeName(i)), prop);
                    }
                }
                else {
                    DockableProperties.setLoadingLegacyFile(false);

                    while (reader.hasNext()) {
                        next = reader.nextTag();

                        if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("property")) {
                            String property = null;
                            String type = null;
                            String value = null;

                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                String attributeLocalName = reader.getAttributeLocalName(i);

                                switch (attributeLocalName) {
                                    case "name":
                                        property = reader.getAttributeValue(i);
                                        break;
                                    case "type":
                                        type = reader.getAttributeValue(i);
                                        break;
                                    case "value":
                                        value = reader.getAttributeValue(i);
                                        break;
                                }
                            }
                            if (property != null && type != null && value != null) {
                                Property parsedProperty = DockableProperties.parseProperty(property, type, value);
                                properties.put(parsedProperty.getName(), parsedProperty);
                            }
                        }
                        else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("properties")) {
                            break;
                        }
                    }
                }

                if (node != null) {
                    node.setProperties(currentPersistentID, properties);
                }
            }
            if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("tabbed")) {
                break;
            }
        }
        return node;
    }
}
