package org.acme.vaccinationscheduler.bootstrap;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;

import org.acme.vaccinationscheduler.util.FhirUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import org.mitre.synthea.engine.Generator;
import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.export.Exporter;

@ApplicationScoped
public class FhirDataGenerator {

    private static final String GENERATE_PATIENTS="com.redhat.vaccination.scheduling.generatePatients";
    private static final String PATIENT_GENERATOR_COUNT="com.redhat.vaccination.scheduling.patientGeneratorCount";
    private static final String PATIENT_GENERATOR_STATE="com.redhat.vaccination.scheduling.patientGeneratorState";
    private static final String PATIENT_GENERATOR_CITY="com.redhat.vaccination.scheduling.patientGeneratorCity";
    private static final String PATIENT_GENERATOR_BASE_DIR="com.redhat.vaccination.scheduling.patientGeneratorBaseDir";


    @Inject
    @ConfigProperty(name = GENERATE_PATIENTS, defaultValue = "false")
    boolean generatePatients;

    @Inject
    @ConfigProperty(name = PATIENT_GENERATOR_COUNT, defaultValue = "50")
    int patientGeneratorCount;

    @Inject
    @ConfigProperty(name = PATIENT_GENERATOR_STATE, defaultValue = "Michigan")
    String patientGeneratorState;

    @Inject
    @ConfigProperty(name = PATIENT_GENERATOR_CITY, defaultValue = "Detroit")
    String patientGeneratorCity;

    @Inject
    @ConfigProperty(name = PATIENT_GENERATOR_BASE_DIR, defaultValue = "/tmp")
    String patientGeneratorBaseDir;

    private static Logger log = Logger.getLogger(FhirDataGenerator.class);

    public void onStart(@Observes @Priority(value = 1) StartupEvent ev) throws InterruptedException {
        if(!generatePatients){
            log.info("onStart() .... will not generate patients");
            return;
        }
        long randomSeed = ThreadLocalRandom.current().nextLong(100, 100000);
        String outputDir = this.patientGeneratorBaseDir+"/"+randomSeed+"/";
        log.info("onStart() .... will generate the following # of patients: "+this.patientGeneratorCount+" to output dir = "+outputDir);


        // Control demographics of the population
        Generator.GeneratorOptions options = new Generator.GeneratorOptions();
        options.population = this.patientGeneratorCount;
        options.city = this.patientGeneratorCity;
        options.state = this.patientGeneratorState;
        options.seed = randomSeed;

        // Set Common Configuration options as per:  https://github.com/synthetichealth/synthea/wiki/Common-Configuration
        Config.set("exporter.baseDirectory", outputDir);
        Config.set("exporter.fhir.transaction_bundle", "true");
        Config.set("exporter.fhir.export", "true");
        Config.set("exporter.hospital.fhir.export", "true");
        Config.set("exporter.practitioner.fhir.export", "true");
        Config.set("generate.only_live_patients", "true");

        Exporter.ExporterRuntimeOptions ero = new Exporter.ExporterRuntimeOptions();
        ero.enableQueue(Exporter.SupportedFhirVersion.R4);
        Generator generator = new Generator(options, ero);

        ExecutorService generatorService = Executors.newFixedThreadPool(1);
        generatorService.submit(() -> generator.run());

        int fhirRecordCount = 0;
        while(fhirRecordCount < patientGeneratorCount) {
            String jsonRecord = ero.getNextRecord();
              
            fhirRecordCount++;
            //log.info("\n\n\njsonRecord = "+jsonRecord);
            log.info("record count = "+fhirRecordCount);
        }
        generatorService.shutdownNow();
        log.info("onStart() Total # of fhir records = "+fhirRecordCount);

        Thread.sleep(5000);
        File outputDirFile = new File(outputDir+"fhir");
        if(outputDirFile.exists()){
            File[] files = outputDirFile.listFiles();
            log.info("The following # of files have been found in "+outputDirFile.getAbsolutePath()+" : "+files.length);
            for(File file : files) {
                if(file.getName().startsWith(FhirUtil.HOSPITAL_INFORMATION)){
                    log.info("found hospital file: "+ file.getAbsolutePath());
                }else if (file.getName().startsWith(FhirUtil.PRACTITIONER_INFORMATION)){
                    log.info("found practitioner file: "+ file.getAbsolutePath());
                }else{
                    log.info("skipping the following file: "+file.getAbsolutePath());
                }
            }
        }else {
            log.error("The following directory does not exist: "+outputDirFile.getAbsolutePath());
        }
    }

}