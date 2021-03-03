package org.acme.vaccinationscheduler.persistence;

import javax.enterprise.context.ApplicationScoped;

import org.acme.vaccinationscheduler.entity.AppointmentEntity;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepositoryBase<AppointmentEntity, Long> {

}
