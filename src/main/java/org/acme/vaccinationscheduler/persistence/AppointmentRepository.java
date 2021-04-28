package org.acme.vaccinationscheduler.persistence;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.acme.vaccinationscheduler.entity.AppointmentEntity;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepositoryBase<AppointmentEntity, Long> {
    public List<AppointmentEntity> findByVaccinationCenter(String vaccinationCenter) {
        return find("vaccination_center_name", vaccinationCenter).list();
    }

    public List<AppointmentEntity> findByPersonId(Long personId) {
        return find("person_id", personId).list();
    }

}
