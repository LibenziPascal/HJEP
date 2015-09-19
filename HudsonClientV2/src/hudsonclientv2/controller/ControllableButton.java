package hudsonclientv2.controller;

import hudsonclientv2.utils.logging.HudsonPluginLogger;

import java.awt.Button;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ControllableButton extends Button {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public ControllableButton(final String title, final IButtonController buttonController) {
	super(title);
	this.addMouseListener(new MouseListener() {

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
	    }

	    @Override
	    public void mouseClicked(MouseEvent e) {
		try {
		    buttonController.doControl();
		} catch (IOException | ParserConfigurationException | SAXException e1) {
		    HudsonPluginLogger.logException(e1);
		}
	    }
	});
    }

}
