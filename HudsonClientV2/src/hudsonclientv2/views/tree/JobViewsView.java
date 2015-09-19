package hudsonclientv2.views.tree;

import hudsonclientv2.Activator;
import hudsonclientv2.bo.HudsonPluginConstants;
import hudsonclientv2.bo.Job;
import hudsonclientv2.communication.rest.RestConnectionUtils;
import hudsonclientv2.communication.rest.reader.ListingXMLReader;
import hudsonclientv2.configuration.jobs.view.AbstractLaunchableJobsView;
import hudsonclientv2.controller.HudsonRemoteBuildControllerNG;
import hudsonclientv2.controller.HudsonRemoteBuilderController;
import hudsonclientv2.controller.HudsonRemoteDescriptionController;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapCookie;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.holders.SimpleUser;
import hudsonclientv2.utils.logging.HudsonPluginLogger;
import hudsonclientv2.views.GridErrorView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

// TODO for actions: automation of repo credentials use if not available for jobs --> by default save entry in holder?
public class JobViewsView extends AbstractLaunchableJobsView {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "hudsonclientv2.views.tree.JobViewsView";

    private TreeViewer viewer;

    private TreeParent invisibleRoot;
    private Map<String, String> allColors = new HashMap<String, String>();
    private Set<String> listOfTreatedRepos = new HashSet<String>();

    private boolean mapColorsInit = false;

    /*
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */

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
	    if (!mapColorsInit) {
		try {
		    allColors.putAll(HudsonRemoteBuilderController.getAllColors(JobHolder.getJobNames().iterator().next()));
		} catch (SAXException | IOException | ParserConfigurationException e) {
		    HudsonPluginLogger.logException(e);
		}
		mapColorsInit = true;
	    }
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
		if (building) {
		    colorImage = getIconCompact(
			    colorImage,
			    new Image(viewer.getTree().getDisplay(), this.getClass().getClassLoader()
			            .getResourceAsStream(HudsonPluginConstants.ICON_FOLDER + "releng_gears.gif")), 128);
		}
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
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
	viewer.getControl().setFocus();
    }

    @Override
    protected void refresh() {
	viewer.getTree().getDisplay().asyncExec(new Runnable() {

	    public void run() {
		mapColorsInit = false;
		viewer.refresh();
		mapColorsInit = false;
	    }
	});
    }

    @Override
    protected void createViewer(Composite parent) {
	viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	viewer.setContentProvider(new ViewContentProvider());
	viewer.setLabelProvider(new ViewLabelProvider());
	viewer.setSorter(new NameSorter());
	viewer.setInput(getViewSite());
    }

    @Override
    protected ColumnViewer getViewer() {
	return viewer;
    }

    @Override
    protected Display getDisplay() {
	return viewer.getTree().getDisplay();
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
	manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    @Override
    protected void postConstruct() {
	viewer.addSelectionChangedListener(new ISelectionChangedListener() {

	    @Override
	    public void selectionChanged(SelectionChangedEvent paramSelectionChangedEvent) {
		TreeObject selectedLeaf = (TreeObject) ((IStructuredSelection) paramSelectionChangedEvent.getSelection()).getFirstElement();
		if (!(selectedLeaf instanceof TreeParent)) {
		    try {
			Object elem = selectedLeaf;
			while (elem!=null && ((TreeObject) elem).getParent() !=null && !((TreeObject) elem).getParent().equals(invisibleRoot)) {
			    elem = ((TreeObject) elem).getParent();
			}
			//TODO if red build -> cause
			if(elem!=null) {
			    //TODO when clic on it, open the file concerned
			    GridErrorView.setMessages(HudsonRemoteBuildControllerNG.getLastFails(selectedLeaf.getJob()));
			}
		    } catch (SAXException | IOException | ParserConfigurationException e) {
			HudsonPluginLogger.logException(e);
		    }
		}
	    }
	});
	viewer.addTreeListener(new ITreeViewerListener() {

	    @Override
	    public void treeExpanded(TreeExpansionEvent arg0) {
		// TODO refresh sur cette vue? peut-être uniquement par action
		// utilisateur
		// TODo mêmes actions sur les builds que dans la jobView ==>
		// Refactoring (Les deux vues doivent donc étendre une abstract)
		// TODO load clouds -> only when nested views will be
		// available.
		// For nested views. First we look for views. If there's views,
		// look for it and create ParentElement.
		// If there's no views -> look for jobs and create leafs
		listOfTreatedRepos.clear();
		System.out.println(arg0.getElement());
		final Stack<String> nestedViewList = new Stack<String>();
		try {
		    // Here we can do for the lazy view
		    // Select the root
		    Object elem = arg0.getElement();
		    while (elem instanceof TreeParent && !((TreeParent) elem).getParent().equals(invisibleRoot)) {
			// If level > 0 and tree parent then it must be a nested
			// view
			if (((TreeParent) elem).getParent().getLevel() > 0) {
			    nestedViewList.add(((TreeParent) elem).getParent().toString());
			}
			elem = ((TreeParent) elem).getParent();
		    }
		    System.out.println("NESTED VIEWS Parent" + nestedViewList);
		    Collections.reverse(nestedViewList);

		    String buildURL = "/view/";
		    for (String view : nestedViewList) {
			buildURL = buildURL.concat(view).concat("/view/");
		    }
		    final SimpleUser user = MapHolder.getEntry(elem.toString());
		    final List<String> cookies = MapCookie.getEntryNG(elem.toString(), user.getUsername(), user.getPassword());
		    // T=ODO list views if no view go to job else create
		    // ParentFolder

		    final Set<String> collectedJobsUrls = new HashSet<String>();
		    // If we're not on leaf then
		    final ArrayList<String> nestedViews = new ArrayList<String>();
		    try {
			HudsonRemoteDescriptionController.findNestedViewsOneLvl(nestedViews, cookies,
			        elem.toString().concat(buildURL).concat(arg0.getElement().toString()));
		    } catch (FileNotFoundException e) {

		    }
		    if (!nestedViews.isEmpty()) {
			final TreeParent parentView = (TreeParent) arg0.getElement();
			while (parentView.hasChildren()) {
			    parentView.removeChild(parentView.getChildren()[0]);
			}
			final Set<String> nestedViewsSet = new HashSet<String>(nestedViews);
			for (String nestedView : nestedViewsSet) {
			    TreeParent nestedViewTreeObject = new TreeParent(nestedView);
			    parentView.addChild(nestedViewTreeObject);
			    nestedViewTreeObject.addChild(new TreeParent(""));
			}
		    } else {
			// This must be done only if we're llokin for jobs
			HudsonRemoteDescriptionController.scanViews(collectedJobsUrls, cookies,
			        elem.toString().concat(buildURL).concat(arg0.getElement().toString()));
			if (arg0.getElement() instanceof TreeParent) {
			    final TreeParent parentView = (TreeParent) arg0.getElement();
			    while (parentView.hasChildren()) {
				parentView.removeChild(parentView.getChildren()[0]);
			    }
			    for (final String collectedJobUrl : collectedJobsUrls) {
				final String[] splittedUrl = collectedJobUrl.split("/");
				final String jobName = splittedUrl[splittedUrl.length - 1];
				// Get the first level
				if (!listOfTreatedRepos.contains(elem.toString())) {
				    allColors.putAll(HudsonRemoteBuilderController.getAllColorsForRepo(elem.toString()));
				    listOfTreatedRepos.add(elem.toString());
				}
				// Remove the nested view (like a hack)
				if (allColors.containsKey(jobName)) {
				    parentView.addChild(new TreeObject(new Job(elem.toString(), jobName, "", null)));
				}
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
}