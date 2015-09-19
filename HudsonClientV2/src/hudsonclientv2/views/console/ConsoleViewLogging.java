package hudsonclientv2.views.console;

import java.io.IOException;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleViewLogging {
	private ConsoleViewLogging() {
	}

	public static void logAndSetActive(String console) throws IOException {
		MessageConsole consoleView = log(console);
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		conMan.showConsoleView(consoleView);
	}

	public static MessageConsole log(String console) throws IOException {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		MessageConsole myConsole = null;
		for (int i = 0; i < existing.length; i++)
			if ("Last build Log".equals(existing[i].getName()))
				myConsole = (MessageConsole) existing[i];
		// no console found, so create a new one
		if (myConsole == null) {
			myConsole = new MessageConsole("Last build Log", null);
		}
		System.out.println(console);

		conMan.addConsoles(new IConsole[] { myConsole });
		myConsole.newMessageStream().println(console);
		return myConsole;
	}
}
