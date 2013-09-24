package com.borqs.notification;

import android.util.Log;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

class UserPresenceIQProvider implements IQProvider {

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
Log.e("----------", "parseIQ: " + parser.getEventType());

        UserPresenceIQ result = new UserPresenceIQ();
        
        boolean done = false;
        while(!done) {
            int eventType = parser.next();
            String name = parser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if(name.equals("status")) {
                    String status = parser.nextText();
                    result.setStatus(status);
                } else if(name.equals("ip")) {
                    String ip = parser.nextText();
                    result.setIp(ip);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if(name.equals(UserPresenceIQ.IQ_USER_PRESENCE_NAME)) {
                    done = true;
                }
            }
        }

        return result;

/*
        IQ iqPacket = null;
        String id = parser.getAttributeValue("", "id");
        String to = parser.getAttributeValue("", "to");
        String from = parser.getAttributeValue("", "from");
        IQ.Type type = IQ.Type.fromString(parser.getAttributeValue("", "type"));
        XMPPError error = null;

        boolean done = false;
        while (!done) {
            int eventType = parser.next();

            if (eventType == XmlPullParser.START_TAG) {
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                if (elementName.equals("error")) {
                    // TODO: generate a corresponding error.
                    //error = PacketParserUtils.parseError(parser);
                    error = new XMPPError(999);
                }
                else if (elementName.equals(UserPresenceIQ.IQ_USER_PRESENCE_NAME) && 
                        namespace.equals(UserPresenceIQ.IQ_USER_PRESENCE_NAMESPACE)) {
                    iqPacket = parseUserPresence(parser);
                }
                else {
                    // Something wrong.
                    // TODO: generate a corresponding error.
                    error = new XMPPError(998);
                }
            }
            else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("iq")) {
                    done = true;
                }
            }
        }
        // Decide what to do when an IQ packet was not understood
        if (iqPacket == null) {
            // create an empty IQ packet.
            iqPacket = new IQ() {
                public String getChildElementXML() {
                    return null;
                }
            };
        }

        // Set basic values on the iq packet.
        iqPacket.setPacketID(id);
        iqPacket.setTo(to);
        iqPacket.setFrom(from);
        iqPacket.setType(type);
        iqPacket.setError(error);
        return iqPacket;
*/
    }
    
    private static IQ parseUserPresence(XmlPullParser parser) throws Exception {
        UserPresenceIQ result = null;
        
        boolean done = false;
        while(!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if(parser.getName().equals("status")) {
                    String status = parser.nextText();
                    result.setStatus(status);
                } else if(parser.getName().equals("ip")) {
                    String ip = parser.nextText();
                    result.setIp(ip);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if(parser.getName().equals("status")) {
                    done = true;
                }
            }
        }

        return result;
    }
}