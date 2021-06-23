package com.redhat.naps.vaccinationscheduler.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;



// As per:  https://www.hl7.org/fhir/resourcelist.html

@Path("/fhir")
@RegisterRestClient
public interface FhirServerClient {


    /**********      Individuals      ***************/
    @GET
    @Path("/Patient")
    @Produces("application/fhir+json")
    Response getPatients();

    @POST
    @Path("/Patient")
    @Consumes("application/fhir+json")
    Response postPatient( String patientJson);

    @GET
    @Path("/Practitioner")
    @Produces("application/fhir+json")
    Response getPractitioners();

    @POST
    @Path("/Practitioner")
    @Consumes("application/fhir+json")
    Response postPractitioner( String practitionerJson);

    @POST
    @Path("/PractitionerRole")
    @Consumes("application/fhir+json")
    Response postPractitionerRole(String practiionerRoleJson);

    @GET
    @Path("/PractitionerRole")
    @Produces("application/fhir+json")
    Response getPractionerRoles();




    /**********      Entities #1      ***************/
    @GET
    @Path("/Organization")
    @Produces("application/fhir+json")
    Response getOrganizations();

    @POST
    @Path("/Organization")
    @Consumes("application/fhir+json")
    Response postOrganization( String organizationJson);

    @GET
    @Path("/Location")
    @Produces("application/fhir+json")
    Response getLocations();

    @GET
    @Path("/Location")
    @Produces("application/fhir+json")
    Response getLocationByOrgName(@QueryParam("name") String orgName);

    @POST
    @Path("/Location")
    @Consumes("application/fhir+json")
    Response postLocation( String locationJson);




    /**********      Workflow & Management      ***************/
    @GET
    @Path("/Appointment")
    @Produces("application/fhir+json")
    Response getAppointments();

    @POST
    @Path("/Appointment")
    @Consumes("application/fhir+json")
    Response postAppointment( String appointmentJson);

    @GET
    @Path("/Schedule")
    @Produces("application/fhir+json")
    Response getSchedules();

    @POST
    @Path("/Schedule")
    @Consumes("application/fhir+json")
    Response postSchedule( String scheduleJson);

    @GET
    @Path("/Encounter")
    @Produces("application/fhir+json")
    Response getEncounters();

    @POST
    @Path("/Encounter")
    @Consumes("application/fhir+json")
    Response postEncounter( String encounterJson);





    /**********      Diagnostics      ***************/
    @GET
    @Path("/Observation")
    @Produces("application/fhir+json")
    Response getObservations();

    @POST
    @Path("/Observation")
    @Consumes("application/fhir+json")
    Response postObservation( String observationJson);


    
}
