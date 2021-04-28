/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.naps.vaccinationscheduler.persistence;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.naps.vaccinationscheduler.domain.VaccinationSchedule;
import com.redhat.naps.vaccinationscheduler.mapping.AppointmentMapper;
import com.redhat.naps.vaccinationscheduler.service.AppointmentService;

@ApplicationScoped
public class VaccinationScheduleRepository {
    
    @Inject
    AppointmentService apptService;
    @Inject
    AppointmentMapper apptMapper;

    private VaccinationSchedule vaccinationSchedule;
    
    private static final Logger LOG = Logger.getLogger(VaccinationScheduleRepository.class);

    public VaccinationSchedule find() {
        return vaccinationSchedule;
    }

    public void save(VaccinationSchedule vaccinationSchedule) {
        this.vaccinationSchedule = vaccinationSchedule;         
    }
    
    public void persist(VaccinationSchedule vaccinationSchedule) {
        /*
        if(this.vaccinationSchedule!=null && !this.vaccinationSchedule.getInjectionList().isEmpty()) {
            List<Injection> injections = vaccinationSchedule.getInjectionList();
            LOG.info("Persisting injection list of size: "+injections.size());
            for(Injection i : injections) { 
                if(i!=null && i.getPerson()!=null && i.getId()!=null) {
                    LOG.trace("Persisting appointment for injection with id: "+i.getId());
                    apptService.saveOrUpdate(apptMapper.fromInjection(i));
                }
            }
        } */
    }
    
    public void handleException(Long l, Throwable t) {
        LOG.error("Error during solver execution: "+t.getMessage());
    }

}
