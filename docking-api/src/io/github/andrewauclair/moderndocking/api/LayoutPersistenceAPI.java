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
package io.github.andrewauclair.moderndocking.api;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.Property;
import io.github.andrewauclair.moderndocking.exception.DockableNotFoundException;
import io.github.andrewauclair.moderndocking.exception.DockableRegistrationFailureException;
import io.github.andrewauclair.moderndocking.exception.DockingLayoutException;
import io.github.andrewauclair.moderndocking.internal.DockableProperties;
import io.github.andrewauclair.moderndocking.internal.DockableWrapper;
import io.github.andrewauclair.moderndocking.internal.DockingInternal;
import io.github.andrewauclair.moderndocking.layouts.ApplicationLayout;
import io.github.andrewauclair.moderndocking.layouts.DockingAnchorPanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingLayoutNode;
import io.github.andrewauclair.moderndocking.layouts.DockingSimplePanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingSplitPanelNode;
import io.github.andrewauclair.moderndocking.layouts.DockingTabPanelNode;
import io.github.andrewauclair.moderndocking.layouts.WindowLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Save and load layouts to/from files and streams
 */
public class LayoutPersistenceAPI {
    private static final Logger logger = Logger.getLogger(LayoutPersistenceAPI.class.getPackageName());

    private static final String NL = "\n";

    private static final String TAG_LAYOUT = "layout";
    private static final String TAG_UNDOCKED = "undocked";
    private static final String TAG_APP_LAYOUT = "app-layout";
    private static final String TAG_SIMPLE = "simple";
    private static final String TAG_DOCKABLE = "dockable";
    private static final String TAG_SLIDE_POSITION = "slidePosition";
    private static final String TAG_PERSISTENT_ID = "persistentID";
    private static final String TAG_CLASS_NAME = "class-name";
    private static final String TAG_ANCHOR = "anchor";
    private static final String TAG_TITLE_TEXT = "title-text";
    private static final String TAG_TAB_TEXT = "tab-text";
    private static final String TAG_PROPERTIES = "properties";
    private static final String TAG_PROPERTY = "property";
    private static final String TAG_VALUE = "value";
    private static final String TAG_SPLIT = "split";
    private static final String TAG_RIGHT = "right";
    private static final String TAG_LEFT = "left";

    private final DockingAPI docking;

    private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private class ToolbarDockable {
        String id;
        double slidePosition;
    };

