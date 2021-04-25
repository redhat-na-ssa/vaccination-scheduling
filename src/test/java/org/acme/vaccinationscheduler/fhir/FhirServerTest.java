package org.acme.vaccinationscheduler.fhir;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import java.util.List;

import com.google.inject.Inject;

import org.acme.vaccinationscheduler.rest.FhirServerClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

@QuarkusTest
public class FhirServerTest {

    private static Logger log = Logger.getLogger(FhirServerTest.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    @RestClient
    FhirServerClient fhirClient;

    @Test
    public void testGetObservation() {

        String observationsString = fhirClient.getObservations();
        log.info("testGetObservation() observations = "+observationsString);

        Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, observationsString );

        List<BundleEntryComponent> becs = bObj.getEntry();
        log.info("testGetObservation() total # of observations = "+becs.size());
        for(BundleEntryComponent bec : becs) {
            Observation obs = (Observation)bec.getResource();
            log.info("testGetObservation() obervation id = "+obs.getId());
        }
        


    }
    
}