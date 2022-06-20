package com.auclair.packet.view;

import docking.Docking;
import docking.DockingRegion;
import docking.RootDockingPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
	private final PacketTablePanel packetTable;
	private final PacketDetailsPanel packetDetails;

	public MainFrame() {

		setSize(new Dimension(800, 600));

		new Docking(this);

		RootDockingPanel root = new RootDockingPanel();
		Docking.registerDockingPanel(root, this);

		add(root, BorderLayout.CENTER);
		
		packetTable = new PacketTablePanel();
		Docking.dock(packetTable, this);
		packetDetails = new PacketDetailsPanel();
		Docking.dock(packetDetails, packetTable, DockingRegion.SOUTH);
	}
}
