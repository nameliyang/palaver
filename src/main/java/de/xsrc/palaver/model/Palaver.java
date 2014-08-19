package de.xsrc.palaver.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.datafx.util.EntityWithId;

@XmlRootElement(name = "palaver")
public class Palaver implements EntityWithId<String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Who should receive user send the msgs
	 */
	private StringProperty recipient;

	/**
	 * Which account should send msgs
	 */
	private StringProperty account;

	@XmlElement(name = "history", type = History.class)
	public History history;

	public Palaver() {
		this.recipient = new SimpleStringProperty();
		this.account = new SimpleStringProperty();
		this.history = new History();
	}

	public String getId() {
		return account.get() + ":" + recipient.get();
	}

	public String getRecipient() {
		return recipient.get();
	}

	public String getAccount() {
		return account.get();
	}

	public void setRecipient(String s) {
		recipient.set(s);
	}

	public void setAccount(String s) {
		account.set(s);
	}

	public void setAccount(Account a) {
		account.set(a.getId());

	}

	public String toString() {
		return getId();
	}

	public void add(Entry entry) {
		history.addEntry(entry);
	}
}
