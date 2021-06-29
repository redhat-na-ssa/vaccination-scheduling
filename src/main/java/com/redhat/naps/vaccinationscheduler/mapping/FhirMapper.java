package com.redhat.naps.vaccinationscheduler.mapping;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DecimalType;

import com.redhat.naps.vaccinationscheduler.domain.PlanningInjection;
import com.redhat.naps.vaccinationscheduler.domain.PlanningVaccinationCenter;
import com.redhat.naps.vaccinationscheduler.domain.PlanningLocation;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPerson;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPractitioner;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPractitionerRole;
import com.redhat.naps.vaccinationscheduler.util.FhirUtil;

@ApplicationScoped
public class FhirMapper {

    private static Logger log = Logger.getLogger(FhirMapper.class);

    private DateTimeFormatter fhirDateTimeFormatter;
    
    void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
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
    
    public LocalDateTime fromFhirSlotToPlanningSlot(Slot fhirSlot){

        LocalDateTime ldt = convertToLocalDateTime(fhirSlot.getStart());
        return ldt;
    }

    public PlanningPractitioner fromFhirPractitionerToPlanningPractitioner(Practitioner pObj) {
    	String name = (pObj.getName()==null || pObj.getName().isEmpty())?"":pObj.getName().get(0).getFamily();
    	PlanningPractitioner pPrac = new PlanningPractitioner(name);
    	
    	return pPrac;
    }

	public PlanningPractitionerRole fromFhirPractitionerRoleToPlanningPractitionerRole(PractitionerRole pr) {
		PlanningPractitionerRole pRole = new PlanningPractitionerRole();

		pRole.setPractitionerId(pr.getPractitioner().getId());
		pRole.setPractitionerName(pr.getPractitioner().getDisplay());
		pRole.setVaccinationCenterId(pr.getOrganization().getId());
		pRole.setVaccinationCenterName(pr.getOrganization().getDisplay());
		return pRole;
	}

    public PlanningVaccinationCenter fromFhirOrganizationToPlanningVaccinationCenter(Organization pObj, Location lObj) {
    	String id = pObj.getId();
        String name = pObj.getName();
        LocationPositionComponent lpObj = lObj.getPosition();
        PlanningLocation pLocation = new PlanningLocation(lpObj.getLatitude().doubleValue(), lpObj.getLongitude().doubleValue());
        
        //TO-DO:  Investigate purpose of PlanningVaccinationCenter.lineCount
        PlanningVaccinationCenter pvc = new PlanningVaccinationCenter(id, name, pLocation, 1);
        return pvc;
    }


    public PlanningPerson fromFhirPatientToPlanningPerson(Patient pObj) {
        String patientId = pObj.getIdElement().getIdPart();
        /*List<Identifier> ids = pObj.getIdentifier();
        for(Identifier id : ids) {
            log.info("identifier = "+id.getSystemElement().asStringValue()+" : "+ id.getValue());
        }*/
        HumanName name = pObj.getName().get(0);
        String fullName = name.getGivenAsSingleString()+" "+name.getFamily();

        Date birthDate = pObj.getBirthDate();
        if(birthDate == null){
          log.warnv("{0} fromFhirPatientToPlanningPerson() No birthday from FHIR Patient .... will set to NOW", fullName);
          birthDate = new Date();
        }
        LocalDate lBirthDate = convertToLocalDate(birthDate);
        Period period = Period.between(lBirthDate, LocalDate.now());

        // Set PlanningLocation on PlanningPerson
        PlanningLocation pLocation = new PlanningLocation();
        List<Address> addresses = pObj.getAddress();
        if(addresses.size() > 0){
            Address addressObj = pObj.getAddress().get(0);
            Extension eObj = addressObj.getExtensionByUrl(FhirUtil.PATIENT_ADDRESS_EXTENSION);
            if(eObj != null) {
                List<Extension> extensions = eObj.getExtension();
                for(Extension extension: extensions){
                    Property urlProp = extension.getNamedProperty(FhirUtil.URL);
                    List<Base> urlPropValues = urlProp.getValues();
                    UriType uri = (UriType) urlPropValues.get(0);
                    Property vd = extension.getNamedProperty(FhirUtil.VALUE_DECIMAL);
                    List<Base> vdPropValues = vd.getValues();
                    DecimalType decimalV = (DecimalType) vdPropValues.get(0);
                    //log.info(fullName+ "  fromFhirPatientToPlanningPerson() geolocation:  "+ uri.getValue() +" = "+decimalV.getValue() );
                    if(uri.getValue().equals(FhirUtil.LATITUDE)) {
                        double lat = decimalV.getValue().doubleValue();
                        pLocation.setLatitude(lat);
                    }
                    else {
                        double lon = decimalV.getValue().doubleValue();
                        pLocation.setLongitude(lon);
                    }
                }
            }else{
                log.warnv("{0} fromFhirPatientToPlanningPerson() no geolocation address extension for patient. Will set to North Pole", fullName);
                pLocation = new PlanningLocation(90.00, 135.00);
            }
        }else{
            log.warnv("{0}  fromFhirPatientToPlanningPerson() No address from patient. Will set to North Pole", fullName);
            pLocation = new PlanningLocation(90.00, 135.00);
        }

        PlanningPerson person = new PlanningPerson(patientId, fullName, pLocation, lBirthDate, period.getYears());
        return person;
    }
    


    public  Appointment fromPlanningInjectionToFhirAppointment(PlanningInjection aObj) throws FHIRFormatError, IOException{
        
        Appointment fhirObj = new Appointment();
        List<Appointment.AppointmentParticipantComponent> participants = new ArrayList<Appointment.AppointmentParticipantComponent>();

        // Id
        fhirObj.setId(Long.toString( aObj.getId() ));

        // Patient
        String personName = aObj.getPerson().getName();
        Appointment.AppointmentParticipantComponent patient = new Appointment.AppointmentParticipantComponent();
        patient.setActor( new Reference( "Patient/"+aObj.getPerson().getId() ).setDisplay(personName) );
        //patient.setId(aObj.getPerson().getId() );
        participants.add(patient);
        
        // Location
        String lName = aObj.getVaccinationCenter().getName();
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
        immunizationConcept.setText(FhirUtil.FIRST_DOSE_ADMINISTERED +" : "+aObj.getPerson().isFirstShotInjected() );
        LocalDate secondIdealDate = aObj.getPerson().getSecondShotIdealDate();
        if(secondIdealDate != null){
            immunizationConcept.setText(FhirUtil.SECOND_SHOT_IDEAL_DATE+" : "+fhirDateTimeFormatter.format(aObj.getPerson().getSecondShotIdealDate() ));
        }

        Appointment.AppointmentParticipantComponent immunization = new Appointment.AppointmentParticipantComponent();
        immunization.setType(concepts);
        participants.add(immunization);

        // Time Slot
        LocalDateTime startTime = aObj.getDateTime();
        Date startDate = convertToDate(startTime);
        LocalDateTime endTime = startTime.now().plusMinutes(timeSlotDurationMinutes);
        Date endDate = convertToDate(endTime);
        fhirObj.setStart(startDate);
        fhirObj.setEnd(endDate);
        Slot slot = new Slot();
        slot.setStart(startDate);
        slot.setEnd(endDate);
        fhirObj.addSlot(new Reference(slot));

        // Status
        //fhirObj.setStatusElement(parseEnumeration(status, new Appointment.AppointmentStatusEnumFactory() ));
    
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

    private LocalDate convertToLocalDate(Date dObj) {
        return dObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime convertToLocalDateTime(Date dObj) {
        return dObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
