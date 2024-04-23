package org.pikopika.web.webkit;

import jakarta.ws.rs.ClientErrorException;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

public class CxfFaultListenerImpl implements FaultListener {

    private static final Logger logger = LogManager.getLogger();
    private static final String NEWLINE = "\n";

    public boolean faultOccurred(final Exception exception, final String description, final Message message) {

		if (exception instanceof ClientErrorException) {
			var response = ((ClientErrorException) exception).getResponse();
			if (response.getStatus() == 404) {
				final String uri = (String) message.get(Message.REQUEST_URL);
				logger.error("HTTP 404 , uri:{} ", uri);
			}
			return false;
		}

        createErrorLog(message);
        logger.error(" --------------------------------------------------------------------------------------------------");
        logger.error(" Stack Trace  :         ");
        logger.error(" --------------------------------------------------------------------------------------------------");
        logger.error(NEWLINE, exception);
        logger.error(" --------------------------------------------------------------------------------------------------");

        return true;
    }

    private void createErrorLog(final Message message) {

        final Message inMessage = message.getExchange().getInMessage();

        final InputStream is = inMessage.getContent(InputStream.class);

        final EndpointInfo endpoint = message.getExchange().getEndpoint().getEndpointInfo();
        String logName = null;

        if (endpoint != null && endpoint.getService() != null) {
            final String serviceName = endpoint.getService().getName().getLocalPart();
            final InterfaceInfo iface = endpoint.getService().getInterface();
            final String portName = endpoint.getName().getLocalPart();
            final String portTypeName = iface.getName().getLocalPart();
            logName = serviceName + "." + portName + "." + portTypeName;
        }

        final LoggingMessage buffer
                = new LoggingMessage("Error occured on Service Call  : " + NEWLINE + "----------------------------", logName);

//        ServletRequest request = (ServletRequest) message.get("HTTP.REQUEST");
//        String remoteAddress = request == null ? "NONE" : request.getRemoteAddr();

        final Integer responseCode = (Integer) message.get(Message.RESPONSE_CODE);
        if (responseCode != null) {
            buffer.getResponseCode().append(responseCode);
        }

        final String encoding = (String) message.get(Message.ENCODING);

        if (encoding != null) {
            buffer.getEncoding().append(encoding);
        }
        final String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
        if (httpMethod != null) {
            buffer.getHttpMethod().append(httpMethod);
        }
        final String ct = (String) message.get(Message.CONTENT_TYPE);
        if (ct != null) {
            buffer.getContentType().append(ct);
        }
        final Object headers = message.get(Message.PROTOCOL_HEADERS);

        if (headers != null) {
            buffer.getHeader().append(headers);
        }
        final String uri = (String) message.get(Message.REQUEST_URL);
        if (uri != null) {
            buffer.getAddress().append(uri);
            final String query = (String) message.get(Message.QUERY_STRING);
            if (query != null) {
                buffer.getAddress().append("?").append(query);
            }
        }

//		final String requestXml = is.toString();
//		if (requestXml != null) {
//			buffer.getMessage().append("LoggedIn User:  ");
//			buffer.getMessage().append(getCurrentUsername() + NEWLINE);
//			buffer.getMessage().append("Request payload  : " + NEWLINE);
//			buffer.getMessage().append(requestXml);
//		} else {
//			buffer.getMessage().append("LoggedIn User:  ");
//			buffer.getMessage().append(getCurrentUsername() + NEWLINE);
//			buffer.getMessage().append("No inbound request message to append.");
//		}

        logger.error("########## CXF call error");
        logger.error(buffer.toString());
    }
}