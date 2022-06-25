package layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// layout of the entire application including all frames with docking roots
public class FullAppLayout {
	private static class FrameLayout {
		private final DockingLayout layout;
		private final boolean isMainFrame;

		private FrameLayout(DockingLayout layout, boolean isMainFrame) {
			this.layout = layout;
			this.isMainFrame = isMainFrame;
		}
	}

	private final List<FrameLayout> layouts = new ArrayList<>();

	public void setMainFrame(DockingLayout layout) {
		for (FrameLayout frameLayout : layouts) {
			if (frameLayout.isMainFrame) {
				layouts.remove(frameLayout);
				break;
			}
		}
		layouts.add(new FrameLayout(layout, true));
	}

	public void addFrame(DockingLayout layout) {
		layouts.add(new FrameLayout(layout, layout.isMainFrame()));
	}

	public DockingLayout getMainFrameLayout() {
		for (FrameLayout frameLayout : layouts) {
			if (frameLayout.isMainFrame) {
				return frameLayout.layout;
			}
		}
		return null;
	}

	public List<DockingLayout> getFloatingFrameLayouts() {
		return layouts.stream()
				.filter(layout -> !layout.isMainFrame)
				.map(layout -> layout.layout)
				.collect(Collectors.toList());
	}
}
