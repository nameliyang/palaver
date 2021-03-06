package de.xsrc.palaver.xmpp.task;

import de.xsrc.palaver.AbstractTest;
import de.xsrc.palaver.Connection;
import de.xsrc.palaver.beans.Credentials;
import de.xsrc.palaver.xmpp.exception.AccountCreationException;
import de.xsrc.palaver.xmpp.exception.AccountDeletionException;
import de.xsrc.palaver.xmpp.exception.ConnectionException;
import javafx.collections.ObservableMap;
import javafx.embed.swing.JFXPanel;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.junit.*;

import static org.junit.Assert.assertTrue;

public class ConnectTaskTest extends AbstractTest {


    private static final Credentials CREDENTIALS = getAccount("alice.open.test@xsrc.de", "password");
    private ObservableMap<String, Connection> connectionMap;
    private Connection connection;

    @BeforeClass
    public static void createAccount() throws AccountCreationException, ConnectionException {
        new JFXPanel(); // Initialize JFX :)
        final CreateAccountTask createAccountTask = new CreateAccountTask(CREDENTIALS, getObservableMap());
        createAccountTask.call().close();

    }

    @AfterClass
    public static void deleteAccount() throws AccountDeletionException, ConnectionException {
        DeleteAccountTask task = new DeleteAccountTask(CREDENTIALS);
        task.call();
    }

    @Before
    public void connect() throws ConnectionException {
        connectionMap = getObservableMap();
        connection = (new ConnectTask(CREDENTIALS, connectionMap)).call();
    }

    @After
    public void disconnect() {
        connection.close();
    }

    @Test
    public void connectionEstablishedAndAuthenticated() {
        assertTrue("Connection to server is established", connection.xmpptcpConnection.isConnected());
        assertTrue("Connection to server is authenticated", connection.xmpptcpConnection.isAuthenticated());
    }

    @Test
    public void connectionIsManaged() {
        Connection connection2 = connectionMap.get(CREDENTIALS.getJid());
        assertTrue("ObservableMap contains appropriate connection", connection == connection2);
    }

    @Test
    public void carbonsEnabled() {
        final CarbonManager carbonManager = CarbonManager.getInstanceFor(connection.xmpptcpConnection);
        assertTrue("Carbons are enabled", carbonManager.getCarbonsEnabled());
    }

    @Test
    @Ignore
    public void streamManagementEnabled() {
        assertTrue("Stream Management should be available", connection.xmpptcpConnection.isSmAvailable());
        assertTrue("Stream Management should be enabled", connection.xmpptcpConnection.isSmEnabled());
    }

    /**
     * Expects ConnectionException to be thrown
     */
    @Test(expected = ConnectionException.class)
    public void testConnectionFailed() throws AccountCreationException, ConnectionException {
        final CreateAccountTask createAccountTask = new CreateAccountTask(getAccount("alice.create.test@example" +
                ".invalid", "password"), getObservableMap());
        createAccountTask.call();
    }
}