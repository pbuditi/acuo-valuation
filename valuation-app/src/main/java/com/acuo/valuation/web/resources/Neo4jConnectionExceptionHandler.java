package com.acuo.valuation.web.resources;

import org.neo4j.ogm.exception.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/json")
public class Neo4jConnectionExceptionHandler implements ExceptionMapper<ConnectionException> {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4jConnectionExceptionHandler.class);

    @Override
    public Response toResponse(ConnectionException exception) {
        LOG.error("Internal server exception", exception);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
    }

}