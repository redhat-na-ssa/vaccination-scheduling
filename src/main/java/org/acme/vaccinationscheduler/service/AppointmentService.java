package org.acme.vaccinationscheduler.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.acme.vaccinationscheduler.mapping.AppointmentMapper;
import org.acme.vaccinationscheduler.persistence.AppointmentRepository;
import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.entity.AppointmentEntity;
import org.acme.vaccinationscheduler.exception.ServiceException;

@ApplicationScoped
public class AppointmentService {
	
	private AppointmentRepository apptRepository;
	private AppointmentMapper apptMapper;
	
	public AppointmentService(AppointmentRepository apptRepository, AppointmentMapper apptMapper) {
		this.apptRepository = apptRepository;
		this.apptMapper = apptMapper;
	}
	
	public List<Appointment> findAll() {
		return apptRepository.findAll().stream()
				.map(apptMapper::toDomain)
				.collect(Collectors.toList());
	}
	
	public Optional<Appointment> findById(Long appointmentId) {
		return apptRepository.findByIdOptional(appointmentId).map(apptMapper::toDomain);
	}
	
	public List<Appointment> findByVaccinationCenter(String vaccinationCenter) {
		return apptRepository.findByVaccinationCenter(vaccinationCenter)
				.stream().map(apptMapper::toDomain)
				.collect(Collectors.toList());
	}
	
	public List<Appointment> findByPersonId(Long personId) {
		return apptRepository.findByPersonId(personId)
				.stream().map(apptMapper::toDomain)
				.collect(Collectors.toList());
	}
	
	@Transactional
	public Appointment save(Appointment appointment) {
		AppointmentEntity entity = apptMapper.toEntity(appointment);
		apptRepository.persist(entity);
		return apptMapper.toDomain(entity);
	}
	
	@Transactional
	public Appointment update(Appointment appointment) {
		if (appointment.getAppointmentId() == null) {
			throw new ServiceException("Appointment does not have an appointmentId");
		}
		Optional<AppointmentEntity> optional = apptRepository.findByIdOptional(appointment.getAppointmentId());
		if (optional.isEmpty()) {
			throw new ServiceException(String.format("No appointment found for appointmentId[%s]", appointment.getAppointmentId()));
		}
		AppointmentEntity entity = optional.get();
		entity.setAppointmentStatus(appointment.getAppointmentStatus()==null?null:appointment.getAppointmentStatus().toString());
		entity.setIsFirstDoseAdministered(appointment.getIsFirstDoseAdministered());
		entity.setPersonId(appointment.getPersonId());
		entity.setPersonName(appointment.getPersonName());
		entity.setTimeslotDateTime(appointment.getTimeslotDateTime());
		entity.setVaccinationCenterName(appointment.getVaccinationCenterName());
		entity.setVaccineType(appointment.getVaccineType()==null?null:appointment.getVaccineType().toString());
		apptRepository.persist(entity);
		return apptMapper.toDomain(entity);
	}
	
	@Transactional
	public Appointment updateNonNull(Appointment appointment) {
		if (appointment.getAppointmentId() == null) {
			throw new ServiceException("Appointment does not have an appointmentId");
		}
		Optional<AppointmentEntity> optional = apptRepository.findByIdOptional(appointment.getAppointmentId());
		if (optional.isEmpty()) {
			throw new ServiceException(String.format("No appointment found for appointmentId[%s]", appointment.getAppointmentId()));
		}
		AppointmentEntity entity = optional.get();
		if(appointment.getAppointmentStatus()!=null)
			entity.setAppointmentStatus(appointment.getAppointmentStatus().toString());
		if(appointment.getIsFirstDoseAdministered()!=null)
			entity.setIsFirstDoseAdministered(appointment.getIsFirstDoseAdministered());
		if(appointment.getPersonId()!=null)
			entity.setPersonId(appointment.getPersonId());
		if(appointment.getPersonName()!=null)
			entity.setPersonName(appointment.getPersonName());
		if(appointment.getTimeslotDateTime()!=null)
			entity.setTimeslotDateTime(appointment.getTimeslotDateTime());
		if(appointment.getVaccinationCenterName()!=null)
			entity.setVaccinationCenterName(appointment.getVaccinationCenterName());
		if(appointment.getVaccineType()!=null)
			entity.setVaccineType(appointment.getVaccineType().toString());
		apptRepository.persist(entity);
		return apptMapper.toDomain(entity);
	}
	
	@Transactional
	public Appointment saveOrUpdate(Appointment appointment) {
		if (appointment.getAppointmentId() == null) {
			throw new ServiceException("Appointment does not have an appointmentId");
		}
		Optional<AppointmentEntity> optional = apptRepository.findByIdOptional(appointment.getAppointmentId());
		if (optional.isEmpty()) {
			return save(appointment);
		} else {
			return update(appointment);
		}
	}

	@Transactional
	public Appointment saveOrUpdateNonNull(Appointment appointment) {
		if (appointment.getAppointmentId() == null) {
			throw new ServiceException("Appointment does not have an appointmentId");
		}
		Optional<AppointmentEntity> optional = apptRepository.findByIdOptional(appointment.getAppointmentId());
		if (optional.isEmpty()) {
			return save(appointment);
		} else {
			return updateNonNull(appointment);
		}
	}

}
