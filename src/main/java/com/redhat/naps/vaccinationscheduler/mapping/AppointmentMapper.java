package com.redhat.naps.vaccinationscheduler.mapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.model.codesystems.ServiceType;
import org.hl7.fhir.r4.model.EnumFactory;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;

import com.redhat.naps.vaccinationscheduler.domain.PlanningAppointment;
import com.redhat.naps.vaccinationscheduler.util.FhirUtil;

@ApplicationScoped
public class AppointmentMapper {

    private static Logger log = Logger.getLogger(AppointmentMapper.class);

    private DateTimeFormatter fhirDateTimeFormatter;
    
    void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
        log.info("onStart() .... ");
        try {
            // https://www.hl7.org/fhir/datatypes.html
            fhirDateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-DDhh:mm:ss+zz:zz");
        }catch(IllegalArgumentException x){
            x.printStackTrace();
            throw x;
        }
    }

    @Inject
    @ConfigProperty(name = FhirUtil.TIMESLOTDURATION_MINUTES, defaultValue = "30")
    int timeSlotDurationMinutes;
    
    public  Appointment fromPlanningAppointment(PlanningAppointment aObj) throws FHIRFormatError, IOException{
        
        Appointment fhirObj = new Appointment();
        List<Appointment.AppointmentParticipantComponent> participants = new ArrayList<Appointment.AppointmentParticipantComponent>();

        // Id
        fhirObj.setId(Long.toString( aObj.getAppointmentId()));
        
        // Status
        String status = aObj.getAppointmentProviderStatus();
        fhirObj.setStatusElement(parseEnumeration(status, new Appointment.AppointmentStatusEnumFactory() ));

        // Person
        String personName = aObj.getPersonName();
        Appointment.AppointmentParticipantComponent person = new Appointment.AppointmentParticipantComponent();
        person.setActor( new Reference( personName ) );
        person.setId(Long.toString(aObj.getPersonId()));
        participants.add(person);
        
        // Location
        String lName = aObj.getVaccinationCenterName();
        HealthcareService hCareService = new HealthcareService();
        hCareService.addLocation(new Reference(lName));
        Appointment.AppointmentParticipantComponent location = new Appointment.AppointmentParticipantComponent();
        location.setActor(new Reference(hCareService));
        participants.add(location);

        // Specify Appointment Type as: immunization 
        List<CodeableConcept> concepts = new ArrayList<CodeableConcept>();
        CodeableConcept immunizationConcept = new CodeableConcept();
        immunizationConcept.addCoding().setCode(ServiceType._57.getDisplay()); // https://www.hl7.org/fhir/valueset-service-type.html#expansion
        concepts.add(immunizationConcept);

        // As text, specify the vaccine type
        immunizationConcept.setText(aObj.getVaccineType().name());

        // As text, specify whether this is the firstDoseAdministered
        immunizationConcept.setText(FhirUtil.FIRST_DOSE_ADMINISTERED +" : "+aObj.getIsFirstDoseAdministered());

        Appointment.AppointmentParticipantComponent immunization = new Appointment.AppointmentParticipantComponent();
        immunization.setType(concepts);
        participants.add(immunization);

        /* Start and End times;  ie:
             start: 2013-12-09T09:00:00Z
             end:   2013-12-09T11:00:00Z
         */
        LocalDateTime startTime = aObj.getTimeslotDateTime();
        LocalDateTime endTime = startTime.now().plusMinutes(timeSlotDurationMinutes);
        fhirObj.setStart(convertToDate(startTime));
        fhirObj.setEnd(convertToDate(endTime));
    
        fhirObj.setParticipant(participants);

        return fhirObj;

    }

    private <E extends Enum<E>> Enumeration<E> parseEnumeration(String s, EnumFactory e) throws IOException, FHIRFormatError {
        Enumeration<E> res = new Enumeration<E>(e);
        if (s != null)
          res.setValue((E) e.fromCode(s));
        return res;
    }

    private Date convertToDate(LocalDateTime timeObj) {
        return Date.from(timeObj.atZone(ZoneId.systemDefault()).toInstant());
    }
}
