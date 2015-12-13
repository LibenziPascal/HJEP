package hudsonclientv2.configuration.repo.view;

import hudsonclientv2.bo.Job;
import hudsonclientv2.bo.JobBuild;
import hudsonclientv2.configuration.jobs.view.JobView;
import hudsonclientv2.controller.HudsonRemoteDescriptionController;
import hudsonclientv2.holders.JobHolder;
import hudsonclientv2.holders.MapHolder;
import hudsonclientv2.holders.SimpleUser;
import hudsonclientv2.utils.logging.HudsonPluginLogger;
import hudsonclientv2.utils.popups.SWTPopupUtils;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

//TODO refactoring
public class CreateEditRepositoryPopup {

	private CreateEditRepositoryPopup() {
	}

	public static void popup(TableViewer viewer, String repoURL, String username, String pwd) {
		Shell rootPanel = SWTPopupUtils.createAndCenterPopup(viewer.getTable().getDisplay(), 800, 800);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 50;
		layout.marginTop = 50;
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 4;
		layout.verticalSpacing = 15;
		layout.horizontalSpacing = 30;
		rootPanel.setLayout(layout);

		GridData gridData4Cells = cellsGridData(4);

		GridData gridData3Cells = cellsGridData(3);

		Text descLabel = new Text(rootPanel, SWT.READ_ONLY | SWT.NONE);
		descLabel.setText("This window allow you to configure a remote Jenkins repository.\n");
		descLabel.setLayoutData(gridData4Cells);

		Text repoText = new Text(rootPanel, SWT.READ_ONLY | SWT.NONE);
		repoText.setText("Repository URL:");
		Text urlRepositoryTF = new Text(rootPanel, SWT.BORDER);
		if (repoURL != null && !repoURL.isEmpty()) {
			urlRepositoryTF.setText(repoURL);
		} else {
			urlRepositoryTF.setText("http://jenkins");
		}
		urlRepositoryTF.setEditable(true);
		urlRepositoryTF.setLayoutData(gridData3Cells);

		Text userNameLabel = new Text(rootPanel, SWT.READ_ONLY | SWT.NONE);
		userNameLabel.setText("Username:");
		userNameLabel.setLayoutData(defaultGridData());
		Text usernameTF = new Text(rootPanel, SWT.BORDER);
		if (username != null && !username.isEmpty()) {
			usernameTF.setText(username);
		} else {
			usernameTF.setText("libenzi");
		}
		usernameTF.setEditable(true);
		usernameTF.setLayoutData(defaultGridData());

		Text pwdLabel = new Text(rootPanel, SWT.READ_ONLY | SWT.NONE);
		pwdLabel.setText("Password:");
		pwdLabel.setLayoutData(defaultGridData());

		Text pwdTF = new Text(rootPanel, SWT.BORDER);
		if (pwd != null && !pwd.isEmpty()) {
			pwdTF.setText(pwd);
		} else {
			pwdTF.setText("it's4myGeeks");
		}
		pwdTF.setEditable(true);
		pwdTF.setEchoChar('*');
		pwdTF.setLayoutData(defaultGridData());

		new Text(rootPanel, SWT.READ_ONLY | SWT.NONE).setText("Filtre:");
		final List filterObjectTypeList = new List(rootPanel, SWT.BORDER);
		filterObjectTypeList.add("*");
		filterObjectTypeList.add("Jobs");
		filterObjectTypeList.add("Views");

		new Text(rootPanel, SWT.READ_ONLY | SWT.NONE).setText("");
		new Text(rootPanel, SWT.READ_ONLY | SWT.NONE).setText("");

		List resultList = new List(rootPanel, SWT.V_SCROLL);
		resultList.setLayoutData(cellsGridData(4, 30));
		// rootPanel.pack();
		final HudsonRemoteDescriptionController hudsonRemoteDescriptionController = new HudsonRemoteDescriptionController(urlRepositoryTF,
		        filterObjectTypeList, usernameTF, pwdTF, resultList);

		Button listButton = new Button(rootPanel, SWT.PUSH);
		listButton.setText("List");
		listButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent paramMouseEvent) {
			}

			@Override
			public void mouseDown(MouseEvent paramMouseEvent) {
				try {
					hudsonRemoteDescriptionController.doControl();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void mouseUp(MouseEvent paramMouseEvent) {

			}
		});

		Button closeButton = new Button(rootPanel, SWT.PUSH);
		closeButton.setText("Close");
		closeButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent paramMouseEvent) {
			}

			@Override
			public void mouseDown(MouseEvent paramMouseEvent) {
				rootPanel.dispose();
				refresh(viewer);
				saveHolders();
			}

			@Override
			public void mouseDoubleClick(MouseEvent paramMouseEvent) {
			}
		});

		final Button seeDetailsJobButton = new Button(rootPanel, SWT.PUSH);
		seeDetailsJobButton.setText("See details");
		seeDetailsJobButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent paramMouseEvent) {
			}

			@Override
			public void mouseDown(MouseEvent paramMouseEvent) {
				try {
					hudsonRemoteDescriptionController.seeCurrentDetails(rootPanel);
				} catch (IOException | ParserConfigurationException | SAXException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent paramMouseEvent) {
			}
		});

		final Button addJobButton = new Button(rootPanel, SWT.PUSH);
		addJobButton.setText("Add job");
		addJobButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent paramMouseEvent) {
			}

			@Override
			public void mouseDown(MouseEvent paramMouseEvent) {
				Job hudsonJob = new Job(urlRepositoryTF.getText(), filterObjectTypeList.getSelection()[0], "", new SimpleUser(usernameTF.getText(), pwdTF
				        .getText()));
				hudsonJob.setBuilds(new ArrayList<JobBuild>());
				try {
					hudsonRemoteDescriptionController.addCurrentSelectedJobs();
					refresh(viewer);
				} catch (SAXException | IOException | ParserConfigurationException e) {
					HudsonPluginLogger.logException(e);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent paramMouseEvent) {
			}
		});

		if (!username.isEmpty()) {
			rootPanel.setText("View/Edit a repository configuration");
			try {
				filterObjectTypeList.select(0);
				rootPanel.open();
				new HudsonRemoteDescriptionController(urlRepositoryTF, filterObjectTypeList, usernameTF, pwdTF, resultList).doControl();
			} catch (IOException e) {
				HudsonPluginLogger.logException(e);
			}
		} else {
			rootPanel.setText("Create a repository configuration");
			rootPanel.open();
		}
		while (!rootPanel.isDisposed()) {
			if (!rootPanel.getDisplay().readAndDispatch()) {
				rootPanel.getDisplay().sleep();
			}
		}
	}

	private static void saveHolders() {
		JobHolder.save();
		MapHolder.save();
	}

	private static GridData defaultGridData() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		return gridData;
	}

	private static GridData cellsGridData(int hSpan) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = hSpan;
		return gridData;
	}

	private static GridData cellsGridData(int hSpan, int vSpan) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = hSpan;
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = vSpan;
		return gridData;
	}

	private static void refresh(TableViewer viewer) {
		viewer.getTable().getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			}
		});
		JobView.refreshFromExt();
	}

	public static void popup(TableViewer viewer, String repoURL) {
		Job selectedJob = JobHolder.getRepo(repoURL);
		popup(viewer, selectedJob.getUrlRepo(), selectedJob.getUser().getUsername(), selectedJob.getUser().getPassword());
	}

}
