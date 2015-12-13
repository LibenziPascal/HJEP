package hudsonclientv2.handlers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ShellActivationTracker implements Listener {
	private static Shell activeShell;

	public ShellActivationTracker(Display display) {
		activeShell = display.getActiveShell();
		display.addFilter(SWT.Activate, this);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget instanceof Shell) {
			activeShell = (Shell) event.widget;
		}
	}

	public static Shell getActiveShell() {
		return activeShell;
	}
}