package hudsonclientv2.configuration.jobs.view;

import hudsonclientv2.Activator;
import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.bo.ResultsTests;
import hudsonclientv2.controller.HudsonRemoteBuilderController;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.utils.logging.HudsonPluginLogger;
import hudsonclientv2.utils.popups.SWTPopupUtils;
import hudsonclientv2.views.console.ConsoleViewLogging;
import hudsonclientv2.views.console.StringInput;
import hudsonclientv2.views.console.StringStorage;
import hudsonclientv2.views.tests.FailureTestsView;
import hudsonclientv2.views.trends.handlers.TrendPaintListener;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.part.ViewPart;
import org.xml.sax.SAXException;

public class JobView extends ViewPart {
    public static final String ID = "hudsonclientv2.configuration.jobs.view.JobView";

    private static TableViewer viewer;

    private IAction doubleClickAction;

    private IAction deleteAction;

    private IAction logConsoleAction, inTxtEditorConsoleAction, browserConsoleAction;

    private IAction refreshAction;

    private IAction trendAction, seeTestInErrorsAction;

    private TrendPaintListener trendPaintListener;

    private Shell trendPopup;

    private static boolean mapColorsInit = false;

    ILog log = Activator.getDefault().getLog();

    public JobView() {
    }

    @Override
    public void createPartControl(Composite parent) {
	initHolders();

	viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	viewer.setContentProvider(new ViewContentProvider());
	viewer.setLabelProvider(new ViewLabelProvider());
	viewer.setSorter(new NameSorter());
	viewer.setInput(getViewSite());

	// Create the help context id for the viewer's control
	PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "HudsonClient.viewerJob");
	makeActions();
	hookContextMenu();
	hookDblClickAct();
	contributeToActionBars();
	launchTimer();

