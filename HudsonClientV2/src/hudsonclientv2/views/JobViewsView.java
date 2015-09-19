package hudsonclientv2.views;

import hudsonclientv2.Activator;
import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.bo.Job;
import hudsonclientv2.communication.rest.RestConnectionUtils;
import hudsonclientv2.communication.rest.reader.ListingXMLReader;
import hudsonclientv2.controller.HudsonRemoteBuilderController;
import hudsonclientv2.controller.HudsonRemoteDescriptionController;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapCookie;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.holders.SimpleUser;
import hudsonclientv2.utils.logging.HudsonPluginLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.xml.sax.SAXException;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class JobViewsView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "hudsonclientv2.views.JobViewsView";

    private TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action action1;
    private Action action2;
    private Action doubleClickAction;

    private TreeParent invisibleRoot;
    private Map<String, String> allColors = new HashMap<String, String>();
    private Set<String> listOfTreatedRepos = new HashSet<String>();

    /*
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */

    class TreeObject implements IAdaptable {
	private String name;
	private TreeParent parent;

	public TreeObject(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	public void setParent(TreeParent parent) {
	    this.parent = parent;
	}

	public TreeParent getParent() {
	    return parent;
	}

	public String toString() {
	    return getName();
	}

	public Object getAdapter(Class key) {
	    return null;
	}
    }

    class TreeParent extends TreeObject {
	private ArrayList children;

	public TreeParent(String name) {
	    super(name);
	    children = new ArrayList();
	}

	public void addChild(TreeObject child) {
	    children.add(child);
	    child.setParent(this);
	}

	public void removeChild(TreeObject child) {
	    children.remove(child);
	    child.setParent(null);
	}

	public TreeObject[] getChildren() {
	    return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
	}

	public boolean hasChildren() {
	    return children.size() > 0;
	}
    }

    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {

	    if (parent.equals(getViewSite())) {
		if (invisibleRoot == null)
		    initialize();
		return getChildren(invisibleRoot);
	    }
	    return getChildren(parent);
	}

	public Object getParent(Object child) {
	    if (child instanceof TreeObject) {
		return ((TreeObject) child).getParent();
	    }
	    return null;
	}

	public Object[] getChildren(Object parent) {
	    if (parent instanceof TreeParent) {
		return ((TreeParent) parent).getChildren();
	    }
	    return new Object[0];
	}

	public boolean hasChildren(Object parent) {
	    if (parent instanceof TreeParent)
		return ((TreeParent) parent).hasChildren();
	    return false;
	}

	/*
	 * We will set up a dummy model to initialize tree heararchy. In a real
	 * code, you will connect to a real model and expose its hierarchy.
	 */
	private void initialize() {
	    // Get repositories
	    MapHolder.init();
	    JobHolder.init();
	    Set<String> allRepos = MapHolder.getUrls();
	    // TODO prevent pb with iterator
	    final Map<String, String> allColors = new HashMap<String, String>();
	    try {
		allColors.putAll(HudsonRemoteBuilderController.getAllColors(JobHolder.getJobNames().iterator().next()));
	    } catch (SAXException | IOException | ParserConfigurationException e1) {
		HudsonPluginLogger.logException(e1);
	    }
	    invisibleRoot = new TreeParent("");
	    for (String urlRepo : allRepos) {
		try {
		    // We add some roots
		    final TreeParent serverElement = new TreeParent(urlRepo);

		    // Retrieve views from the server
		    final SimpleUser user = MapHolder.getEntry(urlRepo);
		    final List<String> cookies = MapCookie.getEntryNG(urlRepo, user.getUsername(), user.getPassword());
		    // Retrieve views from the url
		    Map<String, String> views = new ListingXMLReader().getNodeJobOrView("View",
			    RestConnectionUtils.makeConnectionAndRead(cookies, new URL(urlRepo.concat(HudsonPluginConstants.SUFFIX_API_XML))));
		    for (Entry<String, String> view : views.entrySet()) {
			HudsonPluginLogger.logInfo(Activator.PLUGIN_ID, "Found view: " + view.getKey() + " " + view.getValue());
			TreeParent viewElement = new TreeParent(view.getKey());

			viewElement.addChild(new TreeParent(""));

			serverElement.addChild(viewElement);
		    }

		    invisibleRoot.addChild(serverElement);
		} catch (ParserConfigurationException | SAXException | IOException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }
	    // }
	}
    }

    class ViewLabelProvider extends LabelProvider {

	public String getText(Object obj) {
	    return obj.toString();
	}

	public Image getImage(Object obj) {
	    // String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
	    if (obj instanceof TreeParent) {
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	    } else {
		String colorName = allColors.get(getText(obj));
		/*
	         * Image cloudImage = new Image(viewer.getTable().getDisplay(),
	         * this.getClass().getClassLoader()
	         * .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER +
	         * imageName));
	         */
		if (colorName == null) {
		    String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		    return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
		Image colorImage = null;
		boolean building = colorName.contains("anime");
		if (building) {
		    colorImage = new Image(viewer.getTree().getDisplay(), this.getClass().getClassLoader()
			    .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + colorName + ".gif"));

		} else {
		    colorImage = new Image(viewer.getTree().getDisplay(), this.getClass().getClassLoader()
			    .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + colorName + ".png"));
		}
		// if (building){
		// colorImage = getIconCompact(
		// colorImage,
		// new Image(viewer.getTable().getDisplay(),
		// this.getClass().getClassLoader()
		// .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER +
		// "releng_gears.gif")), 128);
		// }
		return colorImage;
	    }
	}
    }

    class NameSorter extends ViewerSorter {
    }

    /**
     * The constructor.
     */
    public JobViewsView() {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent) {
	viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	drillDownAdapter = new DrillDownAdapter(viewer);
	viewer.setContentProvider(new ViewContentProvider());
	viewer.setLabelProvider(new ViewLabelProvider());
	viewer.setSorter(new NameSorter());
	viewer.setInput(getViewSite());

	// Create the help context id for the viewer's control
	PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "HudsonClientV2.viewer");
	makeActions();
	hookContextMenu();
	hookDoubleClickAction();
	contributeToActionBars();
    }

    private void hookContextMenu() {
	MenuManager menuMgr = new MenuManager("#PopupMenu");
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(new IMenuListener() {
	    public void menuAboutToShow(IMenuManager manager) {
		JobViewsView.this.fillContextMenu(manager);
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
	manager.add(action1);
	manager.add(new Separator());
	manager.add(action2);
    }

    private void fillContextMenu(IMenuManager manager) {
	manager.add(action1);
	manager.add(action2);
	manager.add(new Separator());
	drillDownAdapter.addNavigationActions(manager);
	// Other plug-ins can contribute there actions here
	manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
	manager.add(action1);
	manager.add(action2);
	manager.add(new Separator());
	drillDownAdapter.addNavigationActions(manager);
    }

    private void makeActions() {
	action1 = new Action() {
	    public void run() {
		showMessage("Action 1 executed");
	    }
	};
	action1.setText("Action 1");
	action1.setToolTipText("Action 1 tooltip");
	action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

	action2 = new Action() {
	    public void run() {
		showMessage("Action 2 executed");
	    }
	};
	action2.setText("Action 2");
	action2.setToolTipText("Action 2 tooltip");
	action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	doubleClickAction = new Action() {
	    public void run() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		showMessage("Double-click detected on " + obj.toString());
	    }
	};
    }

    private void hookDoubleClickAction() {
	viewer.addDoubleClickListener(new IDoubleClickListener() {
	    public void doubleClick(DoubleClickEvent event) {
		doubleClickAction.run();
	    }
	});
	viewer.addTreeListener(new ITreeViewerListener() {

	    @Override
	    public void treeExpanded(TreeExpansionEvent arg0) {
		// TODO load colors and clouds -> only when nested views will be
		// available.
		// For nested views. First we look for views. If there's views,
		// look for it and create ParentElement.
		// If there's no views -> look for jobs and create leafs
		listOfTreatedRepos.clear();
		System.out.println(arg0.getElement());
		try {
		    // Here we can do for the lazy view
		    // TODO nested Views ?
		    // Select the root
		    Object elem = arg0.getElement();
		    while (elem instanceof TreeParent && !((TreeParent) elem).getParent().equals(invisibleRoot)) {
			elem = ((TreeParent) elem).getParent();
		    }
		    final SimpleUser user = MapHolder.getEntry(elem.toString());
		    final List<String> cookies = MapCookie.getEntryNG(elem.toString(), user.getUsername(), user.getPassword());
		    // T=ODO list views if no view go to job else create
		    // ParentFolder

		    final Set<String> collectedJobsUrls = new HashSet<String>();
		    // This must be done only if we're llokin for jobs
		    HudsonRemoteDescriptionController.scanViews(collectedJobsUrls, cookies,
			    elem.toString().concat("/view/").concat(arg0.getElement().toString()));
		    if (arg0.getElement() instanceof TreeParent) {
			TreeParent parentView = (TreeParent) arg0.getElement();
			while (parentView.hasChildren()) {
			    parentView.removeChild(parentView.getChildren()[0]);
			}
			for (final String collectedJobUrl : collectedJobsUrls) {
			    final String[] splittedUrl = collectedJobUrl.split("/");
			    String jobName = splittedUrl[splittedUrl.length - 1];
			    //Get the first level
			    if(!listOfTreatedRepos.contains(elem.toString())) {
				    allColors.putAll(HudsonRemoteBuilderController.getAllColorsForRepo(elem.toString()));
				    listOfTreatedRepos.add(elem.toString());
			    }
			    //Remove the nested view (like a hack)
			    if(allColors.containsKey(jobName)) {
				parentView.addChild(new TreeObject(jobName));
			    }
			}

		    }
		    viewer.getTree().getDisplay().asyncExec(new Runnable() {

			public void run() {
			    viewer.refresh();
			}
		    });
		} catch (ParserConfigurationException | SAXException | IOException e) {
		    HudsonPluginLogger.logException(e);
		}
	    }

	    @Override
	    public void treeCollapsed(TreeExpansionEvent arg0) {

	    }
	});
    }

    private void showMessage(String message) {
	MessageDialog.openInformation(viewer.getControl().getShell(), "Hudson Repository View", message);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
	viewer.getControl().setFocus();
    }
}