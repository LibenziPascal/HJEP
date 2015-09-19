package hudsonclientv2.configuration.jobs.view;

import hudsonclientv2.bo.Job;
import hudsonclientv2.bo.ResultsTests;
import hudsonclientv2.controller.HudsonRemoteBuildControllerNG;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.utils.logging.HudsonPluginLogger;
import hudsonclientv2.utils.popups.SWTPopupUtils;
import hudsonclientv2.views.console.ConsoleViewLogging;
import hudsonclientv2.views.console.StringInput;
import hudsonclientv2.views.console.StringStorage;
import hudsonclientv2.views.tests.FailureTestsView;
import hudsonclientv2.views.tree.TreeObject;
import hudsonclientv2.views.trends.handlers.TrendPaintListener;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.part.ViewPart;
import org.xml.sax.SAXException;

public abstract class AbstractLaunchableJobsView extends ViewPart {

    private IAction doubleClickAction;

    private IAction logConsoleAction, inTxtEditorConsoleAction, browserConsoleAction;

    private IAction trendAction, seeTestInErrorsAction;

    private TrendPaintListener trendPaintListener;

    private Shell trendPopup;

    @Override
    public void createPartControl(Composite parent) {
	initHolders();

	createViewer(parent);

	// Create the help context id for the viewer's control
	PlatformUI.getWorkbench().getHelpSystem().setHelp(getViewer().getControl(), "HudsonClient.viewerJob");
	makeActions();
	hookContextMenu();
	hookDblClickAct();

	// Sample: contribute action toolbar and launch a timer for periodic
	// refresh
	postConstruct();

	trendPaintListener = new TrendPaintListener();
    }

    @Override
    public void setFocus() {
	getViewer().getControl().setFocus();
    }

    protected void makeActions() {
	trendAction = new Action() {
	    @Override
	    public void run() {
		ISelection selection = getViewer().getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    final List<ResultsTests> newResults = new ArrayList<ResultsTests>();
		    if (obj instanceof Job) {
			newResults.addAll(HudsonRemoteBuildControllerNG.getTrend((Job) obj));
		    } else if (obj instanceof TreeObject && ((TreeObject) obj).getJob() != null) {
			newResults.addAll(HudsonRemoteBuildControllerNG.getTrend(((TreeObject) obj).getJob()));
		    }
		    if (newResults == null || newResults.isEmpty()) {
			return;
		    }
		    trendPaintListener.setResultsToDraw(newResults);
		    if (trendPopup == null || trendPopup.isDisposed()) {
			trendPopup = SWTPopupUtils.createAndCenterPopup("Test trends:" + obj.toString(), getDisplay(), 800, 600);
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
			if (item instanceof Job) {
				t.addAll(HudsonRemoteBuildControllerNG.getLastFails((Job) item));
			    } else if (item instanceof TreeObject && ((TreeObject) item).getJob() != null) {
				t.addAll(HudsonRemoteBuildControllerNG.getLastFails(((TreeObject) item).getJob()));
			    }
		    } catch (SAXException | IOException | ParserConfigurationException e) {
			HudsonPluginLogger.logException(e);
		    }
		}
		new FailureTestsView(getDisplay(), t).show();
	    }

	    private Object[] getObjectsFromSelection() {
		ISelection selection = getViewer().getSelection();
		Object[] obj = ((IStructuredSelection) selection).toArray();
		return obj;
	    }
	};
	seeTestInErrorsAction.setText("See tests in error");

	doubleClickAction = new Action() {
	    public void run() {
		ISelection selection = getViewer().getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    if (obj instanceof Job) {
			HudsonRemoteBuildControllerNG.build((Job) obj);
		    } else if (obj instanceof TreeObject && ((TreeObject) obj).getJob() != null) {
			HudsonRemoteBuildControllerNG.build(((TreeObject) obj).getJob());
		    }
		    refresh();
		} catch (ParserConfigurationException | SAXException | IOException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }
	};
	doubleClickAction.setText("Build");
	logConsoleAction = new Action() {
	    public void run() {
		ISelection selection = getViewer().getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    String consoleContents = "";
		    if (obj instanceof Job) {
			consoleContents = HudsonRemoteBuildControllerNG.console((Job) obj);
		    } else if (obj instanceof TreeObject && ((TreeObject) obj).getJob() != null) {
			consoleContents = HudsonRemoteBuildControllerNG.console(((TreeObject) obj).getJob());
		    }
		    ConsoleViewLogging.logAndSetActive(consoleContents);
		} catch (IOException | ParserConfigurationException | SAXException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }

	};
	logConsoleAction.setText("See Last build Console Log");

