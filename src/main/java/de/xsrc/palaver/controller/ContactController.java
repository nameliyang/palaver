package de.xsrc.palaver.controller;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.xsrc.palaver.model.Palaver;
import de.xsrc.palaver.provider.ContactProvider;
import de.xsrc.palaver.provider.PalaverProvider;
import de.xsrc.palaver.utils.ColdStorage;
import de.xsrc.palaver.utils.Utils;
import de.xsrc.palaver.xmpp.model.Contact;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.datafx.controller.FXMLController;
import org.datafx.controller.FxmlLoadException;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.action.ActionMethod;
import org.datafx.controller.flow.action.ActionTrigger;
import org.datafx.controller.flow.action.BackAction;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

import java.util.logging.Logger;

@FXMLController("/fxml/ContactView.fxml")
public class ContactController {

	private static final Logger logger = Logger.getLogger(ContactController.class
					.getName());
	@FXML
	@BackAction
	private Button back;

	@FXML
	@ActionTrigger("addContactButton")
	private Button addContactButton;


	@FXMLViewFlowContext
	private ViewFlowContext context;

	@FXML
	@ActionTrigger("startPalaverButton")
	private Button startPalaverButton;

	@FXML
	private TextField searchInput;
	@FXML
	@ActionTrigger("contactListView")
	private ListView<Contact> contactListView;
	private ContactProvider provider;

	@FXML
	private void initialize() {
		AwesomeDude.setIcon(back, AwesomeIcon.CHEVRON_LEFT, "24");

		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER);

		hbox.getChildren().add(AwesomeDude.createIconLabel(AwesomeIcon.PLUS, "24"));
		hbox.getChildren().add(AwesomeDude.createIconLabel(AwesomeIcon.USER, "24"));
		addContactButton.setGraphic(hbox);

		provider = ApplicationContext.getInstance()
						.getRegisteredObject(ContactProvider.class);
		contactListView.setItems(provider.getData());
		contactListView.setManaged(true);
		contactListView.setCellFactory(listView -> new BuddyCell());

		searchInput.textProperty().addListener((observable, oldVal, newVal) -> handleSearchByKey(oldVal, newVal));

		AwesomeDude.setIcon(startPalaverButton, AwesomeIcon.SEARCH, "20");
		Platform.runLater(searchInput::requestFocus);
	}

	public void handleSearchByKey(String oldVal, String newVal) {
		// If the number of characters in the text box is less than last time
		// it must be because the user pressed delete
		if (oldVal != null && (newVal.length() < oldVal.length())) {
			// Restore the lists original set of entries
			// and start from the beginning
			contactListView.setItems(provider.getData());
		}

		// Change to upper case so that case is not an issue
		newVal = newVal.toUpperCase();

		// Filter out the entries that don't contain the entered text
		ObservableList<Contact> sublist = FXCollections.observableArrayList();
		for (Contact entry : contactListView.getItems()) {
			if (entry.toString().toUpperCase().contains(newVal)) {
				sublist.add(entry);
			}
		}
		contactListView.setItems(sublist);
		contactListView.getSelectionModel().select(0);

	}

	@FXML
	@ActionMethod("startPalaverAction")
	public void startPalaverAction() throws VetoException, FlowException, XMPPException.XMPPErrorException, SmackException {
		Contact buddy = contactListView.getSelectionModel().getSelectedItems().get(0);
		if (buddy != null) {
			logger.fine("Starting palaver with " + buddy.getJid());
			PalaverProvider.openPalaver(buddy);
			}
	}

	@ActionMethod("addContactAction")
	public void addContactAction() throws FxmlLoadException {
		Flow f = new Flow(AddContactController.class);
		try {
			Utils.getDialog(f, null).showAndWait();
		} catch (FlowException e) {
			e.printStackTrace();
		}
	}

}
