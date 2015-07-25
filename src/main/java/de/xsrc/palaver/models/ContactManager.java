package de.xsrc.palaver.models;

import de.xsrc.palaver.beans.Contact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.jxmpp.util.XmppStringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;

// Crosspoint between view, smack and ContactProvider (file backend)
public class ContactManager {
    private static final Logger logger = Logger.getLogger(ContactManager.class
            .getName());

    private final ObservableSet<Contact> data = FXCollections.observableSet(new HashSet<>());

    public static Contact createContact(String account, String jid, String name, Boolean conference) {
        Contact contact = new Contact();
        contact.setAccount(account);
        contact.setJid(jid);
        if (name != null && name.length() > 0) {
            contact.setName(name);
        } else {
            contact.setName(XmppStringUtils.parseLocalpart(contact.getJid()));
        }
        if (conference) {
            contact.setConference(true);
        }
        return contact;
    }
    public ObservableList<Contact> getData() {
        final LinkedList<Contact> contactLinkedList = new LinkedList<>(data);
        return FXCollections.observableList(contactLinkedList);
    }


    public void addContact(Contact contact) {
        data.add(contact);
    }

    public void updateContact(Contact contact) {
        throw new UnsupportedOperationException();
    }

    public void removeContact(Contact contact) {
        data.remove(contact);
    }
}
