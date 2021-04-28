package org.acme.vaccinationscheduler.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.model.EnumFactory;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Appointment;

import org.acme.vaccinationscheduler.domain.PlanningAppointment;

@ApplicationScoped
public class AppointmentMapper {

	private static Logger log = Logger.getLogger(AppointmentMapper.class);

	void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
		log.info("onStart() .... ");
	}
	
	public  Appointment fromAppointment(PlanningAppointment aObj) throws FHIRFormatError, IOException{
		
		Appointment fhirObj = new Appointment();
		fhirObj.setId(Long.toString( aObj.getAppointmentId()));
		
		String status = aObj.getAppointmentProviderStatus();
		fhirObj.setStatusElement(parseEnumeration(status, new Appointment.AppointmentStatusEnumFactory() ));

		List<Appointment.AppointmentParticipantComponent> participants = new ArrayList<Appointment.AppointmentParticipantComponent>();

		String personName = aObj.getPersonName();
		Appointment.AppointmentParticipantComponent person = new Appointment.AppointmentParticipantComponent();
		person.setActor( new Reference( personName ) );
		participants.add(person);
		
		String lName = aObj.getVaccinationCenterName();
		Appointment.AppointmentParticipantComponent location = new Appointment.AppointmentParticipantComponent();
		location.setActor( new Reference( lName ) );
		participants.add(location);
	
		fhirObj.setParticipant(participants);

		return fhirObj;

	}

	private <E extends Enum<E>> Enumeration<E> parseEnumeration(String s, EnumFactory e) throws IOException, FHIRFormatError {
		Enumeration<E> res = new Enumeration<E>(e);
		if (s != null)
		  res.setValue((E) e.fromCode(s));
		return res;
	}
}
