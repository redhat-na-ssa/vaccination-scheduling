package com.redhat.naps.vaccinationscheduler.domain;

public class PlanningPractitionerRole {
    
    private String practitionerId;
    private String practitionerName;
    private String vaccinationCenterId;
    private String vaccinationCenterName;
    
    public PlanningPractitionerRole() {
        super();
        this.practitionerId = null;
        this.practitionerName = null;
        this.vaccinationCenterId = null;
        this.vaccinationCenterName = null;        
    }
    
    public PlanningPractitionerRole(String practitionerId, String practitionerName, String vaccinationCenterId,
            String vaccinationCenterName) {
        super();
        this.practitionerId = practitionerId;
        this.practitionerName = practitionerName;
        this.vaccinationCenterId = vaccinationCenterId;
        this.vaccinationCenterName = vaccinationCenterName;
    }
    public String getPractitionerId() {
        return practitionerId;
    }
    public void setPractitionerId(String practitionerId) {
        this.practitionerId = practitionerId;
    }
    public String getPractitionerName() {
        return practitionerName;
    }
    public void setPractitionerName(String practitionerName) {
        this.practitionerName = practitionerName;
    }
    public String getVaccinationCenterId() {
        return vaccinationCenterId;
    }
    public void setVaccinationCenterId(String vaccinationCenterId) {
        this.vaccinationCenterId = vaccinationCenterId;
    }
    public String getVaccinationCenterName() {
        return vaccinationCenterName;
    }
    public void setVaccinationCenterName(String vaccinationCenterName) {
        this.vaccinationCenterName = vaccinationCenterName;
    }

    @Override
    public String toString() {
        return "PlanningPractitionerRole [practitionerId=" + practitionerId + ", practitionerName=" + practitionerName
                + ", vaccinationCenterId=" + vaccinationCenterId + ", vaccinationCenterName=" + vaccinationCenterName
                + "]";
    }

}
