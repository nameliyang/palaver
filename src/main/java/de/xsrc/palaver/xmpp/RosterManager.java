package de.xsrc.palaver.xmpp;

import de.xsrc.palaver.Connection;
import de.xsrc.palaver.beans.Credentials;
import de.xsrc.palaver.models.ContactManager;
import de.xsrc.palaver.xmpp.listeners.PalaverRosterListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.rosterstore.DirectoryRosterStore;
import org.jxmpp.util.XmppStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class manages multiple {@link Roster}s for {@link Connection}s and keeps them in sync with {@link
 * ContactManager}
 * Created by Bahtiar `kalkin-` Gadimov on 25.07.15.
 */
public class RosterManager {

    private static final Logger logger = Logger.getLogger(RosterManager.class.getName());

    protected final ObservableMap<String, Roster> rosterMap = FXCollections.observableMap(new
            ConcurrentHashMap<>());
    private final ContactManager contactManager;
    private final String path;

    public RosterManager(ContactManager contactManager, String path) {
        this.contactManager = contactManager;
        this.path = path;
    }

    /**
     * Initializes or opens a {@link DirectoryRosterStore} for storing roster entries in a set of files as specified by
     * RFC 6121 and setups the given {@link Roster} to use it.
     *
     * @param path path to directory where data is/should be stored
     */
    protected void setupRosterStore(String path, Roster roster) throws IOException {
        final File dir = new File(path);
        final DirectoryRosterStore rosterStore;
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                rosterStore = DirectoryRosterStore.init(dir);
            } else {
                throw new IOException("Could not create roster directory");
            }
        } else {
            rosterStore = DirectoryRosterStore.open(dir);
        }
        roster.setRosterStore(rosterStore);
    }

    /**
     * Creates a new roster entry and presence subscription.
     *
     * @param credentials
     * @param jid
     * @throws SmackException.NotLoggedInException
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException.NotConnectedException
     * @throws SmackException.NoResponseException
     */
    public void subscribe(Credentials credentials, String jid) throws SmackException.NotLoggedInException,
            XMPPException.XMPPErrorException, SmackException.NotConnectedException, SmackException.NoResponseException {
        logger.finer("Adding " + jid + " to " + credentials.getJid() + "account");
        final Roster roster = rosterMap.get(credentials.getJid());
        roster.createEntry(jid, XmppStringUtils.parseLocalpart(jid), null);
    }

    /**
     * Removes a roster entry from the roster.
     *
     * @param credentials
     * @param jid
     * @throws SmackException.NotLoggedInException
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException.NotConnectedException
     * @throws SmackException.NoResponseException
     */
    public void unsubscribe(Credentials credentials, String jid) throws SmackException.NotLoggedInException,
            XMPPException.XMPPErrorException, SmackException.NotConnectedException, SmackException.NoResponseException {
        logger.finer("Removing " + jid + " from " + credentials.getJid() + "account");
        final Roster roster = rosterMap.get(credentials.getJid());
        final RosterEntry entry = roster.getEntry(jid);
        roster.removeEntry(entry);
    }

    /**
     * This helper method synchronizes the content of a {@link Roster} for the given {@link Connection} with the
     * {@ContactManager}. It setups a {@link DirectoryRosterStore}, adds {@link RosterEntriesImporter} and
     * {@link PalaverRosterListener} to the {@link Roster}
     *
     * @param connection {@link Connection} for which to manage the {@link Roster}
     */
    public void registerConnection(Connection connection) {
        final XMPPConnection xmppConnection = connection.xmpptcpConnection;
        final Credentials credentials = connection.getCredentials();
        final Roster roster = Roster.getInstanceFor(xmppConnection);
        final String jid = credentials.getJid();
        final String rosterPath = this.path + credentials.getJid();
        try {
            setupRosterStore(rosterPath, roster);
        } catch (IOException e) {
            throw new RuntimeException("Can't create DirectoryRoster store in path " + rosterPath, e);
        }
        final RosterEntriesImporter rosterEntriesImporter = new RosterEntriesImporter(jid, contactManager);
        final PalaverRosterListener palaverRosterListener = new PalaverRosterListener(jid, contactManager, roster);
        rosterMap.put(credentials.getJid(), roster);
        roster.getEntriesAndAddListener(palaverRosterListener, rosterEntriesImporter);
    }
}
