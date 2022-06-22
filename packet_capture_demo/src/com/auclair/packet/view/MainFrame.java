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
