package org.acme.vaccinationscheduler.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.vaccinationscheduler.domain.Appointment;

@Path("/appointment")
public class AppointmentResource {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/promote/confirmed")
    public Response promoteToConfirmed(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/supply")
    public Response appointmentCancelledNoSupply(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/noshow")
    public Response appointmentCanceledNoShow(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/administered")
    public Response vaccineAdministered(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/notadministered/")
    public Response vaccineNotAdministered(Appointment appointment){
        return Response.ok().build();
    }






    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/accepted")
    public Response appointmentAcceptedByRecipient(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/administered")
    public Response recipientVaccineAdministered(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/notadministered/")
    public Response recipientVaccineNotAdministered(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/declined")
    public Response appointmentDeclinedByRecipient(Appointment appointment){
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/cancelled")
    public Response appointmentCancelledByRecipient(Appointment appointment){
        return Response.ok().build();
    }
    
}
