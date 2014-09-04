package de.xsrc.palaver.xmpp;

import de.xsrc.palaver.beans.Account;
import de.xsrc.palaver.xmpp.task.ConnectTask;
import de.xsrc.palaver.xmpp.task.DisconnectTask;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.datafx.concurrent.ObservableExecutor;
import org.datafx.controller.context.ApplicationContext;
import org.jivesoftware.smack.XMPPConnection;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ConnectionManager {
	private static final Logger logger = Logger.getLogger(ConnectionManager.class
					.getName());
	private static ConcurrentHashMap<Account, XMPPConnection> conMap = new ConcurrentHashMap<>();
	private static ConnectionManager instance;

	private ConnectionManager(ObservableList<Account> accounts) {
		accounts.addListener((ListChangeListener.Change<? extends Account> c) -> {
			ObservableExecutor executor = ApplicationContext.getInstance().getRegisteredObject(ObservableExecutor.class);
			while (c.next()) {
				if (c.getAddedSize() > 0) {
					List<? extends Account> list = c.getAddedSubList();
					for (Account account : list) {
						executor.submit(getConnectTask(account));
						account.credentialsChangedProperty().addChangeListener(evt -> {
							executor.submit(new DisconnectTask(conMap.get(account)));
							executor.submit(getConnectTask(account));
						});
					}
				} else if (c.wasRemoved()) {
					for (Account account : c.getRemoved()) {
						executor.submit(new DisconnectTask(conMap.get(account)));
					}

				}
			}

		});

	}

	public static void start(ObservableList<Account> accounts) {
		if (instance == null) {
			instance = new ConnectionManager(accounts);
			return;
		}
		throw new IllegalStateException("Connection Manager is already started");
	}

	public static ConnectionManager getInstance() {
		return instance;
	}

	public static XMPPConnection getConnection(Account account) {
		return conMap.get(account);
	}

	public static ConcurrentHashMap<Account, XMPPConnection> getConMap() {
		return conMap;
	}

	private ConnectTask getConnectTask(Account account) {
		ConnectTask connectTask = new ConnectTask(account);
		connectTask.setOnSucceeded(event -> {
			XMPPConnection connection = (XMPPConnection) event.getSource().getValue();
			conMap.put(account, connection);
		});
		return connectTask;
	}
}
