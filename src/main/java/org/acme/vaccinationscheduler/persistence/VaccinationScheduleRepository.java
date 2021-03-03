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

package org.acme.vaccinationscheduler.persistence;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.vaccinationscheduler.domain.Injection;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.mapping.AppointmentMapper;
import org.acme.vaccinationscheduler.service.AppointmentService;

@ApplicationScoped
public class VaccinationScheduleRepository {
	
    @Inject
    AppointmentService apptService;
    @Inject
    AppointmentMapper apptMapper;

    private VaccinationSchedule vaccinationSchedule;

    public VaccinationSchedule find() {
        return vaccinationSchedule;
    }

    public void save(VaccinationSchedule vaccinationSchedule) {
        this.vaccinationSchedule = vaccinationSchedule;
        //VaccinationSchedule sched = vaccinationScheduleRepository.find();
		
		  List<Injection> injections = vaccinationSchedule.getInjectionList();
		  for(Injection i : injections) { 
			  if(i!=null && i.getPerson()!=null && i.getId()!=null) { 
				  apptService.saveOrUpdate(apptMapper.fromInjection(i));
			  }
		  }
		 
    }

}
