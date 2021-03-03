package org.acme.vaccinationscheduler.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.AppointmentStatus;
import org.acme.vaccinationscheduler.service.AppointmentService;

@Path("/appointment")
public class AppointmentResource {
	
	@Inject
	AppointmentService apptService;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/promote/confirmed")
    public Response promoteToConfirmed(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED);
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/supply")
    public Response appointmentCancelledNoSupply(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CANCELLED_PROVIDER_SUPPLY );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/noshow")
    public Response appointmentCanceledNoShow(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CANCELLED_NO_SHOW );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/administered")
    public Response vaccineAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.COMPLETED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/notadministered/")
    public Response vaccineNotAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }






    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/accepted")
    public Response appointmentAcceptedByRecipient(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/administered")
    public Response recipientVaccineAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/notadministered/")
    public Response recipientVaccineNotAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/declined")
    public Response appointmentDeclinedByRecipient(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/cancelled")
    public Response appointmentCancelledByRecipient(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }
    
}
