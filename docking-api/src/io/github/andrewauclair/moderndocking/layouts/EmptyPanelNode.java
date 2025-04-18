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
package io.github.andrewauclair.moderndocking.layouts;

import io.github.andrewauclair.moderndocking.DockingRegion;

/**
 * Used to avoid issues with null nodes when root layout nodes are empty
 */
public class EmptyPanelNode implements DockingLayoutNode {
	/**
	 * Create new instance
	 */
	public EmptyPanelNode() {
	}

	@Override
	public DockingLayoutNode findNode(String persistentID) {
		return null;
	}

	@Override
	public void dock(String persistentID, DockingRegion region, double dividerProportion) {
	}

	@Override
	public void replaceChild(DockingLayoutNode child, DockingLayoutNode newChild) {
	}

	@Override
	public DockingLayoutNode getParent() {
		return null;
	}

	@Override
	public void setParent(DockingLayoutNode parent) {
	}
}
