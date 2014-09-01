package de.xsrc.palaver.controller;

import de.xsrc.palaver.model.Entry;
import de.xsrc.palaver.model.Palaver;
import de.xsrc.palaver.xmpp.PalaverManager;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.datafx.controller.FXMLController;
import org.datafx.controller.FxmlLoadException;
import org.datafx.controller.ViewFactory;
import org.datafx.controller.context.ViewContext;
import org.jivesoftware.smack.SmackException.NotConnectedException;

import java.util.List;
import java.util.logging.Logger;

@FXMLController("/fxml/HistoryView.fxml")
public class HistoryController {
	private static final Logger logger = Logger
					.getLogger(HistoryController.class.getName());
	@FXML
	private TextField chatInput;
	@FXML
	private VBox historyBox;
	@FXML
	private ScrollPane scrollPane;

	private Palaver palaver;
	private ObservableList<Entry> history;

	@FXML
	private void initialize() {
		requestFocus();

	}

	public void setPalaver(Palaver p) {
		this.palaver = p;
		history = p.history.entryListProperty();
		add(history);
		history.addListener((Change<? extends Entry> change) -> {
			while (change.next()) {
				logger.finer("New Messages were added to " + p);
				add(change.getAddedSubList());
			}
		});
		historyBox.heightProperty().addListener((observable, oldValue, newValue) -> {
			Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
			palaver.setUnread(false);
		});
	}

	private void add(List<? extends Entry> list) {
		for (Entry entry : list) {
			Label l = new Label();
			l.textProperty().bind(entry.bodyProperty());

			final ViewContext<EntryController> context;
			try {
				context = ViewFactory.getInstance().createByController(
								EntryController.class);
				context.getController().setEntry(entry);
				Platform.runLater(() -> historyBox.getChildren().add(context.getRootNode()));
			} catch (FxmlLoadException e) {
				e.printStackTrace();
				logger.severe("Could not add entry from " + entry.getFrom() + " "
								+ entry);
			}
		}
	}

	@FXML
	private void sendMsgAction() throws NotConnectedException {
		String body = chatInput.getText();
		PalaverManager.sendMsg(this.palaver, body);
		chatInput.clear();
	}

	public void requestFocus() {
		Platform.runLater(() -> {
			chatInput.requestFocus();
			chatInput.positionCaret(chatInput.getLength());
		});

	}
}
