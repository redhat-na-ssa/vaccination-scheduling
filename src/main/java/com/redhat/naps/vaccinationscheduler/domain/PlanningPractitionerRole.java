package com.redhat.naps.vaccinationscheduler.domain;

public class PlanningPractitionerRole {
	
	private String practitionerId;
	private String practitionerName;
	private String vaccinationCenterId;
	private String vaccinationCenterName;
	
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

}
