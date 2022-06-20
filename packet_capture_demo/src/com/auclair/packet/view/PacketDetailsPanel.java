package com.auclair.packet.view;

import docking.Dockable;
import docking.Docking;
import docking.DockingRegion;
import docking.ui.FlatLafDragHeader;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class PacketDetailsPanel extends JPanel implements Dockable {
	private final FlatLafDragHeader header = new FlatLafDragHeader(this, "Packet Details");

	JXTreeTable treeTable;

	public PacketDetailsPanel() {
		super(new GridBagLayout());

		Docking.registerDockable(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;

		add(header, gbc);
		gbc.gridy++;

		treeTable = new JXTreeTable();

		gbc.weighty = 1.0;
		add(treeTable, gbc);
		gbc.gridy++;
	}

	@Override
	public JComponent dragSource() {
		return header;
	}

	@Override
	public String persistentID() {
		return "packet-details";
	}

	@Override
	public String tabText() {
		return "Details";
	}

	@Override
	public boolean floatingAllowed() {
		return true;
	}

	@Override
	public boolean limitToRoot() {
		return false;
	}

	@Override
	public List<DockingRegion> disallowedRegions() {
		return Collections.emptyList();
	}

	@Override
	public boolean allowClose() {
		return true;
	}

	@Override
	public boolean allowMinMax() {
		return false;
	}

	@Override
	public boolean hasMoreOptions() {
		return false;
	}
}
