package com.redhat.naps.vaccinationscheduler.rest;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.Appointment;
import org.jboss.logging.Logger;

import com.redhat.naps.vaccinationscheduler.domain.PlanningAppointment;
import com.redhat.naps.vaccinationscheduler.service.AppointmentService;

@Path("/appointment")
public class AppointmentResource {
    
    @Inject
    AppointmentService apptService;
    
    private static final Logger LOG = Logger.getLogger(AppointmentResource.class);
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/promote/confirmed")
    public Response promoteToConfirmed(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentProviderStatus(Appointment.AppointmentStatus.BOOKED.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/supply")
    public Response appointmentCancelledNoSupply(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentProviderStatus(Appointment.AppointmentStatus.CANCELLED.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/noshow")
    public Response appointmentCanceledNoShow(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentProviderStatus(Appointment.AppointmentStatus.NOSHOW.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/administered")
    public Response vaccineAdministered(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentProviderStatus(Appointment.AppointmentStatus.FULFILLED.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/notadministered/")
    public Response vaccineNotAdministered(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentProviderStatus(Appointment.AppointmentStatus.PENDING.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/standby/")
    public Response vaccineStandby(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentProviderStatus(Appointment.AppointmentStatus.PENDING.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/accepted")
    public Response appointmentAcceptedByRecipient(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentRecipientStatus(Appointment.AppointmentStatus.CHECKEDIN.toCode());
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/administered")
    public Response recipientVaccineAdministered(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentRecipientStatus(Appointment.AppointmentStatus.FULFILLED.toCode());
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/notadministered/")
    public Response recipientVaccineNotAdministered(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentRecipientStatus(Appointment.AppointmentStatus.PENDING.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/declined")
    public Response appointmentDeclinedByRecipient(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentRecipientStatus(Appointment.AppointmentStatus.CANCELLED.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/cancelled")
    public Response appointmentCancelledByRecipient(PlanningAppointment appointment){
        PlanningAppointment appt = new PlanningAppointment();
        appt.setAppointmentId(appointment.getAppointmentId());
        appt.setAppointmentRecipientStatus(Appointment.AppointmentStatus.CANCELLED.toCode() );
        apptService.updateNonNull(appt);
        
        return Response.ok().build();
    }
    
}
