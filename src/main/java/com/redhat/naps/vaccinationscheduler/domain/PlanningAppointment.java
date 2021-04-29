package com.redhat.naps.vaccinationscheduler.domain;

import java.time.LocalDateTime;
import org.hl7.fhir.r4.model.Appointment;

public class PlanningAppointment {

    private Long appointmentId;
    private VaccineType vaccineType;
    private String vaccinationCenterName;
    private LocalDateTime timeslotDateTime;
    private String personId;
    private String personName;
    private Boolean isFirstDoseAdministered = false;
    private String appointmentProviderStatus;
    private String appointmentRecipientStatus;
    
    public String getAppointmentProviderStatus() {
        return appointmentProviderStatus;
    }
    public void setAppointmentProviderStatus(String appointmentProviderStatus) {
        this.appointmentProviderStatus = appointmentProviderStatus;
    }
    public String getAppointmentRecipientStatus() {
        return appointmentRecipientStatus;
    }
    public void setAppointmentRecipientStatus(String appointmentRecipientStatus) {
        this.appointmentRecipientStatus = appointmentRecipientStatus;
    }
    public VaccineType getVaccineType() {
        return vaccineType;
    }
    public void setVaccineType(VaccineType vaccineType) {
        this.vaccineType = vaccineType;
    }
    public Boolean getIsFirstDoseAdministered() {
        return isFirstDoseAdministered;
    }
    public void setIsFirstDoseAdministered(Boolean isFirstDoseAdministered) {
        this.isFirstDoseAdministered = isFirstDoseAdministered;
    }
    public Long getAppointmentId() {
        return appointmentId;
    }
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    public String getVaccinationCenterName() {
        return vaccinationCenterName;
    }
    public void setVaccinationCenterName(String vaccinationCenterName) {
        this.vaccinationCenterName = vaccinationCenterName;
    }
    public LocalDateTime getTimeslotDateTime() {
        return timeslotDateTime;
    }
    public void setTimeslotDateTime(LocalDateTime timeslotDateTime) {
        this.timeslotDateTime = timeslotDateTime;
    }
    public String getPersonId() {
        return personId;
    }
    public void setPersonId(String personId) {
        this.personId = personId;
    }
    public String getPersonName() {
        return personName;
    }
    public void setPersonName(String personName) {
        this.personName = personName;
    }
    @Override
    public String toString() {
        return "PlanningAppointment [appointmentId=" + appointmentId + ", appointmentProviderStatus="
                + appointmentProviderStatus + ", appointmentRecipientStatus=" + appointmentRecipientStatus
                + ", isFirstDoseAdministered=" + isFirstDoseAdministered + ", personId=" + personId + ", personName="
                + personName + ", timeslotDateTime=" + timeslotDateTime + ", vaccinationCenterName="
                + vaccinationCenterName + ", vaccineType=" + vaccineType + "]";
    }


}
