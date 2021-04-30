package com.redhat.naps.vaccinationscheduler;

import com.redhat.naps.vaccinationscheduler.domain.FhirServerAdminConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import ca.uhn.fhir.context.FhirContext;

import org.mitre.synthea.engine.Generator;
import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.export.Exporter;

import com.redhat.naps.vaccinationscheduler.util.FhirUtil;
import com.redhat.naps.vaccinationscheduler.rest.FhirServerClient;

@Path("/fhirServerAdmin")
public class FhirServerAdminResource {

    private static FhirContext fhirCtx = FhirContext.forR4();
    private static Logger log = Logger.getLogger(FhirServerAdminResource.class);

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_COUNT, defaultValue = "1")
    int patientGeneratorCount;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_STATE, defaultValue = "Michigan")
    String patientGeneratorState;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_CITY, defaultValue = "Detroit")
    String patientGeneratorCity;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_BASE_DIR, defaultValue = "/tmp")
    String patientGeneratorBaseDir;

    @Inject
    @RestClient
    FhirServerClient fhirClient;

    public void onStart(@Observes @Priority(value = 1) StartupEvent ev) {
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/seedFhirServer/")
    public Response seedFhirServer(final FhirServerAdminConfig adminConfig) throws InterruptedException, IOException {

        if(adminConfig != null){
            this.patientGeneratorCount = adminConfig.getPatientGeneratorCount();
            if(StringUtils.isNotEmpty(adminConfig.getPatientGeneratorCity()))
                this.patientGeneratorCity = adminConfig.getPatientGeneratorCity();
            if(StringUtils.isNotEmpty(adminConfig.getPatientGeneratorState()))
                this.patientGeneratorState = adminConfig.getPatientGeneratorState();
        }

        // 1)  Define a directory on the filesystem where output files will be written to
        long randomSeed = ThreadLocalRandom.current().nextLong(100, 100000);
        String outputDir = this.patientGeneratorBaseDir+"/"+randomSeed+"/";
        log.info("onStart() .... will generate the following # of patients: "+patientGeneratorCount+" to output dir = "+outputDir+" at the following location: "+patientGeneratorCity+" "+patientGeneratorState);

        // 2)  Control demographics of population
        Generator.GeneratorOptions options = new Generator.GeneratorOptions();
        options.population = patientGeneratorCount;
        options.city = this.patientGeneratorCity;
        options.state = this.patientGeneratorState;
        options.seed = randomSeed;

        // 3)  Set Common Configuration options as per:  https://github.com/synthetichealth/synthea/wiki/Common-Configuration
        Config.set("exporter.baseDirectory", outputDir);
        Config.set("exporter.fhir.transaction_bundle", "true");
        Config.set("exporter.fhir.export", "true");
        Config.set("exporter.hospital.fhir.export", "true");
        Config.set("exporter.practitioner.fhir.export", "true");
        Config.set("generate.only_live_patients", "true");
        Exporter.ExporterRuntimeOptions ero = new Exporter.ExporterRuntimeOptions();
        ero.enableQueue(Exporter.SupportedFhirVersion.R4);
        Generator generator = new Generator(options, ero);

        // 4) Run the FHIR resource generator
        ExecutorService generatorService = Executors.newFixedThreadPool(1);
        generatorService.submit(() -> generator.run());

        // 5)  Seed FHIR Server with patient resources
        seedPatients(patientGeneratorCount, ero);

        generatorService.shutdownNow();
        
        Thread.sleep(5000);
        
        // 6) Seed FHIR Server with Hospital and Practitioner resources
        seedHospitalAndPractitioners(outputDir);

        return Response.ok(patientGeneratorCount).build();
    }
    
    private void seedPatients(int patientGeneratorCount, Exporter.ExporterRuntimeOptions ero) throws InterruptedException, IOException {
        int fhirRecordCount = 0;
        while(fhirRecordCount < patientGeneratorCount) {
            String jsonRecord = ero.getNextRecord();
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, jsonRecord);
            Patient pObj = (Patient)bObj.getEntryFirstRep().getResource();
            String pJson = fhirCtx.newJsonParser().encodeResourceToString(pObj);
            Response response = null;
            try {
                response = fhirClient.postPatient(pJson);
                log.infov("{1}    seedPatients() fhir server status code: {2}", pObj.getId(), response.getStatus());
            }catch(WebApplicationException x){
                response = x.getResponse();
                log.error("seedPatients() error status = "+response.getStatus()+"  when posting the following Patient to the FhirServer: "+pObj.getId());
                log.error("seedPatients() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
            }
            response.close();
            fhirRecordCount++;
            //log.info("\n\n\njsonRecord = "+jsonRecord);
            log.info("record count = "+fhirRecordCount);
        }
              
        log.info("seedPatients() Total # of fhir records = "+fhirRecordCount);
    }
    
    // FHIR resource generator outputs hospital and practitioner resources only to the filesystem
    // Thus, this function reads those resources from the filesystem 
    private void seedHospitalAndPractitioners(String outputDir) throws IOException {
        File outputDirFile = new File(outputDir+"fhir");
        if(outputDirFile.exists()){
            File[] files = outputDirFile.listFiles();
            log.info("The following # of files have been found in "+outputDirFile.getAbsolutePath()+" : "+files.length);
            for(File file : files) {
                if(file.getName().startsWith(FhirUtil.HOSPITAL_INFORMATION)){
                    log.info("found hospital file: "+ file.getAbsolutePath());
                    seedHospital(file);
                }else if (file.getName().startsWith(FhirUtil.PRACTITIONER_INFORMATION)){
                    log.info("found practitioner file: "+ file.getAbsolutePath());
                    seedPractitioner(file);
                }else{
                    log.info("skipping the following file: "+file.getAbsolutePath());
                }
            }
        }else {
            log.error("The following directory does not exist: "+outputDirFile.getAbsolutePath());
        }
    }

    private void seedHospital(File hospitalFile) throws IOException {
        InputStream fStream = null;
        String bundleString = null;
        try {
            fStream = new FileInputStream(hospitalFile);
            bundleString = IOUtils.toString(fStream, "UTF-8");
        }finally {
            if(fStream != null)
              fStream.close();
        }
        Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, bundleString);
        Organization pObj = (Organization)bObj.getEntryFirstRep().getResource();
        String pJson = fhirCtx.newJsonParser().encodeResourceToString(pObj);
        Response response = null;
        try {
            response = fhirClient.postOrganization(pJson);
            log.infov("{1}    seedHospital() fhir server status code: {2}", pObj.getId(), response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("seedHospital() error status = "+response.getStatus()+"  when posting the following Organization to the FhirServer: "+pObj.getId());
            log.error("seedHospital() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();
    }

    private void seedPractitioner(File practitionerFile) throws IOException {
        InputStream fStream = null;
        String bundleString = null;
        try {
            fStream = new FileInputStream(practitionerFile);
            bundleString = IOUtils.toString(fStream, "UTF-8");
        }finally {
            if(fStream != null)
              fStream.close();
        }
        Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, bundleString);
        Practitioner pObj = (Practitioner)bObj.getEntryFirstRep().getResource();
        String pJson = fhirCtx.newJsonParser().encodeResourceToString(pObj);
        Response response = null;
        try {
            response = fhirClient.postPractitioner(pJson);
            log.infov("{1}    seedPractitioner() fhir server status code: {2}", pObj.getId(), response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("seedPractitioner() error status = "+response.getStatus()+"  when posting the following Practitioner to the FhirServer: "+pObj.getId());
            log.error("seedPractitioner() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();
    }

}