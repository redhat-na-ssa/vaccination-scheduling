package com.redhat.naps.vaccinationscheduler.fhir;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import ca.uhn.fhir.context.FhirContext;

import com.redhat.naps.vaccinationscheduler.domain.PlanningPerson;
import com.redhat.naps.vaccinationscheduler.domain.PlanningAppointment;
import com.redhat.naps.vaccinationscheduler.domain.VaccineType;
import com.redhat.naps.vaccinationscheduler.mapping.FhirMapper;
import com.redhat.naps.vaccinationscheduler.rest.FhirServerClient;
import com.redhat.naps.vaccinationscheduler.util.FhirUtil;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

@QuarkusTest
public class FhirMappingTest {

    private static Logger log = Logger.getLogger(FhirMappingTest.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    FhirMapper fhirMapper;

    @Inject
    @ConfigProperty(name = FhirUtil.TEST_INTERACT_WITH_FHIRSERVER, defaultValue = "false")
    boolean interactWithFhirServer;

    @Inject
    @RestClient
    FhirServerClient fhirClient;

    //@Disabled
    @Test
    public void mapFhirToPlanningPatientTest() throws IOException {
        log.info("mapFhirToPlanningPatient() ... ");

        // GET
        Response gResponse = null;
        try {
            gResponse = fhirClient.getPatients();
            assertTrue(gResponse.getStatus() == 200 || gResponse.getStatus() == 201);
            String orgJson = IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8");
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("mapFhirToPlanningPatientTest() # of Patients = "+becs.size());
            assertTrue(becs.size() > 0);

            for(BundleEntryComponent bec : becs){
                Patient pObj = (Patient)bec.getResource();
                try {
                    PlanningPerson person = fhirMapper.fromFhirPatientToPlanningPerson(pObj);
                    assertTrue(pObj.getId().equals(person.getId()));

                }catch(Throwable x){
                    x.printStackTrace();
                }
            }

        }catch(WebApplicationException x) {
            gResponse = x.getResponse();
            log.error("mapFhirToPlanningPatientTest() error status = "+gResponse.getStatus()+"  when getting patients from FhirServer");
            log.error("mapFhirToPlanningPatientTest() error meesage = "+IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8"));
        }
        gResponse.close();

        
    }

    @Disabled
    @Test
    public void mapPlanningToFhirAppointmentTest() throws FHIRFormatError, IOException {
        String patientId = "210";
        String patientIdString = "Patient/"+patientId;
        String patientName = "RHT Shadowman";
        PlanningAppointment oAppointment = createSamplePlanningAppointment(patientId, patientName);
        
        try {
            Appointment appObj = fhirMapper.fromPlanningAppointment(oAppointment);
            String appJson = fhirCtx.newJsonParser().encodeResourceToString(appObj);
    
            log.info("mapPlanningToFhirAppointmentTest() appJSON = \n\n"+appJson+"\n");

            if(interactWithFhirServer){

                Response response = null;
                try {
                    // 1) Start by posting a patient to FHIR server
                    Patient patientObj = createSampleFhirPatient(patientId, patientName);
                    String patientJson = fhirCtx.newJsonParser().encodeResourceToString(patientObj);
                    response = fhirClient.postPatient(patientJson);
                    log.info("mapPlanningToFhirAppointmentTest() patient POST response status = "+response.getStatus());

                    // 2)  Now that patient exists in FHIR server, post corresponding appointment
                    response = fhirClient.postAppointment(appJson);
                    log.info("appointmentMappingTest() appointment POST response status = "+response.getStatus());
                }catch(WebApplicationException x){
                    response = x.getResponse();
                    log.info("mapPlanningToFhirAppointmentTest() response exception = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
                }
            }


        }catch(Throwable x){
            x.printStackTrace();
        }
    }



    private Patient createSampleFhirPatient(String patientId, String patientName) {
        Patient pObj = new Patient();
        pObj.getIdentifier().add(new Identifier().setValue(patientId));
        String[] names = patientName.split(" ");
        HumanName hName = new HumanName().addGiven(names[0]).setFamily(names[1]);
        List<HumanName> hList = new ArrayList<HumanName>();
        hList.add(hName);
        pObj.setName(hList);
        return pObj;
    }
    
    private PlanningAppointment createSamplePlanningAppointment(String patientId, String patientName) {
        PlanningAppointment oAppointment = new PlanningAppointment();
        oAppointment.setAppointmentId(1001L);
        oAppointment.setAppointmentProviderStatus(Appointment.AppointmentStatus.CANCELLED.toCode());
        oAppointment.setAppointmentRecipientStatus(Appointment.AppointmentStatus.FULFILLED.toCode());
        oAppointment.setPersonId(patientId);
        oAppointment.setPersonName(patientName);
        oAppointment.setVaccineType(VaccineType.MODERNA);
        oAppointment.setTimeslotDateTime(LocalDateTime.now());
        return oAppointment;
    }


    
}
