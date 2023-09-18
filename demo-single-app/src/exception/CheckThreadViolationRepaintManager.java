/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2012-2015 the original author or authors.
 */
package exception;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.Objects;

import static javax.swing.SwingUtilities.isEventDispatchThread;

/**
 * <p>
 * This class is used to detect Event Dispatch Thread rule violations<br>
 * See <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How to Use Threads</a> for more info
 * </p>
 *
 * <p>
 * This is a modification of original idea of Scott Delap.<br>
 * </p>
 *
 * @author Scott Delap
 * @author Alexander Potochkin
 * <p>
 * <a href="https://swinghelper.dev.java.net/">...</a>
 */
abstract class CheckThreadViolationRepaintManager extends RepaintManager {
	private final boolean completeCheck;

	private WeakReference<JComponent> lastComponent;

	CheckThreadViolationRepaintManager() {
		// it is recommended to pass the complete check
		this(true);
	}

	CheckThreadViolationRepaintManager(boolean completeCheck) {
		this.completeCheck = completeCheck;
	}

	@Override
	public synchronized void addInvalidComponent(JComponent component) {
		checkThreadViolations(Objects.requireNonNull(component));
		super.addInvalidComponent(component);
	}

	@Override
	public void addDirtyRegion(JComponent component, int x, int y, int w, int h) {
		checkThreadViolations(Objects.requireNonNull(component));
		super.addDirtyRegion(component, x, y, w, h);
	}

	private void checkThreadViolations(JComponent c) {
		if (!isEventDispatchThread() && (completeCheck || c.isShowing())) {
			boolean imageUpdate = false;
			boolean repaint = false;
			boolean fromSwing = false;
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			for (StackTraceElement st : stackTrace) {
				if (repaint && st.getClassName().startsWith("javax.swing.")) {
					fromSwing = true;
				}
				if (repaint && "imageUpdate".equals(st.getMethodName())) {
					imageUpdate = true;
				}
				if ("repaint".equals(st.getMethodName())) {
					repaint = true;
					fromSwing = false;
				}
			}
			if (imageUpdate) {
				// assuming it is java.awt.image.ImageObserver.imageUpdate(...)
				// image was asynchronously updated, that's ok
				return;
			}
			if (repaint && !fromSwing) {
				// no problems here, since repaint() is thread safe
				return;
			}
			// ignore the last processed component
			if (lastComponent != null && c == lastComponent.get()) {
				return;
			}
			lastComponent = new WeakReference<>(c);
			violationFound(c, stackTrace);
		}
	}

	abstract void violationFound(JComponent c, StackTraceElement[] stackTrace);
}