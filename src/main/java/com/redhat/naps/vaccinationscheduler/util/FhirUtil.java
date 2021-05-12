package com.redhat.naps.vaccinationscheduler.util;

public class FhirUtil {

    public static final String SEED_FHIR_SERVER_AT_STARTUP="com.redhat.naps.vaccinationscheduler.seed.fhir.server.at.startup";
    public static final String SLEEP_MILLIS_AFTER_FHIR_GENERATION="com.redhat.naps.vaccinationscheduler.sleep.millis.after.fhir.generation";
    public static final String HOSPITAL_GENERATOR_COUNT = "com.redhat.naps.vaccinationscheduler.hospitalGeneratorCount";
    public static final String PATIENT_GENERATOR_COUNT="com.redhat.naps.vaccinationscheduler.patientGeneratorCount";
    public static final String PATIENT_GENERATOR_STATE="com.redhat.naps.vaccinationscheduler.patientGeneratorState";
    public static final String PATIENT_GENERATOR_CITY="com.redhat.naps.vaccinationscheduler.patientGeneratorCity";
    public static final String PATIENT_GENERATOR_BASE_DIR="com.redhat.naps.vaccinationscheduler.patientGeneratorBaseDir";
    public static final String TIMESLOTDURATION_MINUTES="com.redhat.naps.vaccinationscheduler.timeslotduration.minutes";

    public static final String HOSPITAL_INFORMATION="hospitalInformation";
    public static final String PRACTITIONER_INFORMATION="practitionerInformation";
    public static final String PATIENT_INFORMATION="patientInformation";
    public static final String OBSERVATION_INFORMATION = "observationInformation";
    public static final String SCHEDULE_INFORMATION = "scheduleInformation";
    public static final String APPOINTMENT_INFORMATION = "appointmentInformation";

    public static final String FIRST_DOSE_ADMINISTERED = "firstDoseAdministered";
    public static final String SECOND_SHOT_IDEAL_DATE = "secondShotIdealDate";

    public static final String TEST_INTERACT_WITH_FHIRSERVER = "com.redhat.naps.vaccinationscheduler.test.interact_with_fhirserver";

    public static final String PATIENT_ADDRESS_EXTENSION = "http://hl7.org/fhir/StructureDefinition/geolocation";

    public static final String URL="url";
    public static final String VALUE_DECIMAL="valueDecimal";
    public static final String LONGITUDE="longitude";
    public static final String LATITUDE="latitude";
    public static final String OPTIONAL="optional";

    public static final String SYNTHEA_SYSTEM_IDENTIFIER="https://github.com/synthetichealth/synthea";
    
}
