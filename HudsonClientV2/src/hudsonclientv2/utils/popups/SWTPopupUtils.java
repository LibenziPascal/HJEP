package hudsonclientv2.utils.popups;

import java.awt.Dimension;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTPopupUtils {

	private SWTPopupUtils() {

	}

	public static Shell createAndCenterPopup(int width, int height) {
		return createAndCenterPopup(new Dimension(width, height));
	}

	public static Shell createAndCenterPopup(Dimension d) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize((int) d.getWidth(), (int) d.getHeight());

		centerWindow(shell);
		return shell;
	}

	private static void centerWindow(Shell shell) {
		// AW feel a good --> If more than one monitor then we success center in
		// first screen.
		// TODO better way: get the active screen shoud be a bonus
		int divisor = 2;
		if (shell.getDisplay() != null && shell.getDisplay().getMonitors() != null) {
			divisor *= shell.getDisplay().getMonitors().length;
		}
		Rectangle bds = shell.getDisplay().getBounds();

		Point p = shell.getSize();

		int nLeft = (bds.width - p.x) / divisor;
		int nTop = (bds.height - p.y) / divisor;

		shell.setBounds(nLeft, nTop, p.x, p.y);
	}

	public static Shell createAndCenterPopup(String title, Display display, int i, int j) {
		Shell shell = new Shell(display);
		shell.setSize(i, j);
		shell.setText(title);

		centerWindow(shell);
		return shell;
	}

	public static Shell createAndCenterPopup(Display display, int i, int j) {
		return createAndCenterPopup("", display, i, j);
	}

	public static void showMessage(Shell sh, String message) {
		MessageDialog.openInformation(sh, "Information", message);
	}

}
