package org.pikopika.web.webkit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;

public class Assert {
    static Logger logger = LogManager.getLogger(Assert.class);

    static ParameterizedMessageFactory formatter = new ParameterizedMessageFactory();

    public static class AssertException extends RuntimeException {

        private static final long serialVersionUID = 756918358032593099L;
        private final int errorCode;

        public AssertException(String message) {
            this(message, 500);
        }

        public AssertException(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    public static void NotNull(Object obj) {
        if (obj == null)
            throw new AssertException("require not null object");
    }

    public static void NotNull(Object obj, String msg, Object... params) {
        if (obj == null) {
            Message message = formatter.newMessage(msg, params);
            msg = message.getFormattedMessage();
            logger.error(msg);
            throw new AssertException(msg);
        }
    }

    public static void NotNull(Object obj, int errorCode, String msg, Object... params) {
        if (obj == null) {
            logger.error(msg, params);
            throw new AssertException("require not null object", errorCode);
        }
    }

    public static void True(boolean value, int errorCode, String msg, Object... params) {
        if (value == false) {
            Message message = formatter.newMessage(msg, params);
            msg = message.getFormattedMessage();
            logger.error(msg);
            throw new AssertException(msg, errorCode);
        }

    }

    public static void True(boolean value, String msg, Object... params) {
        if (value == false) {
            Message message = formatter.newMessage(msg, params);
            msg = message.getFormattedMessage();
            logger.error(msg);
            throw new AssertException(msg);
        }
    }

    public static void True(boolean value) {
        if (value == false) {
            throw new AssertException("require value should be true");
        }
    }

    public static void False(boolean value) {
        if (value == true) {
            throw new AssertException("require value should be false");
        }
    }

    public static void False(boolean value, String msg, Object... params) {
        if (value == true) {
            Message message = formatter.newMessage(msg, params);
            msg = message.getFormattedMessage();
            logger.error(msg);
            throw new AssertException(msg);
        }
    }

    public static void False(boolean value, int errorCode, String msg, Object... params) {
        if (value) {
            Message message = formatter.newMessage(msg, params);
            msg = message.getFormattedMessage();
            logger.error(msg);
            throw new AssertException(msg, errorCode);
        }
    }
}
