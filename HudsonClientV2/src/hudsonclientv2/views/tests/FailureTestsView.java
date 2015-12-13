package hudsonclientv2.views.tests;

import hudsonclientv2.configuration.jobs.view.JavaEditorOpener;
import hudsonclientv2.utils.popups.SWTPopupUtils;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FailureTestsView {

	private Shell shell;

	public FailureTestsView(final Display d, Set<String> testsList) {
		shell = SWTPopupUtils.createAndCenterPopup(d, 400, 400);
		shell.setLayout(new GridLayout());

		final MouseListener mouseListener = new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				if (arg0.getSource() instanceof Text) {
					String methodQualifiedName = ((Text) arg0.getSource()).getText();
					String[] splittedClassFUllNameAndMethod = methodQualifiedName.split("\\.");
					if (splittedClassFUllNameAndMethod.length > 1) {
						// TODO increase comportement of opener with full class
						// name?
						new JavaEditorOpener().openEditor(shell, splittedClassFUllNameAndMethod[splittedClassFUllNameAndMethod.length - 2],
						        splittedClassFUllNameAndMethod[splittedClassFUllNameAndMethod.length - 1]);
					} else {
						((Text) arg0.getSource()).setToolTipText("No linkage possible");
					}
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		};

		for (String methodQualifiedName : testsList) {
			Text l = new Text(shell, SWT.NONE);
			l.setForeground(new Color(shell.getDisplay(), 0, 0, 255));
			l.setCursor(new Cursor(d, SWT.CURSOR_HAND));
			l.setText(methodQualifiedName);
			l.setEditable(false);
			l.addMouseListener(mouseListener);
		}

		Button openAll = new Button(shell, SWT.BUTTON1);
		openAll.setText("Open All Failures");
		openAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					for (String methodQualifiedName : testsList) {
						String[] splittedClassFUllNameAndMethod = methodQualifiedName.split("\\.");
						if (splittedClassFUllNameAndMethod.length > 1) {
							new JavaEditorOpener().openEditor(shell, splittedClassFUllNameAndMethod[splittedClassFUllNameAndMethod.length - 2],
							        splittedClassFUllNameAndMethod[splittedClassFUllNameAndMethod.length - 1]);
						}
					}
					break;
				}
			}
		});
	}

	public void show() {
		shell.open();

		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}
	}
}
