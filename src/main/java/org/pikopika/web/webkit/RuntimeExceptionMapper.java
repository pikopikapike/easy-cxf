package org.pikopika.web.webkit;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuntimeExceptionMapper implements ExceptionMapper<Exception> {
    static Logger logger = LogManager.getLogger();

    @Override
    public Response toResponse(Exception exception) {
        String message = String.format("{\"res\": %d, \"error\":\"%s\"}", 500, exception.getMessage());
        logger.error("assert error", exception);
        return Response.status(Response.Status.OK).entity(message).build();
    }
}
