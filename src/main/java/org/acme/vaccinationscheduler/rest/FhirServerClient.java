package org.acme.vaccinationscheduler.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/fhir")
@RegisterRestClient
public interface FhirServerClient {

    @GET
    @Path("/Observation")
    @Produces("application/fhir+json")
    Response getObservations();

    @POST
    @Path("/Observation")
    @Consumes("application/fhir+json")
    Response postObservation( String observationJson);

    @GET
    @Path("/Practitioner")
    @Produces("application/fhir+json")
    Response getPractitioners();

    @POST
    @Path("/Practitioner")
    @Consumes("application/fhir+json")
    Response postPractitioner( String practitionerJson);

    @GET
    @Path("/Organization")
    @Produces("application/fhir+json")
    Response getOrganization();

    @POST
    @Path("/Organization")
    @Consumes("application/fhir+json")
    Response postOrganization( String organizationJson);

    @GET
    @Path("/Patient")
    @Produces("application/fhir+json")
    Response getPatient();

    @POST
    @Path("/Patient")
    @Consumes("application/fhir+json")
    Response postPatient( String patientJson);
    
}