    /**
     * Create a new instance of the layout persistence API
     *
     * @param docking The docking instance this belongs to
     */
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
        // make sure all the required directories exist
        if (file.getParentFile() != null) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
        }

        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            saveLayoutToOutputStream(out, layout);
        } catch (Exception e) {
            throw new DockingLayoutException(file, DockingLayoutException.FailureType.SAVE, e);
        }
    }

    /**
     * Save the application layout to an output stream
     *
     * @param out The output stream to write the layout to
     * @param layout The layout to save
     * @throws XMLStreamException Thrown if there are any XML issues while saving
     */
    public void saveLayoutToOutputStream(final OutputStream out, final  ApplicationLayout layout) throws XMLStreamException {
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(out);

        writer.writeStartDocument();
        writer.writeCharacters(NL);
        writer.writeStartElement(TAG_APP_LAYOUT);

        saveLayoutToFile(writer, layout.getMainFrameLayout(), true);

        for (WindowLayout frameLayout : layout.getFloatingFrameLayouts()) {
            saveLayoutToFile(writer, frameLayout, false);
        }

        writer.writeStartElement(TAG_UNDOCKED);
        writer.writeCharacters(NL);

        for (Dockable dockable : DockingInternal.get(docking).getDockables()) {
            if (!docking.isDocked(dockable)) {
                DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(dockable);

                writeSimpleNodeToFile(writer, new DockingSimplePanelNode(docking, dockable.getPersistentID(), dockable.getClass().getTypeName(), "", dockable.getTitleText(), dockable.getTabText(), DockableProperties.saveProperties(wrapper)));
            }
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);

        writer.writeEndElement();

        writer.writeEndDocument();

        writer.close();
    }

    /**
     * Load an ApplicationLayout from the specified file
     *
     * @param file File to load the ApplicationLayout from
     * @return ApplicationLayout loaded from the file
     * @throws DockingLayoutException Thrown if we failed to read from the file or something went wrong with loading the layout
     */
    public ApplicationLayout loadApplicationLayoutFromFile(File file) throws DockingLayoutException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return loadApplicationLayoutFromInputStream(in);
        } catch (Exception e) {
            throw new DockingLayoutException(file, DockingLayoutException.FailureType.LOAD, e);
        }
    }

    /**
     * Load an application layout from an input stream
     *
     * @param in The input stream to read from
     * @return The new application layout
     * @throws XMLStreamException Thrown if the XML is not properly formatted
     */
    public ApplicationLayout loadApplicationLayoutFromInputStream(final InputStream in) throws XMLStreamException {
        XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
        try {
            ApplicationLayout layout = new ApplicationLayout();

            while (reader.hasNext()) {
                int next = reader.nextTag();

                if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(TAG_LAYOUT)) {
                    layout.addFrame(readLayoutFromReader(reader));
                }
                else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(TAG_UNDOCKED)) {
                    readUndocked(reader);
                }
                else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_APP_LAYOUT)) {
                    break;
                }
            }

            return layout;
        } finally {
            reader.close();
        }
    }

    // read undocked dockables from the file and configure their properties on the actual dockable already loaded in memory
    // if the dockable does not exist, we simply ignore it and the properties disappear.
    private void readUndocked(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals(TAG_SIMPLE)) {

                    DockingSimplePanelNode node = readSimpleNodeFromFile(reader);

                    try {
                        DockableWrapper wrapper = DockingInternal.get(docking).getWrapper(DockingInternal.get(docking).getDockable(node.getPersistentID()));

                        DockableProperties.configureProperties(wrapper, node.getProperties());
                    }
                    catch (DockableRegistrationFailureException | DockableNotFoundException ignored) {
                    }
                }
            }
            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_UNDOCKED)) {
                break;
            }
        }
    }

    /**
     * Save a window layout to a file
     *
     * @param file The file to save to
     * @param layout The layout to save
     *
     * @return True if the file was successfully saved, false otherwise
     */
    public boolean saveWindowLayoutToFile(File file, WindowLayout layout) {
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer;
        try {
            writer = factory.createXMLStreamWriter(Files.newOutputStream(file.toPath()));
        }
        catch (Exception e) {
            logger.log(Level.INFO, e.getMessage(), e);
            return false;
        }

        try {
            writer.writeStartDocument();

            saveLayoutToFile(writer, layout, false);

            writer.writeEndDocument();
        }
        catch (XMLStreamException e) {
            logger.log(Level.INFO, e.getMessage(), e);
            return false;
        }
        finally {
            try {
                writer.close();
            }
            catch (XMLStreamException e) {
                logger.log(Level.INFO, e.getMessage(), e);
            }
        }

        return true;
    }

    private void saveLayoutToFile(XMLStreamWriter writer, WindowLayout layout, boolean isMainFrame) throws XMLStreamException {
        writer.writeCharacters(NL);
        writer.writeStartElement(TAG_LAYOUT);
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
            writer.writeStartElement(TAG_DOCKABLE);
            writer.writeAttribute("id", id);
            writer.writeAttribute(TAG_SLIDE_POSITION, String.valueOf(layout.slidePosition(id)));
            writer.writeEndElement();
            writer.writeCharacters(NL);
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);
        writer.writeStartElement("eastToolbar");
        writer.writeCharacters(NL);

        for (String id : layout.getEastAutoHideToolbarIDs()) {
            writer.writeStartElement(TAG_DOCKABLE);
            writer.writeAttribute("id", id);
            writer.writeAttribute(TAG_SLIDE_POSITION, String.valueOf(layout.slidePosition(id)));
            writer.writeEndElement();
            writer.writeCharacters(NL);
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);
        writer.writeStartElement("southToolbar");
        writer.writeCharacters(NL);

        for (String id : layout.getSouthAutoHideToolbarIDs()) {
            writer.writeStartElement(TAG_DOCKABLE);
            writer.writeAttribute("id", id);
            writer.writeAttribute(TAG_SLIDE_POSITION, String.valueOf(layout.slidePosition(id)));
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
        else if (node instanceof DockingAnchorPanelNode) {
            writeAnchorNodeToFile(writer, (DockingAnchorPanelNode) node);
        }
    }

    private void writeSimpleNodeToFile(XMLStreamWriter writer, DockingSimplePanelNode node) throws XMLStreamException {
        writer.writeStartElement("simple");
        writer.writeAttribute(TAG_PERSISTENT_ID, node.getPersistentID());
        writer.writeAttribute(TAG_CLASS_NAME, DockingInternal.get(docking).getDockable(node.getPersistentID()).getClass().getTypeName());
        if (node.getAnchor() != null) {
            writer.writeAttribute(TAG_ANCHOR, node.getAnchor());
        }
        writer.writeAttribute(TAG_TITLE_TEXT, node.getTitleText());
        writer.writeAttribute(TAG_TAB_TEXT, node.getTabText());
        writer.writeCharacters(NL);

        writer.writeStartElement(TAG_PROPERTIES);

        Map<String, Property> properties = node.getProperties();

        for (Property value : properties.values()) {
            if (value != null && !value.isNull()) {
                writer.writeStartElement(TAG_PROPERTY);
                writer.writeAttribute("name", value.getName());
                writer.writeAttribute("type", value.getType().getSimpleName());
                writer.writeAttribute(TAG_VALUE, value.toString());
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);

        writer.writeEndElement();
        writer.writeCharacters(NL);
    }

    private void writeSplitNodeToFile(XMLStreamWriter writer, DockingSplitPanelNode node) throws XMLStreamException {
        double[] positions = node.getDividerPositions();
        List<DockingLayoutNode> nodeChildren = node.getChildren();

        writer.writeStartElement(TAG_SPLIT);
        writer.writeAttribute("orientation", String.valueOf(node.getOrientation()));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(positions[i]);
        }
        writer.writeAttribute("divider-positions", sb.toString());
        writer.writeCharacters(NL);

        for (DockingLayoutNode child : nodeChildren) {
            writeNodeToFile(writer, child);
        }

        writer.writeEndElement();
        writer.writeCharacters(NL);
    }

    private void writeTabbedNodeToFile(XMLStreamWriter writer, DockingTabPanelNode node) throws XMLStreamException {
        writer.writeStartElement("tabbed");
        writer.writeCharacters(NL);

        writer.writeStartElement("selectedTab");
        Dockable selectedTab = DockingInternal.get(docking).getDockable(node.getSelectedTabID());
        writer.writeAttribute(TAG_CLASS_NAME, selectedTab.getClass().getTypeName());
        writer.writeAttribute(TAG_PERSISTENT_ID, node.getSelectedTabID());
        writer.writeAttribute(TAG_ANCHOR, node.getAnchor());
        writer.writeAttribute(TAG_TITLE_TEXT, selectedTab.getTitleText());
        writer.writeAttribute(TAG_TAB_TEXT, selectedTab.getTabText());
        writer.writeCharacters(NL);
        writer.writeEndElement();
        writer.writeCharacters(NL);

        for (DockingSimplePanelNode simpleNode : node.getPersistentIDs()) {
            writer.writeStartElement("tab");
            writer.writeAttribute(TAG_PERSISTENT_ID, simpleNode.getPersistentID());
            writer.writeAttribute(TAG_CLASS_NAME, DockingInternal.get(docking).getDockable(simpleNode.getPersistentID()).getClass().getTypeName());
            writer.writeAttribute(TAG_ANCHOR, simpleNode.getAnchor());
            writer.writeAttribute(TAG_TITLE_TEXT, simpleNode.getTitleText());
            writer.writeAttribute(TAG_TAB_TEXT, simpleNode.getTabText());
            writer.writeCharacters(NL);

            writer.writeStartElement(TAG_PROPERTIES);

            Map<String, Property> properties = simpleNode.getProperties();

            for (Property value : properties.values()) {
                if (value != null && !value.isNull()) {
                    writer.writeStartElement(TAG_PROPERTY);
                    writer.writeAttribute("name", value.getName());
                    writer.writeAttribute("type", value.getType().getSimpleName());
                    if (value.toString() == null) {
                        writer.writeAttribute(TAG_VALUE, "");
                    }
                    else {
                        writer.writeAttribute(TAG_VALUE, value.toString());
                    }
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

    private void writeAnchorNodeToFile(XMLStreamWriter writer, DockingAnchorPanelNode node) throws XMLStreamException {
        writer.writeStartElement(TAG_ANCHOR);
        writer.writeAttribute(TAG_PERSISTENT_ID, node.getPersistentID());
        writer.writeAttribute(TAG_CLASS_NAME, DockingInternal.get(docking).getDockable(node.getPersistentID()).getClass().getTypeName());
        writer.writeCharacters(NL);

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
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        XMLStreamReader reader;
        try {
            reader = factory.createXMLStreamReader(Files.newInputStream(file.toPath()));
        }
        catch (Exception e) {
            logger.log(Level.INFO, e.getMessage(), e);
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
            logger.log(Level.INFO, e.getMessage(), e);
        }
        finally {
            try {
                reader.close();
            }
            catch (XMLStreamException e) {
                logger.log(Level.INFO, e.getMessage(), e);
            }
        }
        return layout;
    }

    private WindowLayout readLayoutFromReader(XMLStreamReader reader) throws XMLStreamException {
        boolean isMainFrame = Boolean.parseBoolean(reader.getAttributeValue(null, "main-frame"));
        String locStr = reader.getAttributeValue(null, "location");
        String sizeStr = reader.getAttributeValue(null, "size");
        int state = Integer.parseInt(reader.getAttributeValue(null, "state"));
        String maximizedDockable = reader.getAttributeValue(null, "max-dockable");

        Point location = new Point(Integer.parseInt(locStr.substring(0, locStr.indexOf(","))), Integer.parseInt(locStr.substring(locStr.indexOf(",") + 1)));
        Dimension size = new Dimension(Integer.parseInt(sizeStr.substring(0, sizeStr.indexOf(","))), Integer.parseInt(sizeStr.substring(sizeStr.indexOf(",") + 1)));

        java.util.List<ToolbarDockable> westToolbar = readToolbarFromFile(reader, "westToolbar");
        java.util.List<ToolbarDockable> eastToolbar = readToolbarFromFile(reader, "eastToolbar");
        java.util.List<ToolbarDockable> southToolbar = readToolbarFromFile(reader, "southToolbar");

        WindowLayout layout = new WindowLayout(isMainFrame, location, size, state, readNodeFromFile(reader, "layout"));

        layout.setWestAutoHideToolbarIDs(westToolbar.stream().map(toolbarDockable -> toolbarDockable.id).collect(Collectors.toList()));
        layout.setEastAutoHideToolbarIDs(eastToolbar.stream().map(toolbarDockable -> toolbarDockable.id).collect(Collectors.toList()));
        layout.setSouthAutoHideToolbarIDs(southToolbar.stream().map(toolbarDockable -> toolbarDockable.id).collect(Collectors.toList()));

        for (ToolbarDockable dockable : westToolbar) {
            layout.setSlidePosition(dockable.id, dockable.slidePosition);
        }

        for (ToolbarDockable dockable : eastToolbar) {
            layout.setSlidePosition(dockable.id, dockable.slidePosition);
        }

        for (ToolbarDockable dockable : southToolbar) {
            layout.setSlidePosition(dockable.id, dockable.slidePosition);
        }

        layout.setMaximizedDockable(maximizedDockable);

        return layout;
    }

    private java.util.List<ToolbarDockable> readToolbarFromFile(XMLStreamReader reader, String name) throws XMLStreamException {
        List<ToolbarDockable> dockables = new ArrayList<>();

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals(TAG_DOCKABLE)) {
                    ToolbarDockable dockable = new ToolbarDockable();
                    dockable.id = reader.getAttributeValue(null, "id");
                    String slidePosition = reader.getAttributeValue(null, TAG_SLIDE_POSITION);

                    if (slidePosition != null) {
                        dockable.slidePosition = Double.parseDouble(slidePosition);
                    }
                    dockables.add(dockable);
                }
            }
            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(name)) {
                break;
            }
        }
        return dockables;
    }

    private DockingLayoutNode readNodeFromFile(XMLStreamReader reader, String name) throws XMLStreamException {
        DockingLayoutNode node = null;
        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("simple")) {
                    node = readSimpleNodeFromFile(reader);
                }
                else if (reader.getLocalName().equals(TAG_SPLIT)) {
                    node = readSplitNodeFromFile(reader);
                }
                else if (reader.getLocalName().equals("tabbed")) {
                    node = readTabNodeFromFile(reader);
                }
                else if (reader.getLocalName().equals(TAG_ANCHOR)) {
                    node = readAnchorNodeFromFile(reader);
                }
            }
            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(name)) {
                break;
            }
        }
        return node;
    }

    private DockingSimplePanelNode readSimpleNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
        String persistentID = reader.getAttributeValue(null, TAG_PERSISTENT_ID);
        String className = reader.getAttributeValue(null, TAG_CLASS_NAME);
        String anchor = reader.getAttributeValue(null, TAG_ANCHOR);
        String titleText = reader.getAttributeValue(null, TAG_TITLE_TEXT);
        String tabText = reader.getAttributeValue(null, TAG_TAB_TEXT);

        // class name didn't always exist, set it to an empty string if it's null
        if (className == null) {
            className = "";
        }
        // anchor didn't always exist, set it to an empty string if it's null
        if (anchor == null) {
            anchor = "";
        }
        if (titleText == null) {
            titleText = "";
        }
        if (tabText == null) {
            tabText = "";
        }

        return new DockingSimplePanelNode(docking, persistentID, className, anchor, titleText, tabText, readProperties(reader));
    }

    // expects that we haven't already read the starting element for <properties>
    private Map<String, Property> readProperties(XMLStreamReader reader) throws XMLStreamException {
        Map<String, Property> properties = new HashMap<>();

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals(TAG_PROPERTIES)) {
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

                            if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(TAG_PROPERTY)) {
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
                                        case TAG_VALUE:
                                            value = reader.getAttributeValue(i);
                                            break;
                                    }
                                }
                                if (property != null && type != null && value != null) {
                                    Property parsedProperty = DockableProperties.parseProperty(property, type, value);
                                    properties.put(parsedProperty.getName(), parsedProperty);
                                }
                            }
                            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_PROPERTIES)) {
                                break;
                            }
                        }
                    }
                }
            }
            if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_PROPERTIES)) {
                break;
            }
        }
        return properties;
    }

    private DockingSplitPanelNode readSplitNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
        int orientation = Integer.parseInt(reader.getAttributeValue(null, "orientation"));
        String anchor = reader.getAttributeValue(null, TAG_ANCHOR);
        String dividerPositionsStr = reader.getAttributeValue(null, "divider-positions");

        if (anchor == null) {
            anchor = "";
        }

        if (dividerPositionsStr != null) {
            return readSplitNodeNewFormat(reader, orientation, anchor, dividerPositionsStr);
        }
        return readSplitNodeLegacyFormat(reader, orientation, anchor);
    }

    private DockingSplitPanelNode readSplitNodeNewFormat(XMLStreamReader reader, int orientation,
                                                          String anchor, String dividerPositionsStr)
            throws XMLStreamException {
        String[] posTokens = dividerPositionsStr.split(",");
        double[] positions = new double[posTokens.length];

        for (int i = 0; i < posTokens.length; i++) {
            positions[i] = Math.max(0.0, Math.min(1.0, Double.parseDouble(posTokens[i].trim())));
        }

        List<DockingLayoutNode> children = new ArrayList<>();

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                String name = reader.getLocalName();

                if (name.equals("simple")) {
                    children.add(readSimpleNodeFromFile(reader));
                }
                else if (name.equals(TAG_SPLIT)) {
                    children.add(readSplitNodeFromFile(reader));
                }
                else if (name.equals("tabbed")) {
                    children.add(readTabNodeFromFile(reader));
                }
                else if (name.equals(TAG_ANCHOR)) {
                    children.add(readAnchorNodeFromFile(reader));
                }
            }
            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_SPLIT)) {
                break;
            }
        }
        return new DockingSplitPanelNode(docking, children, orientation, positions, anchor);
    }

    private DockingSplitPanelNode readSplitNodeLegacyFormat(XMLStreamReader reader, int orientation,
                                                             String anchor) throws XMLStreamException {
        String divPropStr = reader.getAttributeValue(null, "divider-proportion");
        double dividerProportion = (divPropStr != null) ? Double.parseDouble(divPropStr) : 0.5;
        dividerProportion = Math.max(0.0, Math.min(1.0, dividerProportion));

        DockingLayoutNode left = null;
        DockingLayoutNode right = null;

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals(TAG_LEFT)) {
                    left = readNodeFromFile(reader, TAG_LEFT);
                }
                else if (reader.getLocalName().equals(TAG_RIGHT)) {
                    right = readNodeFromFile(reader, TAG_RIGHT);
                }
            }
            else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_SPLIT)) {
                break;
            }
        }
        return new DockingSplitPanelNode(docking, left, right, orientation, dividerProportion, anchor);
    }

    private DockingTabPanelNode readTabNodeFromFile(XMLStreamReader reader) throws XMLStreamException {
        DockingTabPanelNode node = null;

        String currentPersistentID = "";
        String anchor = "";

        while (reader.hasNext()) {
            int next = reader.nextTag();

            if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("selectedTab")) {
                String persistentID = reader.getAttributeValue(null, TAG_PERSISTENT_ID);
                String className = reader.getAttributeValue(null, TAG_CLASS_NAME);
                anchor = reader.getAttributeValue(null, TAG_ANCHOR);

                String titleText = reader.getAttributeValue(null, TAG_TITLE_TEXT);
                String tabText = reader.getAttributeValue(null, TAG_TAB_TEXT);

                // class name didn't always exist, set it to an empty string if it's null
                if (className == null) {
                    className = "";
                }
                if (anchor == null) {
                    anchor = "";
                }
                if (titleText == null) {
                    titleText = "";
                }
                if (tabText == null) {
                    tabText = "";
                }
                node = new DockingTabPanelNode(docking, persistentID, className, anchor, titleText, tabText);
            }
            else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("tab")) {
                currentPersistentID = reader.getAttributeValue(null, TAG_PERSISTENT_ID);
                String className = reader.getAttributeValue(null, TAG_CLASS_NAME);
                anchor = reader.getAttributeValue(null, TAG_ANCHOR);

                String titleText = reader.getAttributeValue(null, TAG_TITLE_TEXT);
                String tabText = reader.getAttributeValue(null, TAG_TAB_TEXT);

                // class name didn't always exist, set it to an empty string if it's null
                if (className == null) {
                    className = "";
                }
                // anchor didn't always exist, set it to an empty string if it's null
                if (anchor == null) {
                    anchor = "";
                }
                if (titleText == null) {
                    titleText = "";
                }
                if (tabText == null) {
                    tabText = "";
                }
                if (node != null) {
                    node.addTab(currentPersistentID, className, anchor, titleText, tabText);
                }
            }
            else if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(TAG_PROPERTIES)) {
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

                        if (next == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(TAG_PROPERTY)) {
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
                                    case TAG_VALUE:
                                        value = reader.getAttributeValue(i);
                                        break;
                                }
                            }
                            if (property != null && type != null && value != null) {
                                Property parsedProperty = DockableProperties.parseProperty(property, type, value);
                                properties.put(parsedProperty.getName(), parsedProperty);
                            }
                        }
                        else if (next == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(TAG_PROPERTIES)) {
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

    private DockingAnchorPanelNode readAnchorNodeFromFile(XMLStreamReader reader) {
        String persistentID = reader.getAttributeValue(null, TAG_PERSISTENT_ID);
        String className = reader.getAttributeValue(null, TAG_CLASS_NAME);

        return new DockingAnchorPanelNode(docking, persistentID, className);
    }
}
