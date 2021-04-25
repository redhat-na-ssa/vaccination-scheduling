package org.acme.vaccinationscheduler.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/fhir")
@RegisterRestClient
public interface FhirServerClient {

    @GET
    @Path("/Observation")
    @Produces("application/fhir+json")
    String getObservations();

    @POST
    @Path("/Observation")
    @Consumes("application/fhir+json")
    String postObservation( String observation);
    
}
