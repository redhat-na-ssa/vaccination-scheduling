package com.redhat.naps.vaccinationscheduler.domain;

public class FhirServerAdminConfig {

    private int patientGeneratorCount = 1;
    private String patientGeneratorState;
    private String patientGeneratorCity;

    public FhirServerAdminConfig() {}    

    public FhirServerAdminConfig(int patientGeneratorCount, String patientGeneratorState, String patientGeneratorCity) {
        this.patientGeneratorCount = patientGeneratorCount;
        this.patientGeneratorState = patientGeneratorState;
        this.patientGeneratorCity = patientGeneratorCity;
    }

    public int getPatientGeneratorCount() {
        return patientGeneratorCount;
    }
    public void setPatientGeneratorCount(int patientGeneratorCount) {
        this.patientGeneratorCount = patientGeneratorCount;
    }
    public String getPatientGeneratorState() {
        return patientGeneratorState;
    }
    public void setPatientGeneratorState(String patientGeneratorState) {
        this.patientGeneratorState = patientGeneratorState;
    }
    public String getPatientGeneratorCity() {
        return patientGeneratorCity;
    }
    public void setPatientGeneratorCity(String patientGeneratorCity) {
        this.patientGeneratorCity = patientGeneratorCity;
    }

    
    
}
