package org.pikopika.web.webkit;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssertExceptionMapper implements ExceptionMapper<Assert.AssertException> {
    static Logger logger = LogManager.getLogger();

    @Override
    public Response toResponse(Assert.AssertException exception) {
        String message = String.format("{\"res\": %d, \"error\":\"%s\"}", exception.getErrorCode(), exception.getMessage());
        logger.error("assert error", exception);
        return Response.status(Response.Status.OK).entity(message).build();
    }
}
