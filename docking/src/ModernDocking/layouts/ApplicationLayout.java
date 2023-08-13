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

/**
 * Layout of the entire application, including all open frames with docking roots
 */
public class ApplicationLayout {
	private static class FrameLayout {
		private final WindowLayout layout;
		private final boolean isMainFrame;

		private FrameLayout(WindowLayout layout, boolean isMainFrame) {
			this.layout = layout;
			this.isMainFrame = isMainFrame;
		}
	}

	/**
	 * List of all the FrameLayouts in this ApplicationLayout
	 */
	private final List<FrameLayout> layouts = new ArrayList<>();

	/**
	 * Create an empty ApplicationLayout
	 */
	public ApplicationLayout() {
	}

	/**
	 * Create an ApplicationLayout from a WindowLayout
	 * @param mainFrame Layout of the application main frame
	 */
	public ApplicationLayout(WindowLayout mainFrame) {
		layouts.add(new FrameLayout(mainFrame, true));
	}

	/**
	 * Add a WindowLayout as the main frame
	 * <p>
	 * Note: This will remove the current main frame layout in this ApplicationLayout, if one exists
	 *
	 * @param layout Layout of the application main frame
	 */
	public void setMainFrame(WindowLayout layout) {
		for (FrameLayout frameLayout : layouts) {
			if (frameLayout.isMainFrame) {
				layouts.remove(frameLayout);
				break;
			}
		}
		layouts.add(new FrameLayout(layout, true));
	}

	/**
	 * Add a new WindowLayout
	 *
	 * @param layout Layout to add to this ApplicationLayout
	 */
	public void addFrame(WindowLayout layout) {
		layouts.add(new FrameLayout(layout, layout.isMainFrame()));
	}

	/**
	 * Get the layout of the main frame stored in this ApplicationLayout
	 *
	 * @return The layout of the main frame, or null, if there is no main frame layout
	 */
	public WindowLayout getMainFrameLayout() {
		for (FrameLayout frameLayout : layouts) {
			if (frameLayout.isMainFrame) {
				return frameLayout.layout;
			}
		}
		return null;
	}

	/**
	 * Get all the floating frame layouts in this ApplicationLayout
	 *
	 * @return All layouts in this ApplicationLayout that are not the main frame
	 */
	public List<WindowLayout> getFloatingFrameLayouts() {
		return layouts.stream()
				.filter(layout -> !layout.isMainFrame)
				.map(layout -> layout.layout)
				.collect(Collectors.toList());
	}
}
