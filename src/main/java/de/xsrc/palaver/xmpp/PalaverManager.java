package de.xsrc.palaver.xmpp;

import de.xsrc.palaver.beans.Palaver;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import java.util.logging.Logger;

public class PalaverManager {

    private static final Logger logger = Logger
            .getLogger(PalaverManager.class.getName());

    public static void sendMsg(Palaver palaver, String body, ConnectionManager connections) {
        logger.finest(String.format("Sending msg from %s to %s: %s", palaver.getAccount(), palaver.getRecipient(), body));


        if (palaver.getConference()) {
            return;
//            TODO FIX MucMessage handling
//			MultiUserChat muc = MucManager.getInstance().getMuc(palaver);
//			try {
//				muc.sendMessage(body);
//			} catch (SmackException.NotConnectedException e) {
//				e.printStackTrace();
//				logger.severe(String.format("Could not send msg to %s", palaver.getRecipient()));
//			}
        } else {
            Message message = new Message(palaver.getRecipient());
            message.setType(Message.Type.chat);
            message.setBody(body);
            XMPPConnection connection = connections.get(palaver.getAccount());
            try {
                connection.sendPacket(message);
            } catch (SmackException.NotConnectedException e) {
                logger.severe(String.format("Could not send msg to conference %s", palaver.getRecipient()));
            }
        }

    }
}
