package org.acme.vaccinationscheduler.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity(name = "Appointment")
@Table(name = "appointment")
public class AppointmentEntity {

	@Id
	@Column(name = "appointment_id")
	private Long appointmentId;
	@Column(name = "vaccine_type")
    private String vaccineType;
	@Column(name = "vaccination_center_name")
    private String vaccinationCenterName;
	@Column(name = "time_slot_date_time")
    private LocalDateTime timeslotDateTime;
	@Column(name = "person_id")
    private Long personId;
	@Column(name = "person_name")
    private String personName;
	@Column(name = "is_first_dose_administered")
    private Boolean isFirstDoseAdministered;
	@Column(name = "appointment_provider_status")
    private String appointmentProviderStatus;
	@Column(name= "appointment_recipient_status")
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
	public String getVaccineType() {
		return vaccineType;
	}
	public void setVaccineType(String vaccineType) {
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
	public Long getPersonId() {
		return personId;
	}
	public void setPersonId(Long personId) {
		this.personId = personId;
	}
	public String getPersonName() {
		return personName;
	}
	public void setPersonName(String personName) {
		this.personName = personName;
	}

}
