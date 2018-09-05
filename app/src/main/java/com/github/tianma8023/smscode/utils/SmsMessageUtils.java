package com.github.tianma8023.smscode.utils;

import android.telephony.SmsMessage;

/**
 * Utils about android.telephony.SmsMessage
 */
public class SmsMessageUtils {

    private static final int SMS_CHARACTER_LIMIT = 160;

    private SmsMessageUtils() {
    }

    public static String getMessageBody(SmsMessage[] messageParts) {
        if (messageParts.length == 1) {
            return messageParts[0].getDisplayMessageBody();
        } else {
            StringBuilder sb = new StringBuilder(SMS_CHARACTER_LIMIT * messageParts.length);
            for (SmsMessage messagePart : messageParts) {
                sb.append(messagePart.getDisplayMessageBody());
            }
            return sb.toString();
        }
    }

}