	inTxtEditorConsoleAction = new Action() {
	    public void run() {
		ISelection selection = getViewer().getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    String consoleContents = "";
		    if (obj instanceof Job) {
			consoleContents = HudsonRemoteBuildControllerNG.console((Job) obj);
		    } else if (obj instanceof TreeObject && ((TreeObject) obj).getJob() != null) {
			consoleContents = HudsonRemoteBuildControllerNG.console(((TreeObject) obj).getJob());
		    } 
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
		ISelection selection = getViewer().getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		try {
		    IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(obj.toString());
		    String consoleUrl = "";
		    if (obj instanceof Job) {
			consoleUrl = HudsonRemoteBuildControllerNG.getLastBuilURL((Job) obj);
		    } else if (obj instanceof TreeObject && ((TreeObject) obj).getJob() != null) {
			consoleUrl = HudsonRemoteBuildControllerNG.getLastBuilURL(((TreeObject) obj).getJob());
		    } 
		    browser.openURL(new URL(consoleUrl));
		} catch (IOException | ParserConfigurationException | SAXException | PartInitException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }

	};
	browserConsoleAction.setText("See Last build Console Log in Browser");
    }

    private void hookContextMenu() {
	MenuManager menuMgr = new MenuManager("Menu -- " + hashCode());
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(new IMenuListener() {
	    public void menuAboutToShow(IMenuManager manager) {
		fillContextMenu(manager);
	    }
	});
	Menu menu = menuMgr.createContextMenu(getViewer().getControl());
	getViewer().getControl().setMenu(menu);
	getSite().registerContextMenu(menuMgr, getViewer());
    }

    protected abstract void refresh();

    private void hookDblClickAct() {
	getViewer().addDoubleClickListener(new IDoubleClickListener() {
	    public void doubleClick(DoubleClickEvent event) {
		doubleClickAction.run();
	    }
	});
    }

    private void initHolders() {
	JobHolder.init();
	MapHolder.init();
    }

    protected IAction getDoubleClickAction() {
	return doubleClickAction;
    }

    protected IAction getLogConsoleAction() {
	return logConsoleAction;
    }

    protected IAction getInTxtEditorConsoleAction() {
	return inTxtEditorConsoleAction;
    }

    protected IAction getBrowserConsoleAction() {
	return browserConsoleAction;
    }

    public IAction getTrendAction() {
	return trendAction;
    }

    public IAction getSeeTestInErrorsAction() {
	return seeTestInErrorsAction;
    }

    public TrendPaintListener getTrendPaintListener() {
	return trendPaintListener;
    }

    public Shell getTrendPopup() {
	return trendPopup;
    }

    public Image getIconCompact(Image im0, Image im1, int alpha) {
	// ImageDescriptor desc0 = ImageDescriptor.createFromImage(im0);
	ImageDescriptor desc1 = ImageDescriptor.createFromImage(im1);
	if (alpha != -1) {
	    desc1.getImageData().alpha = alpha;
	}
	DecorationOverlayIcon resultIcon = new DecorationOverlayIcon(im0, new ImageDescriptor[] { desc1 });// ,
	                                                                                                   // new
	                                                                                                   // Point(32,
	                                                                                                   // 32));
	Image icon = resultIcon.createImage();
	return icon;
    }

    protected abstract void createViewer(Composite parent);

    protected abstract ColumnViewer getViewer();

    protected abstract Display getDisplay();

    protected abstract void fillContextMenu(IMenuManager mgr);

    protected abstract void postConstruct();

}
