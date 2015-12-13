package hudsonclientv2.utils.popups;

import hudsonclientv2.communication.rest.reader.ListingXMLReader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.xml.sax.SAXException;

public class DetailsJobPopup {

	private DetailsJobPopup() {

	}

	public static void showPopupForJobDetails(final Shell parentShell, final String content, final boolean isView) throws ParserConfigurationException,
	        SAXException, IOException {
		Shell shell = SWTPopupUtils.createAndCenterPopup(parentShell.getDisplay(), 800, 600);

		RowLayout layout = new RowLayout();
		layout.marginLeft = 50;
		layout.marginTop = 50;
		shell.setLayout(layout);

		Table table = new Table(shell, SWT.PUSH);
		table.setSize(750, 550);
		TableColumn colonne1 = new TableColumn(table, SWT.LEFT);
		colonne1.setWidth(100);
		TableColumn colonne2 = new TableColumn(table, SWT.LEFT);
		colonne2.setWidth(400);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableItem ligne1 = new TableItem(table, SWT.NONE);
		if (isView) {
			ligne1.setText(new String[] { "Name: ", ListingXMLReader.lookForNodeValueInFlatXml("name", content) });
		} else {
			ligne1.setText(new String[] { "Name: ", ListingXMLReader.lookForNodeValueInFlatXml("displayName", content) });
		}
		TableItem ligne2 = new TableItem(table, SWT.NONE);
		ligne2.setText(new String[] { "Desc: ", ListingXMLReader.lookForNodeValueInFlatXml("description", content) });
		TableItem ligne3 = new TableItem(table, SWT.NONE);
		ligne3.setText(new String[] { "URL: ", ListingXMLReader.lookForNodeValueInFlatXml("url", content) });

		shell.open();

		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}
	}

}
