package com.redhat.naps.vaccinationscheduler;

import com.redhat.naps.vaccinationscheduler.domain.FhirServerAdminConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import ca.uhn.fhir.context.FhirContext;

import org.mitre.synthea.engine.Generator;
import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.export.Exporter;

import com.redhat.naps.vaccinationscheduler.util.FhirUtil;
import com.redhat.naps.vaccinationscheduler.rest.FhirServerClient;

@ApplicationScoped
public class FhirServerAdminService {

    private static FhirContext fhirCtx = FhirContext.forR4();
    private static Logger log = Logger.getLogger(FhirServerAdminService.class);

    @Inject
    @ConfigProperty(name = FhirUtil.SEED_FHIR_SERVER_AT_STARTUP, defaultValue = "false")
    boolean seedFhirServerAtStartup;

    @Inject
    @ConfigProperty(name = FhirUtil.SLEEP_MILLIS_AFTER_FHIR_GENERATION, defaultValue = "5000")
    int sleepMillisAfterFhirGeneration;

    @Inject
    @ConfigProperty(name = FhirUtil.SLEEP_MILLIS_AFTER_HOSPITAL_POST, defaultValue = "5000")
    int sleepMillisAfterHospitalPost;

    @Inject
    @ConfigProperty(name = FhirUtil.HOSPITAL_GENERATOR_COUNT, defaultValue = "1")
    int hospitalGeneratorCount;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_COUNT, defaultValue = "1")
    int patientGeneratorCount;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_MIN_AGE, defaultValue = "15")
    int patientMinAge;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_MAX_AGE, defaultValue = "100")
    int patientMaxAge;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_STATE, defaultValue = "Michigan")
    String patientGeneratorState;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_CITY, defaultValue = FhirUtil.OPTIONAL)
    String patientGeneratorCity;

    @Inject
    @ConfigProperty(name = FhirUtil.PATIENT_GENERATOR_BASE_DIR, defaultValue = "/tmp")
    String patientGeneratorBaseDir;

    @Inject
    @RestClient
    FhirServerClient fhirClient;

    public void onStart(@Observes @Priority(value = 1) StartupEvent ev) throws InterruptedException, IOException {
        if(seedFhirServerAtStartup) {
            log.info("onStart() .... will seed FHIR Server");
            seedFhirServer(null);
        }
    }

    // This function will return null if Organization doesn't exist
    public Location getLocationFromOrganization(Organization org) throws IOException {
        String orgName = org.getName();
        log.trace("getLocationFromOrganization() orgName = "+orgName);
        Response lResponse = null;
        try {
            lResponse = fhirClient.getLocationByOrgName(orgName);
            String lString = IOUtils.toString((InputStream)lResponse.getEntity(), "UTF-8");
            log.trace("getLocationFromOrganization() lString = \n\n"+lString+"\n\n");
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, lString );
            Location lObj = null;
            if(bObj.getTotal() > 0) {
                lObj = (Location)bObj.getEntryFirstRep().getResource();
                log.trace("getLocationFromOrganization() found Location with lat of: "+lObj.getPosition().getLatitude().doubleValue());
            }
            return lObj;
        }catch(WebApplicationException x){
            lResponse = x.getResponse();
            log.error("getLocationFromOrg() error status = "+lResponse.getStatus()+"  when getting Observation from FhirServer");
            log.error("getLocationFromOrg() error message = "+IOUtils.toString((InputStream)lResponse.getEntity(), "UTF-8"));
            throw x;
        }finally {
            if(lResponse != null)
                lResponse.close();
        }
    }
    
    public int seedFhirServer(final FhirServerAdminConfig adminConfig) throws InterruptedException, IOException {

        if(adminConfig != null){
            this.hospitalGeneratorCount = adminConfig.getHospitalGeneratorCount();
            this.patientGeneratorCount = adminConfig.getPatientGeneratorCount();
            if(StringUtils.isNotEmpty(adminConfig.getPatientGeneratorCity()))
                this.patientGeneratorCity = adminConfig.getPatientGeneratorCity();
            if(StringUtils.isNotEmpty(adminConfig.getPatientGeneratorState()))
                this.patientGeneratorState = adminConfig.getPatientGeneratorState();
        }

        
        // 1)  Control demographics of population
        Generator.GeneratorOptions options = new Generator.GeneratorOptions();
        options.population = patientGeneratorCount;
        if(!patientGeneratorCity.equals(FhirUtil.OPTIONAL)){
            options.city = this.patientGeneratorCity;
        }
        options.minAge=patientMinAge;
        options.maxAge=patientMaxAge;

        // 2)  Set Common Configuration options as per:  https://github.com/synthetichealth/synthea/wiki/Common-Configuration
        Config.set("exporter.fhir.transaction_bundle", "true");
        Config.set("exporter.fhir.export", "true");
        Config.set("exporter.hospital.fhir.export", "true");
        Config.set("exporter.practitioner.fhir.export", "true");
        Config.set("generate.only_alive_patients", "true");
        
        // 3)  Loop by # of hospitals to generate
        int totalPatientCount=0;
        for(int c=0; c < hospitalGeneratorCount; c++) {

            // 4)  Define a unique directory on the filesystem where output files will be written to
            long randomSeed = ThreadLocalRandom.current().nextLong(100, 100000);
            String outputDir = this.patientGeneratorBaseDir+"/"+randomSeed+"/";
            options.state = this.patientGeneratorState;
            options.seed = randomSeed;
            Config.set("exporter.baseDirectory", outputDir);
            
            Exporter.ExporterRuntimeOptions ero = new Exporter.ExporterRuntimeOptions();
            ero.enableQueue(Exporter.SupportedFhirVersion.R4);
            Generator generator = new Generator(options, ero);
        
            // 5) Run the FHIR resource generator
            ExecutorService generatorService = Executors.newFixedThreadPool(1);
            generatorService.submit(() -> generator.run());

            /* 
             *  5.5)  TO-DO:  Figure out why synthea needs all the patient generated data to be consumed before it will write hospital and patient data to disk
             *                In the meantime, the following is a hack to enumerate through that patient data
             */
            int fhirRecordCount = 0;
            while(fhirRecordCount < patientGeneratorCount) {
                String notUsed = ero.getNextRecord();
                fhirRecordCount++;
            }
            log.info("seedFhirServer() generated the following # of patients: "+patientGeneratorCount+" to output dir = "+outputDir+" at the following location: "+options.city+" "+patientGeneratorState+" .  Will sleep for the following millis: "+sleepMillisAfterFhirGeneration);

            // 6) Sleep for 5 seconds otherwise all files are not actually written to disk
            generatorService.shutdownNow();
            Thread.sleep(sleepMillisAfterFhirGeneration);

            // 7) Determine whether this hospital already exists in the FHIR server
            boolean hospitalAlreadyExistsInFhriServer = determineHospitalExistenceInFhirServer(outputDir);
            if(!hospitalAlreadyExistsInFhriServer) {

                // 8) Seed FHIR Server with Hospital and Practitioner resources
                seedGeneratedResourcesToFhirServer(outputDir);
    
                totalPatientCount = totalPatientCount+patientGeneratorCount;
            }
            
        }
        log.info("seedFhirServer() Total # of patients pushed to fhir server = "+totalPatientCount);
        return totalPatientCount;

    }

    private boolean determineHospitalExistenceInFhirServer(String outputDir) throws IOException {

        FilenameFilter filter = (dir, name) -> name.startsWith(FhirUtil.HOSPITAL_INFORMATION);
        File outputDirFile = new File(outputDir+"fhir");
        File[] hospitalFiles = outputDirFile.listFiles(filter);
        if(hospitalFiles == null || hospitalFiles.length == 0)
            throw new RuntimeException("determineHospitalExistenceInFhirServer() No hospital files at the following directory: "+outputDir);

        File hospitalFile = hospitalFiles[0];
        
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
        Organization hObj = (Organization)bObj.getEntryFirstRep().getResource();
        Location hospital = this.getLocationFromOrganization(hObj);
        if(hospital != null) {
            log.warn(hObj.getName()+" :  This hospital already exists in fhir server. Will not push a duplicate");
            return true;
        } else {
            log.info(hObj.getName()+" :  This hospital does NOT already exists in fhir server. Will seed fhir server");
            return false;
        }

    }
   

    
    // FHIR resource generator outputs hospital and practitioner resources only to the filesystem
    // Thus, this function reads those resources from the filesystem 
    private void seedGeneratedResourcesToFhirServer(String outputDir) throws IOException, InterruptedException {
        File outputDirFile = new File(outputDir+"fhir");
        FilenameFilter filter = (dir, name) -> name.startsWith(FhirUtil.HOSPITAL_INFORMATION);
        if(outputDirFile.exists()){
            File[] hospitalFiles = outputDirFile.listFiles(filter);
            if(hospitalFiles == null || hospitalFiles.length == 0)
                throw new RuntimeException("seedGeneratedResources() No hospital files at the following directory: "+outputDir);
            File hospitalFile = hospitalFiles[0];
            seedHospital(hospitalFile);
            log.info("found hospital file: "+ hospitalFile.getAbsolutePath() +" . Will now sleep for following millis: "+sleepMillisAfterHospitalPost);

            Thread.sleep(sleepMillisAfterHospitalPost);

            File[] files = outputDirFile.listFiles();
            log.trace("The following # of files have been found in "+outputDirFile.getAbsolutePath()+" : "+files.length);
            for(File file : files) {
                if(file.getName().startsWith(FhirUtil.HOSPITAL_INFORMATION)){
                    // Already processed
                }else if (file.getName().startsWith(FhirUtil.PRACTITIONER_INFORMATION)){
                    log.info("found practitioner file: "+ file.getAbsolutePath());
                    seedPractitioner(file);
                }else{
                    seedPatient(file);
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

        // 2) POST Organization
        Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, bundleString);
        Organization pObj = (Organization)bObj.getEntryFirstRep().getResource();
        String pJson = fhirCtx.newJsonParser().encodeResourceToString(pObj);
        Response response = null;
        try {
            response = fhirClient.postOrganization(pJson);
            log.tracev("{0}    seedHospital() fhir server status code: {1}", pObj.getName(), response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("seedHospital() error status = "+response.getStatus()+"  when posting the following Organization to the FhirServer: "+pObj.getId());
            log.error("seedHospital() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }finally {
            if(response != null)
                response.close();
        }

        // 3) POST corresponding Location
        List<BundleEntryComponent> becs = bObj.getEntry();
        Location lObj = (Location) becs.get(1).getResource();
        String lJson = fhirCtx.newJsonParser().encodeResourceToString(lObj);
        response = null;
        try {
            response = fhirClient.postLocation(lJson);
            log.tracev("{0}    seedHospital() fhir server status code when posting location: {1}", lObj.getName(), response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("seedHospital() error status = "+response.getStatus()+"  when posting the following Location to the FhirServer: "+lObj.getId());
            log.error("seedHospital() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }finally {
            if(response != null)
                response.close();
        }

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
        List<BundleEntryComponent> becs = bObj.getEntry();
        int counter = 2;
        for(BundleEntryComponent bec : becs){
        	Practitioner pObj = null;
        	PractitionerRole prObj = null;
            if(counter % 2 == 0) {
                pObj = (Practitioner)bec.getResource();
                String pJson = fhirCtx.newJsonParser().encodeResourceToString(pObj);
                Response response = null;
                try {
                    response = fhirClient.postPractitioner(pJson);
                    log.tracev("{0}    seedPractitioner() fhir server status code: {1}", pObj.getId(), response.getStatus());
                }catch(WebApplicationException x){
                    response = x.getResponse();
                    log.error("seedPractitioner() error status = "+response.getStatus()+"  when posting the following Practitioner to the FhirServer: "+pObj.getId());
                    log.error("seedPractitioner() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
                }finally {
                    if(response != null)
                        response.close();
                }
            }else {
                prObj = (PractitionerRole)bec.getResource();
                String pJson = fhirCtx.newJsonParser().encodeResourceToString(prObj);
                Response response = null;
                try {
                    response = fhirClient.postPractitionerRole(pJson);
                    log.tracev("{0}    seedPractitionerRole() fhir server status code: {1}", prObj.getId(), response.getStatus());
                }catch(WebApplicationException x){
                    response = x.getResponse();
                    log.error("seedPractitionerRole() error status = "+response.getStatus()+"  when posting the following PractitionerRole to the FhirServer: "+prObj.getId());
                    log.error("seedPractitionerRole() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
                }finally {
                    if(response != null)
                        response.close();
                }

            }
            counter++;

        }
    }

    /*
     * NOTE: this function currently only POSTs generated Patient data.
     *       Generated Encounter data (associated with the Patient) is discarded
     */ 
    private void seedPatient(File patientFile) throws IOException {
        InputStream fStream = null;
        String bundleString = null;
        try {
            fStream = new FileInputStream(patientFile);
            bundleString = IOUtils.toString(fStream, "UTF-8");
        }finally {
            if(fStream != null)
              fStream.close();
        }
        Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, bundleString);
        Patient pObj = (Patient)bObj.getEntryFirstRep().getResource();
        String pJson = fhirCtx.newJsonParser().encodeResourceToString(pObj);
        Response response = null;
        try {
            response = fhirClient.postPatient(pJson);
            log.tracev("{0}    seedPatient() fhir server status code: {1}", pObj.getNameFirstRep().getFamily(), response.getStatus());
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("seedPatient() error status = "+response.getStatus()+"  when posting the following Patient to the FhirServer: "+pObj.getId());
            log.error("seedPatient() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();    
    }

}