	trendPaintListener = new TrendPaintListener();
    }

    private void launchTimer() {
	new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(new Runnable() {

	    @Override
	    public void run() {
		    refresh();
	    }
	}, 60L, 60L, TimeUnit.SECONDS);
    }

    private void initHolders() {
	JobHolder.init();
	MapHolder.init();
    }

    private void hookContextMenu() {
	MenuManager menuMgr = new MenuManager("MyMenu");
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(new IMenuListener() {
	    public void menuAboutToShow(IMenuManager manager) {
		JobView.this.fillContextMenu(manager);
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
	manager.add(new Separator());
	// manager.add(listJobsAction);
    }

    private void fillContextMenu(IMenuManager manager) {
	manager.add(doubleClickAction);
	MenuManager menuMgr = new MenuManager("Console");
	menuMgr.add(logConsoleAction);
	menuMgr.add(inTxtEditorConsoleAction);
	menuMgr.add(browserConsoleAction);
	manager.add(menuMgr);
	MenuManager menuManagerTest = new MenuManager("Tests");
	menuManagerTest.add(trendAction);
	menuManagerTest.add(seeTestInErrorsAction);
	manager.add(menuManagerTest);
	// see sonar results
	// see trace of test?
	// Other plug-ins can contribute there actions here
	manager.add(deleteAction);
	manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
	manager.add(refreshAction);
	Image image = new Image(viewer.getTable().getDisplay(), this.getClass().getClassLoader()
	        .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + "refresh.png"));
	refreshAction.setImageDescriptor(ImageDescriptor.createFromImage(image));
    }

    public static void refresh() {
	viewer.getTable().getDisplay().asyncExec(new Runnable() {

	    public void run() {
		mapColorsInit=false;
		viewer.refresh();
	    }
	});
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
	    return JobHolder.getJobNames().toArray();
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
		//on récupère une seule fois le content et on update toutes les images avec une map dans la jenkins/api/xml
		// couleur et building seront ok.
		//pour le clover, à faire dans une autre vue?
	    try {
		if(!mapColorsInit) {
        	    	colorJobs = HudsonRemoteBuilderController.getAllColors(getText(obj));
        	    	mapColorsInit = true;
		}
		//No more cloud image here. TODO: view details
//		String[] iconsProperties = HudsonRemoteBuilderController.getIconsProperties(getText(obj));
//		String imageName = iconsProperties[1];
//		String colorName = iconsProperties[0];
		String colorName = colorJobs.get(getText(obj));
		/*Image cloudImage = new Image(viewer.getTable().getDisplay(), this.getClass().getClassLoader()
		        .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + imageName));
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
		if (building){
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

    private void hookDblClickAct() {
	viewer.addDoubleClickListener(new IDoubleClickListener() {
	    public void doubleClick(DoubleClickEvent event) {
		doubleClickAction.run();
	    }
	});
    }

    class NameSorter extends ViewerSorter {
    }

    private void makeActions() {
	trendAction = new Action() {
	    @Override
	    public void run() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    final List<ResultsTests> newResults = HudsonRemoteBuilderController.getTrend(obj.toString());
		    if (newResults == null || newResults.isEmpty()) {
			return;
		    }
		    trendPaintListener.setResultsToDraw(newResults);
		    if (trendPopup == null || trendPopup.isDisposed()) {
			trendPopup = SWTPopupUtils.createAndCenterPopup("Test trends:" + obj.toString(), viewer.getTable().getDisplay(), 800, 600);
			trendPopup.addPaintListener(trendPaintListener);
		    }
		    trendPopup.open();
		    trendPopup.redraw();
		    while (!trendPopup.isDisposed()) {
			if (!trendPopup.getDisplay().readAndDispatch()) {
			    trendPopup.getDisplay().sleep();
			}
		    }
		} catch (IOException | SAXException | ParserConfigurationException e) {
		    HudsonPluginLogger.logException(e);
		}

	    }
	};
	trendAction.setText("Trend");
	seeTestInErrorsAction = new Action() {
	    @Override
	    public void run() {
		Object[] obj = getObjectsFromSelection();
		final Set<String> t = new HashSet<String>();
		for (Object item : obj) {
		    try {
			t.addAll(HudsonRemoteBuilderController.getLastFails(item.toString()));
		    } catch (SAXException | IOException | ParserConfigurationException e) {
			HudsonPluginLogger.logException(e);
		    }
		}
		new FailureTestsView(viewer.getTable().getDisplay(), t).show();
	    }

	    private Object[] getObjectsFromSelection() {
	        ISelection selection = viewer.getSelection();
		Object[] obj = ((IStructuredSelection) selection).toArray();
	        return obj;
            }
	};
	seeTestInErrorsAction.setText("See tests in error");

	refreshAction = new Action() {
	    @Override
	    public void run() {
		refresh();
	    }
	};
	doubleClickAction = new Action() {
	    public void run() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    HudsonRemoteBuilderController.build(obj.toString());
		    refresh();
		} catch (ParserConfigurationException | SAXException | IOException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }
	};
	doubleClickAction.setText("Build");
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
	// TODO create menu action in a dedicated class
	logConsoleAction = new Action() {
	    public void run() {
		ISelection selection = viewer.getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    String consoleContents = HudsonRemoteBuilderController.console(obj.toString());
		    ConsoleViewLogging.logAndSetActive(consoleContents);
		} catch (IOException | ParserConfigurationException | SAXException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }

	};
	logConsoleAction.setText("See Last build Console Log");

	inTxtEditorConsoleAction = new Action() {
	    public void run() {
		ISelection selection = viewer.getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    String consoleContents = HudsonRemoteBuilderController.console(obj.toString());
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		    try {
			IStorage storage = new StringStorage(obj.toString(), consoleContents);
			StringInput input = new StringInput(storage);
			if (page != null)
			    page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
		    } catch (PartInitException e) {
			// Put your exception handler here if you wish to
		    }
		} catch (IOException | ParserConfigurationException | SAXException e) {
		    e.printStackTrace();
		}
	    }

	};
	inTxtEditorConsoleAction.setText("See Last build Console Log in Text Editor");
	browserConsoleAction = new Action() {
	    public void run() {
		ISelection selection = viewer.getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(obj.toString());
		    browser.openURL(new URL(HudsonRemoteBuilderController.getLastBuilURL(obj.toString()).concat("/console")));
		} catch (IOException | ParserConfigurationException | SAXException | PartInitException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }

	};
	browserConsoleAction.setText("See Last build Console Log in Browser");
    }

    @Override
    public void setFocus() {
	viewer.getControl().setFocus();
    }

    public Image getIconCompact(Image im0, Image im1, int alpha) {
//	ImageDescriptor desc0 = ImageDescriptor.createFromImage(im0);
	ImageDescriptor desc1 = ImageDescriptor.createFromImage(im1);
	if (alpha != -1) {
	    desc1.getImageData().alpha = alpha;
	}
	DecorationOverlayIcon resultIcon = new DecorationOverlayIcon(im0, new ImageDescriptor[] {desc1});//, new Point(32, 32));
	Image icon = resultIcon.createImage();
	return icon;
    }

}