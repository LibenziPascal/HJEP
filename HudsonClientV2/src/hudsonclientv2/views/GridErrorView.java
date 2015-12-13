package hudsonclientv2.views;

import hudsonclientv2.configuration.jobs.view.JavaEditorOpener;
import hudsonclientv2.handlers.ShellActivationTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class GridErrorView extends ViewPart {

	private static ListViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(getViewSite());
	}

	class ViewContentProvider implements IStructuredContentProvider {

		private List<String> list = new ArrayList<String>();

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer paramViewer, Object paramObject1, Object paramObject2) {

		}

		@Override
		public Object[] getElements(Object paramObject) {
			return list.toArray();
		}

		public void add(String object) {
			list.add(object);
		}

		public void setMessages(Collection<String> lastFails) {
			list.addAll(lastFails);
		}

		public void clear() {
			this.list.clear();
		}

	}

	public static void addMessage(String message) {
		((ViewContentProvider) viewer.getContentProvider()).add(message);
		viewer.getList().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh();

			}
		});
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public static void setMessages(Collection<String> lastFails) {
		((ViewContentProvider) viewer.getContentProvider()).clear();
		((ViewContentProvider) viewer.getContentProvider()).setMessages(lastFails);
		viewer.getList().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh();

			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				String methodQualifiedName = ((IStructuredSelection) arg0.getSelection()).getFirstElement().toString();
				String[] splittedClassFUllNameAndMethod = methodQualifiedName.split("\\.");
				if (splittedClassFUllNameAndMethod.length > 1) {
					new JavaEditorOpener().openEditor(ShellActivationTracker.getActiveShell(),
					        splittedClassFUllNameAndMethod[splittedClassFUllNameAndMethod.length - 2],
					        splittedClassFUllNameAndMethod[splittedClassFUllNameAndMethod.length - 1]);
				}
			}
		});
	}

}
