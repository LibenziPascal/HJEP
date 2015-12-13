package hudsonclientv2.configuration.jobs.view;

import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.controller.HudsonRemoteBuilderController;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.utils.logging.HudsonPluginLogger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

public class JobView extends AbstractLaunchableJobsView {
	public static final String ID = "hudsonclientv2.configuration.jobs.view.JobView";

	private static TableViewer viewer;

	private IAction deleteAction, refreshAction;

	private static boolean mapColorsInit = false;

	public JobView() {
	}

	private void launchTimer() {
		new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				mapColorsInit = false;
				refresh();
				mapColorsInit = false;
			}
		}, 180L, 180L, TimeUnit.SECONDS);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		// manager.add(listJobsAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		Image image = new Image(viewer.getTable().getDisplay(), this.getClass().getClassLoader()
		        .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + "refresh.png"));
		refreshAction.setImageDescriptor(ImageDescriptor.createFromImage(image));
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
			return JobHolder.getJobsList();
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Map<String, String> colorJobs;

		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			// on récupère une seule fois le content et on update toutes les
			// images avec une map dans la jenkins/api/xml
			// couleur et building seront ok.
			// pour le clover, à faire dans une autre vue?
			try {
				if (!mapColorsInit) {
					colorJobs = HudsonRemoteBuilderController.getAllColors(getText(obj));
					mapColorsInit = true;
				}
				// No more cloud image here. TODO: view details
				// String[] iconsProperties =
				// HudsonRemoteBuilderController.getIconsProperties(getText(obj));
				// String imageName = iconsProperties[1];
				// String colorName = iconsProperties[0];
				String colorName = colorJobs.get(getText(obj));
				/*
				 * Image cloudImage = new Image(viewer.getTable().getDisplay(),
				 * this.getClass().getClassLoader()
				 * .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER +
				 * imageName));
				 */
				Image colorImage = null;
				boolean building = colorName.contains("anime");
				if (building) {
					colorImage = new Image(viewer.getTable().getDisplay(), this.getClass().getClassLoader()
					        .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + colorName + ".gif"));

				} else {
					colorImage = new Image(viewer.getTable().getDisplay(), this.getClass().getClassLoader()
					        .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + colorName + ".png"));
				}
				if (building) {
					colorImage = getIconCompact(
					        colorImage,
					        new Image(viewer.getTable().getDisplay(), this.getClass().getClassLoader()
					                .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + "releng_gears.gif")), 128);
				}
				return colorImage;
			} catch (ParserConfigurationException | SAXException | IOException e) {
				HudsonPluginLogger.logException("An error occured during refresh", e);
			}

			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	protected void makeActions() {
		super.makeActions();

		refreshAction = new Action() {
			@Override
			public void run() {
				mapColorsInit = false;
				refresh();
				mapColorsInit = false;
			}
		};
		deleteAction = new Action() {
			public void run() {
				JobHolder.removeEntry(viewer.getTable().getSelection());
				viewer.getTable().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						viewer.refresh();
					}
				});
			}
		};
		deleteAction.setText("Delete");
	}

	@Override
	protected void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
	}

	@Override
	protected ColumnViewer getViewer() {
		return viewer;
	}

	public static void refreshFromExt() {
		viewer.getTable().getDisplay().asyncExec(new Runnable() {

			public void run() {
				mapColorsInit = false;
				viewer.refresh();
				mapColorsInit = false;
			}
		});
	}

	@Override
	protected void refresh() {
		refreshFromExt();
	}

	@Override
	protected Display getDisplay() {
		return viewer.getTable().getDisplay();
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(getDoubleClickAction());
		MenuManager menuMgr = new MenuManager("Console");
		menuMgr.add(getLogConsoleAction());
		menuMgr.add(getInTxtEditorConsoleAction());
		menuMgr.add(getBrowserConsoleAction());
		manager.add(menuMgr);
		MenuManager menuManagerTest = new MenuManager("Tests");
		menuManagerTest.add(getTrendAction());
		menuManagerTest.add(getSeeTestInErrorsAction());
		manager.add(menuManagerTest);
		// see sonar results
		// see trace of test?
		// Other plug-ins can contribute there actions here
		manager.add(deleteAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void postConstruct() {
		contributeToActionBars();
		launchTimer();
	}

}