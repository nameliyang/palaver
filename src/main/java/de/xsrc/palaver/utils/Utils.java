package de.xsrc.palaver.utils;

import de.xsrc.palaver.beans.Account;
import de.xsrc.palaver.beans.Contact;
import de.xsrc.palaver.beans.Palaver;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.FlowHandler;
import org.datafx.controller.flow.container.DefaultFlowContainer;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.jivesoftware.smack.DirectoryRosterStore;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Utils {

	// private static final CrudService<EntityWithId.T>, CrudService.T> storage;

	private static final Logger logger = Logger
					.getLogger(Storage.class.getName());

	private static ConcurrentHashMap<Palaver, MultiUserChat> joinedMucs = new ConcurrentHashMap<>();

	public static Stage getDialog(Flow f, ViewFlowContext flowContext)
					throws FlowException {
		ResourceBundle b = ResourceBundle.getBundle("i18n.Palaver_en");
		DefaultFlowContainer container = new DefaultFlowContainer();
		FlowHandler flowHandler;
		if (flowContext != null) {
			flowHandler = f.createHandler(flowContext);
		} else {
			flowHandler = f.createHandler();
		}
		flowHandler.getViewConfiguration().setResources(b);
		flowHandler.start(container);
		Scene scene = new Scene(container.getView());
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.initModality(Modality.WINDOW_MODAL);
		return stage;
	}

	/**
	 * Loads the file containing models of the class c
	 *
	 * @param c - Class name
	 * @return file object
	 * @throws IOException
	 */
	public static File getFile(Class<?> c) throws IOException {
		return getFile(workingDirectory(), c);
	}

	public static File getFile(String workingDir, Class<?> c) throws IOException {
		File file = new File(workingDir + "/" + c.getSimpleName() + "s"); // Make

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}

	/**
	 * Tries to find User Home and create the config dir
	 *
	 * @return
	 */
	private static String workingDirectory() {
		String os = System.getProperty("os.name").toUpperCase();
		String workingDirectory;
		if (os.contains("WIN")) {
			// it is simply the location of the "AppData" folder =>
			// StackOverflow
			workingDirectory = System.getenv("AppData");
		} else if (os.contains("DARWIN") || os.contains("MAC")) {
			workingDirectory = System.getProperty("user.home")
							+ "/Library/Application Support/Palaver";
		} else {
			// It's a UNIX system! I know this!
			workingDirectory = System.getProperty("user.home") + "/.palaver";
		}
		return workingDirectory;
	}

	public static boolean isMuc(XMPPConnection connection, String jid) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
		DiscoverInfo info = discoManager.discoverInfo(StringUtils.parseServer(jid));
		return info.containsFeature("http://jabber.org/protocol/muc");
	}

	public static MultiUserChat getMuc(Palaver palaver) {
		return getJoinedMucs().get(palaver);
	}

	public static DirectoryRosterStore getRosterStore(Account account) {
		File dir = new File(Utils.workingDirectory() + "/roster/" + account.getJid());
		if (!dir.exists()) {
			dir.mkdirs();
			return DirectoryRosterStore.init(dir);
		}
		return DirectoryRosterStore.open(dir);
	}

	public static Contact createContact(String account, String jid, String name, Boolean conference) {
		Contact contact = new Contact();
		contact.setAccount(account);
		contact.setJid(jid);
		if (name != null && name.length() > 0) {
			contact.setName(name);
		} else {
			contact.setName(StringUtils.parseName(contact.getJid()));
		}
		if (conference) {
			contact.setConference(true);
		}
		return contact;
	}

	public static ConcurrentHashMap<Palaver, MultiUserChat> getJoinedMucs() {
		return joinedMucs;
	}

	public static void setJoinedMucs(ConcurrentHashMap<Palaver, MultiUserChat> joinedMucs) {
		Utils.joinedMucs = joinedMucs;
	}
}
