package org.acme.vaccinationscheduler.domain;

import java.time.LocalDateTime;

public class Appointment {

    private VaccineType vType;
    private VaccinationCenter vCenter;
    private LocalDateTime timeslotDateTime;
    private Person person;
    private VaccinationStatus vStatus;
    private AppointmentStatus aStatus;

    public VaccineType getvType() {
        return vType;
    }
    public void setvType(VaccineType vType) {
        this.vType = vType;
    }
    public VaccinationCenter getvCenter() {
        return vCenter;
    }
    public void setvCenter(VaccinationCenter vCenter) {
        this.vCenter = vCenter;
    }
    public LocalDateTime getTimeslotDateTime() {
        return timeslotDateTime;
    }
    public void setTimeslotDateTime(LocalDateTime timeslotDateTime) {
        this.timeslotDateTime = timeslotDateTime;
    }
    public Person getPerson() {
        return person;
    }
    public void setPerson(Person person) {
        this.person = person;
    }
	public VaccinationStatus getvStatus() {
		return vStatus;
	}
	public void setvStatus(VaccinationStatus vStatus) {
		this.vStatus = vStatus;
	}
	public AppointmentStatus getaStatus() {
		return aStatus;
	}
	public void setaStatus(AppointmentStatus aStatus) {
		this.aStatus = aStatus;
	}
}
