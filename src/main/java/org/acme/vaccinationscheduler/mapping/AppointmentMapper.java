package org.acme.vaccinationscheduler.mapping;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.Injection;
import org.acme.vaccinationscheduler.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = QuarkusMappingConfig.class)
public interface AppointmentMapper {
	
	
	public AppointmentEntity fromAppointment(Appointment appointment);
	
	@Mapping(target="appointmentId", source="id")
	@Mapping(target="vaccinationCenterName", source="vaccinationCenter.name")
	@Mapping(target="timeslotDateTime", source="dateTime")
	@Mapping(target="personId", source="person.id")
	@Mapping(target="personName", source="person.name")
	@Mapping(target="isFirstDoseAdministered", source="person.firstShotInjected")
	public Appointment fromInjection(Injection injection);
	
	public Appointment toDomain(AppointmentEntity appointment);
	
	public AppointmentEntity toEntity(Appointment appointment);
}
