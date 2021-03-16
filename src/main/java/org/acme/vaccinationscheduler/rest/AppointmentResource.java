package org.acme.vaccinationscheduler.rest;

import java.util.ArrayList;
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

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.AppointmentProviderStatus;
import org.acme.vaccinationscheduler.service.AppointmentService;
import org.jboss.logging.Logger;

@Path("/appointment")
public class AppointmentResource {
	
	@Inject
	AppointmentService apptService;
	
	private static final Logger LOG = Logger.getLogger(AppointmentResource.class);
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getByAppointmentId/{appointmentId}")
	public Response getByAppointmentId(@PathParam("appointmentId") Long appointmentId ) {
		Optional<Appointment> appt = apptService.findById(appointmentId);
		if(appt.isEmpty()) {
			LOG.debug("No appointment found with id "+appointmentId);
			return Response.status(404).build();
		}
		return Response.ok(appt).build();
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getByVaccinationCenter/{vaccinationCenter}") 
	public Response findByVaccinationCenter(@PathParam("vaccinationCenter") String vaccinationCenter) { 
		List<Appointment> appts = apptService.findByVaccinationCenter(vaccinationCenter); 
		if(appts.isEmpty()) {
			LOG.debug("No appointments found for vaccination center "+vaccinationCenter);
			return Response.status(404).build();
		}
		LOG.debug("Returning list of "+appts.size()+" appointments at vaccination center "+vaccinationCenter);
		return Response.ok(appts).build(); 
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getByPersonId/{personId}") 
	public Response getByPersonId (@PathParam("personId") Long personId ) {
		List<Appointment> appts = apptService.findByPersonId(personId); 
		if(appts.isEmpty()) {
			LOG.debug("No appointments found for person with id "+personId);
			return Response.status(404).build();
		}
		LOG.debug("Returning list of "+appts.size()+" appointments for person with id "+personId);
		return Response.ok(appts).build(); 
	}
	 
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/promote/confirmed")
    public Response promoteToConfirmed(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED);
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/supply")
    public Response appointmentCancelledNoSupply(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentProviderStatus.CANCELLED_PROVIDER_SUPPLY );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/cancelled/noshow")
    public Response appointmentCanceledNoShow(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentProviderStatus.CANCELLED_NO_SHOW );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/administered")
    public Response vaccineAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		appt.setAppointmentStatus(AppointmentProviderStatus.COMPLETED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/provider/vaccine/notadministered/")
    public Response vaccineNotAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		//appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }






    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/accepted")
    public Response appointmentAcceptedByRecipient(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		//appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/administered")
    public Response recipientVaccineAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		//appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/vaccine/notadministered/")
    public Response recipientVaccineNotAdministered(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		//appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/declined")
    public Response appointmentDeclinedByRecipient(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		//appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recipient/cancelled")
    public Response appointmentCancelledByRecipient(Appointment appointment){
		Appointment appt = new Appointment();
		appt.setAppointmentId(appointment.getAppointmentId());
		//appt.setAppointmentStatus(AppointmentProviderStatus.CONFIRMED );
		apptService.updateNonNull(appt);
    	
        return Response.ok().build();
    }
    
}
