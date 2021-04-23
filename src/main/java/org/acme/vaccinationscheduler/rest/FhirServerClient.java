package org.acme.vaccinationscheduler.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@RegisterRestClient
public interface FhirServerClient {

    @GET
    @Path("/responder/byname/{name}")
    @Produces("application/json")
    String getByName( @DefaultValue("error") @PathParam("name") String name);
    
}
