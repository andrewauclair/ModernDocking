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
package ModernDocking.internal;

import ModernDocking.persist.AppState;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

// simple internal class that calls AppState.persist() whenever a frame resizes, moves or changes state
public class AppStatePersister extends ComponentAdapter implements WindowStateListener {
	public AppStatePersister() {
	}

	public void addFrame(Window window) {
		window.addComponentListener(this);
		window.addWindowStateListener(this);
	}

	public void removeFrame(Window window) {
		window.removeComponentListener(this);
		window.removeWindowStateListener(this);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		AppState.persist();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		AppState.persist();
	}

	@Override
	public void windowStateChanged(WindowEvent e) {
		AppState.persist();
	}
}
