package hudsonclientv2.configuration.repo.view;

import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapCookie;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.holders.SimpleUser;
import hudsonclientv2.utils.logging.HudsonPluginLogger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.part.ViewPart;

public class HudsonRepositoriesView extends ViewPart {
	public static final String ID = "hudsonclientv2.configuration.repo.view.HudsonRepositoriesView";

	private TableViewer viewer;

	private IAction createRepositoryAction;

	private IAction editRepositoryAction;

	private IAction createJobInRepoAction;

	public HudsonRepositoriesView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "HudsonClient.viewer");
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				HudsonRepositoriesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(createRepositoryAction);
		manager.add(new Separator());
		// manager.add(listJobsAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(editRepositoryAction);
		manager.add(createJobInRepoAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(listJobsAction);
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getExistingRepositories();
		}

		private Object[] getExistingRepositories() {
			return MapHolder.getUrls().toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	private void makeActions() {
		createRepositoryAction = new Action() {
			public void run() {
				CreateEditRepositoryPopup.popup(viewer, "", "", "");
			}
		};
		createRepositoryAction.setText("Configure a repository");
		editRepositoryAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				CreateEditRepositoryPopup.popup(viewer, obj.toString());
			}
		};
		editRepositoryAction.setText("Edit a repository");

		createJobInRepoAction = new Action() {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection) selection).getFirstElement();
				try {
					final SimpleUser simple = JobHolder.getRepo(obj.toString()).getUser();
					final List<String> cookies = MapCookie.getEntryNG(obj.toString(), simple.getUsername(), simple.getPassword());
					String cookie = "";
					for (String c : cookies) {
						cookie = cookie.concat(c);
					}
					Browser.setCookie(cookie, obj.toString());
					IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(cookie);
					browser.openURL(new URL(obj.toString().concat("/newJob")));
				} catch (IOException | PartInitException e) {
					HudsonPluginLogger.logException(e);
				}
			}
		};
		createJobInRepoAction.setText("Create a job");
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
