package org.acme.vaccinationscheduler.fhir;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.google.inject.Inject;
import org.jboss.logging.Logger;
import org.apache.commons.io.IOUtils;

import org.acme.vaccinationscheduler.rest.FhirServerClient;
import org.acme.vaccinationscheduler.util.FhirUtil;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;


/*
  NOTE:  Requires the FHIR Server to be running.  Information about the FHIR server can be found at the following:

  1)  https://hapifhir.io/hapi-fhir/docs/server_jpa/get_started.html
  2)  https://github.com/hapifhir/hapi-fhir-jpaserver-starter
*/ 

@QuarkusTest
public class FhirServerTest {

    private static Logger log = Logger.getLogger(FhirServerTest.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    @RestClient
    FhirServerClient fhirClient;

    @Disabled
    @Test
    public void observationTest() throws IOException {

        // POST
        String filePath = "/fhir/"+FhirUtil.OBSERVATION_INFORMATION+".json";
        InputStream fStream = null;
        String oJson = null;
        try {
            fStream = this.getClass().getResourceAsStream(filePath);
            if(fStream != null){
                oJson = IOUtils.toString(fStream, "UTF-8");
            }else {
                log.error("observationTest() resource not found: "+filePath);
                return;
            }
        }finally {
            if(fStream != null)
            fStream.close();
        }
        Response response = null;
        try {
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, oJson);
            Observation obs = (Observation)bObj.getEntryFirstRep().getResource();
            String obsJson = fhirCtx.newJsonParser().encodeResourceToString(obs);
            response = fhirClient.postObservation(obsJson);
            assertEquals(201, response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("observationTest() error status = "+response.getStatus()+"  when posting the following file content to FhirServer: "+filePath);
            log.error("observationTest() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();

        // GET
        try {
            response = fhirClient.getObservations();
            assertTrue(response.getStatus() == 200 || response.getStatus() == 201);
            String obsString = IOUtils.toString((InputStream)response.getEntity(), "UTF-8");
            //log.info("observationTest() observations = "+obsString);
            response.close();
            
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, obsString );
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("observationTest() total # of observations = "+becs.size());
            assertTrue(becs.size() > 0);
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("observationTest() error status = "+response.getStatus()+"  when getting Observation from FhirServer");
            log.error("observationTest() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();
    }

    @Disabled
    @Test
    public void hospitalTest() throws IOException, InterruptedException {

        // POST
        String filePath = "/fhir/"+FhirUtil.HOSPITAL_INFORMATION+".json";
        InputStream fStream = null;
        String hospitalJson = null;
        try {
            fStream = this.getClass().getResourceAsStream(filePath);
            if(fStream != null){
                hospitalJson = IOUtils.toString(fStream, "UTF-8");
            }else {
                log.error("postHospitalTest() resource not found: "+filePath);
                return;
            }
        }finally {
            if(fStream != null)
            fStream.close();
        }
        Response response = null;
        try {
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, hospitalJson);
            Organization org = (Organization)bObj.getEntryFirstRep().getResource();
            String orgJson = fhirCtx.newJsonParser().encodeResourceToString(org);
            response = fhirClient.postOrganization(orgJson);
            assertEquals(201, response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("hospitalTest() error status = "+response.getStatus()+"  when posting the following file content to FhirServer: "+filePath);
            log.error("hospitalTest() error meesage = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();

        Thread.sleep(5000);

        // GET
        Response gResponse = null;
        try {
            gResponse = fhirClient.getOrganizations();
            assertTrue(gResponse.getStatus() == 200 || gResponse.getStatus() == 201);
            String orgJson = IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8");
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
            Organization org = (Organization)bObj.getEntryFirstRep().getResource();
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("hospitalTest() # of Organizations = "+becs.size());
            assertTrue(becs.size() > 0);

        }catch(WebApplicationException x) {
            gResponse = x.getResponse();
            log.error("hospitalTest() error status = "+gResponse.getStatus()+"  when getting hospital from FhirServer");
            log.error("hospitalTest() error meesage = "+IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8"));
        }
        gResponse.close();
    }
    
}
