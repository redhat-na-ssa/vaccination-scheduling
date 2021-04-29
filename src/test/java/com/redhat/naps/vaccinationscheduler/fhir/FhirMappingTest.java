package com.redhat.naps.vaccinationscheduler.fhir;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import ca.uhn.fhir.context.FhirContext;

import com.redhat.naps.vaccinationscheduler.domain.PlanningAppointment;
import com.redhat.naps.vaccinationscheduler.domain.VaccineType;
import com.redhat.naps.vaccinationscheduler.mapping.AppointmentMapper;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.model.Appointment;

@QuarkusTest
public class FhirMappingTest {

    private static Logger log = Logger.getLogger(FhirMappingTest.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    AppointmentMapper appointmentMapper;

    @Test
    public void appointmentMappingTest() throws FHIRFormatError, IOException {
        PlanningAppointment oAppointment = createSampleOptaplannerAppointment();
        
        try {
            Appointment appObj = appointmentMapper.fromPlanningAppointment(oAppointment);
            String appJson = fhirCtx.newJsonParser().encodeResourceToString(appObj);
    
            log.info("appointmentMappingTest() appJSON = \n\n"+appJson+"\n");

        }catch(Throwable x){
            x.printStackTrace();
        }
        
    }
    
    private PlanningAppointment createSampleOptaplannerAppointment() {
        PlanningAppointment oAppointment = new PlanningAppointment();
        oAppointment.setAppointmentId(1001L);
        oAppointment.setAppointmentProviderStatus(Appointment.AppointmentStatus.CANCELLED.toCode());
        oAppointment.setAppointmentRecipientStatus(Appointment.AppointmentStatus.FULFILLED.toCode());
        oAppointment.setPersonId(1002L);
        oAppointment.setPersonName("Shadowman");
        oAppointment.setVaccineType(VaccineType.MODERNA);
        oAppointment.setTimeslotDateTime(LocalDateTime.now());
        return oAppointment;
    }


    
}
