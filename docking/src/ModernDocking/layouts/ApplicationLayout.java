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
package ModernDocking.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// layout of the entire application including all frames with docking roots
public class ApplicationLayout {
	private static class FrameLayout {
		private final WindowLayout layout;
		private final boolean isMainFrame;

		private FrameLayout(WindowLayout layout, boolean isMainFrame) {
			this.layout = layout;
			this.isMainFrame = isMainFrame;
		}
	}

	private final List<FrameLayout> layouts = new ArrayList<>();

	public void setMainFrame(WindowLayout layout) {
		for (FrameLayout frameLayout : layouts) {
			if (frameLayout.isMainFrame) {
				layouts.remove(frameLayout);
				break;
			}
		}
		layouts.add(new FrameLayout(layout, true));
	}

	public void addFrame(WindowLayout layout) {
		layouts.add(new FrameLayout(layout, layout.isMainFrame()));
	}

	public WindowLayout getMainFrameLayout() {
		for (FrameLayout frameLayout : layouts) {
			if (frameLayout.isMainFrame) {
				return frameLayout.layout;
			}
		}
		return null;
	}

	public List<WindowLayout> getFloatingFrameLayouts() {
		return layouts.stream()
				.filter(layout -> !layout.isMainFrame)
				.map(layout -> layout.layout)
				.collect(Collectors.toList());
	}
}
