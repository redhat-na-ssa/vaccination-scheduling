package com.redhat.naps.vaccinationscheduler.fhir;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
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
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import com.redhat.naps.vaccinationscheduler.domain.PlanningPerson;
import com.redhat.naps.vaccinationscheduler.domain.PlanningVaccinationCenter;
import com.redhat.naps.vaccinationscheduler.domain.PlanningAppointment;
import com.redhat.naps.vaccinationscheduler.domain.PlanningInjection;
import com.redhat.naps.vaccinationscheduler.domain.PlanningLocation;
import com.redhat.naps.vaccinationscheduler.domain.VaccineType;
import com.redhat.naps.vaccinationscheduler.mapping.FhirMapper;
import com.redhat.naps.vaccinationscheduler.rest.FhirServerClient;
import com.redhat.naps.vaccinationscheduler.util.FhirUtil;

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

    @Disabled
    @Test
    public void mapFhirOrganizationToPlanningVaccinationCenterTest() throws IOException {

        // GET
        Response gResponse = null;
        try {
            gResponse = fhirClient.getOrganizations();
            assertTrue(gResponse.getStatus() == 200 || gResponse.getStatus() == 201);
            String orgJson = IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8");
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("mapFhirOrganizationToPlanningVaccinationCenterTest() # of Organizations = "+becs.size());
            assertTrue(becs.size() > 0);

            for(BundleEntryComponent bec : becs){
                Organization pObj = (Organization)bec.getResource();
                try {
                    PlanningVaccinationCenter pvc = fhirMapper.fromFhirOrganizationToPlanningVaccinationCenter(pObj);
                    assertTrue(pObj.getName().equals(pvc.getName()));

                }catch(Throwable x){
                    x.printStackTrace();
                }
            }

        }catch(WebApplicationException x) {
            gResponse = x.getResponse();
            log.error("mapFhirOrganizationToPlanningVaccinationCenterTest() error status = "+gResponse.getStatus()+"  when getting patients from FhirServer");
            log.error("mapFhirOrganizationToPlanningVaccinationCenterTest() error meesage = "+IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8"));
        }
        gResponse.close();
    }

    @Disabled
    @Test
    public void mapFhirPatientToPlanningPatientTest() throws IOException {

        // GET
        Response gResponse = null;
        try {
            gResponse = fhirClient.getPatients();
            assertTrue(gResponse.getStatus() == 200 || gResponse.getStatus() == 201);
            String orgJson = IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8");
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("mapFhirPatientToPlanningPatientTest() # of Patients = "+becs.size());
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
            log.error("mapFhirPatientToPlanningPatientTest() error status = "+gResponse.getStatus()+"  when getting patients from FhirServer");
            log.error("mapFhirPatientToPlanningPatientTest() error meesage = "+IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8"));
        }
        gResponse.close();
    }

    /*  Vaccination Schedule planner will generate PlanningAppointments.
        These Planning Appointments will need to be mapped to FHIR Appointments and pushed to the FHIR server
    */
    @Disabled
    @Test
    public void mapPlanningInjectionToFhirAppointmentTest() throws FHIRFormatError, IOException {
        String patientId = "210";
        String patientName = "RHT Shadowman";
        PlanningInjection pInjection = createSamplePlanningInjection(patientId, patientName);
        
        try {
            Appointment appObj = fhirMapper.fromPlanningInjectionToFhirAppointment(pInjection);
            String appJson = fhirCtx.newJsonParser().encodeResourceToString(appObj);
    
            log.info("mapPlanningAppointmentToFhirAppointmentTest() appJSON = \n\n"+appJson+"\n");

            if(interactWithFhirServer){

                Response response = null;
                try {
                    // 1) Start by posting a patient to FHIR server
                    Patient patientObj = createSampleFhirPatient(patientId, patientName);
                    String patientJson = fhirCtx.newJsonParser().encodeResourceToString(patientObj);
                    response = fhirClient.postPatient(patientJson);
                    log.info("mapPlanningAppointmentToFhirAppointmentTest() patient POST response status = "+response.getStatus());

                    // 2)  Now that patient exists in FHIR server, post corresponding appointment
                    response = fhirClient.postAppointment(appJson);
                    log.info("mapPlanningAppointmentToFhirAppointmentTest() appointment POST response status = "+response.getStatus());
                }catch(WebApplicationException x){
                    response = x.getResponse();
                    log.info("mapPlanningAppointmentToFhirAppointmentTest() response exception = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
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
    
    private PlanningInjection createSamplePlanningInjection(String patientId, String patientName) {

        PlanningPerson person = new PlanningPerson(patientId, patientName, null, LocalDate.now(), 77, false, VaccineType.MODERNA, LocalDate.now());
        PlanningVaccinationCenter pvc = new PlanningVaccinationCenter("CHANGEME", new PlanningLocation(90.00, 135.00), 1);
        PlanningInjection pInjection = new PlanningInjection(1001L, pvc, 1, LocalDateTime.now(), VaccineType.MODERNA, person);
        return pInjection;
    }


    
}
