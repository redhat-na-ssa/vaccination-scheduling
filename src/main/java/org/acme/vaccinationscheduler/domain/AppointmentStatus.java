package org.acme.vaccinationscheduler.domain;

public enum AppointmentStatus {
    PROPOSED,
    SCHEDULED,
    STANDBY,
    CONFIRMED,
    CANCELLED_PROVIDER_SUPPLY,
    CANCELLED_NO_SHOW,
    COMPLETED

}
