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

package com.redhat.naps.vaccinationscheduler;

import java.io.IOException;
import java.io.InputStream;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.github.javaparser.utils.Log;
import com.redhat.naps.vaccinationscheduler.domain.PlanningInjection;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPerson;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPractitioner;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPractitionerRole;
import com.redhat.naps.vaccinationscheduler.domain.PlanningVaccinationCenter;
import com.redhat.naps.vaccinationscheduler.domain.VaccinationSchedule;
import com.redhat.naps.vaccinationscheduler.domain.VaccineType;
import com.redhat.naps.vaccinationscheduler.mapping.FhirMapper;
import com.redhat.naps.vaccinationscheduler.rest.FhirServerClient;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

@ApplicationScoped
public class VaccineSchedulingService {

    private static Logger log = Logger.getLogger(VaccineSchedulingService.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    @RestClient
    FhirServerClient fhirClient;

    @Inject
    FhirMapper fhirMapper;

    @Inject
    FhirServerAdminService fhirServerService;

    private VaccinationSchedule vSchedule;
    
    public VaccinationSchedule refreshVaccinationSchedule() throws IOException {

        synchronized(fhirClient) {

            // Retrieve all vaccination center data from FHIR Server
            List<PlanningVaccinationCenter> vaccinationCenterList = new ArrayList<>();
            Response gResponse = null;
            try {
    
                // 1)  Pull all Organizations
                gResponse = fhirClient.getOrganizations();
                String orgJson = IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8");
                Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
                List<BundleEntryComponent> becs = bObj.getEntry();
                log.info("refreshVaccinationSchedule() # of Organizations = "+becs.size());
                for(BundleEntryComponent bec : becs) {
                    Organization org = (Organization)bec.getResource();
                    
                    if(log.isDebugEnabled())
                    	log.debug("!!!!! Organization name: "+org.getName()+" identifier: "+org.getIdentifier().get(0).getValue());
                    
                    // 2)  For each Organization, grab corresponding Location
                    Location lObj = fhirServerService.getLocationFromOrganization(org);
                    if(lObj != null) {
                        PlanningVaccinationCenter pvc = fhirMapper.fromFhirOrganizationToPlanningVaccinationCenter(org, lObj);
                        vaccinationCenterList.add(pvc);
                    }else {
                        log.warn("refreshVaccinationSchedule() will not consider the following hospital since it does not include a Location : "
                        		+org.getName());
                    }
                }
    
            }catch(WebApplicationException x) {
                gResponse = x.getResponse();
                log.error("refreshVaccinationSchedule() error status = "+gResponse.getStatus()+"  when getting hospital from FhirServer");
                log.error("refreshVaccinationSchedule() error meesage = "+IOUtils.toString((InputStream)gResponse.getEntity(), "UTF-8"));
                throw x;
            }finally {
                if(gResponse != null)
                    gResponse.close();
            }


            // 2 Retrieve all PractionerRole resources from FHIR server
            List<PlanningPractitionerRole> pRoleList = new ArrayList<>();  
            Response pResponse = null;
            try {
                pResponse = fhirClient.getPractionerRoles();
                String orgJson = IOUtils.toString((InputStream)pResponse.getEntity(), "UTF-8");
                Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
                List<BundleEntryComponent> becs = bObj.getEntry();
                log.info("refreshVaccinationSchedule() # of PractionerRole resources = "+becs.size());
                for(BundleEntryComponent bec : becs) {
                    PractitionerRole pRole = (PractitionerRole)bec.getResource();
                    if(log.isDebugEnabled()) {
	                    log.trace("Practitioner role: "+ToStringBuilder.reflectionToString(pRole)+"\n");
	                    log.trace("***** Practitioner Role Practitioner: "+ToStringBuilder.reflectionToString(pRole.getPractitioner())+"\n");
	                    log.trace("##### Practitioner Role Organization: "+ToStringBuilder.reflectionToString(pRole.getOrganization())+"\n");
	                    log.debug("$$$$$ Organization name: "+pRole.getOrganization().getDisplay()+" Identifier: "+
	                    		pRole.getOrganization().getIdentifier().getValue()+"\n");
	                    log.debug("***** Practitioner name: "+pRole.getPractitioner().getDisplay()+" Identifier: "+
	                    		pRole.getPractitioner().getIdentifier().getValue()+"\n");
                    }
            		PlanningPractitionerRole role = fhirMapper.fromFhirPractitionerRoleToPlanningPractitionerRole(pRole);
                    pRoleList.add(role);

                }
    
            }catch(WebApplicationException x) {
                pResponse = x.getResponse();
                log.error("refreshVaccinationSchedule() error status = "+pResponse.getStatus()+"  when getting practitioner roles from FhirServer");
                log.error("refreshVaccinationSchedule() error meesage = "+IOUtils.toString((InputStream)pResponse.getEntity(), "UTF-8"));
                throw x;
            }finally{
                pResponse.close();
            }
    
            
    
            // 3) Retrieve all patients from FHIR server
            List<PlanningPerson> personList = new ArrayList<>();
            pResponse = null;
            try {
                pResponse = fhirClient.getPatients();
                String orgJson = IOUtils.toString((InputStream)pResponse.getEntity(), "UTF-8");
                Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, orgJson);
                List<BundleEntryComponent> becs = bObj.getEntry();
                log.info("refreshVaccinationSchedule() # of Patients = "+becs.size());
                for(BundleEntryComponent bec : becs) {
                    Patient org = (Patient)bec.getResource();
                    PlanningPerson pvc = fhirMapper.fromFhirPatientToPlanningPerson(org);
                    personList.add(pvc);
                }
    
            }catch(WebApplicationException x) {
                pResponse = x.getResponse();
                log.error("refreshVaccinationSchedule() error status = "+pResponse.getStatus()+"  when getting patients from FhirServer");
                log.error("refreshVaccinationSchedule() error meesage = "+IOUtils.toString((InputStream)pResponse.getEntity(), "UTF-8"));
                throw x;
            }finally{
                pResponse.close();
            }
            
            // Retrieve all practitioners from FHIR server
            List<PlanningPractitioner> practitionerList = new ArrayList<>();
            Response prResponse = null;
            try {
                prResponse = fhirClient.getPractitioners();
                String prJson = IOUtils.toString((InputStream)prResponse.getEntity(), "UTF-8");
                Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, prJson);
                List<BundleEntryComponent> becs = bObj.getEntry();
                log.info("refreshVaccinationSchedule() # of Practitioners = "+becs.size());
                for(BundleEntryComponent bec : becs) {
                	Practitioner pr = (Practitioner)bec.getResource();
                	if(log.isDebugEnabled())
                		log.debug("Practitioner identifier: "+pr.getIdentifier().get(0).getValue());
                	PlanningPractitioner ppr = fhirMapper.fromFhirPractitionerToPlanningPractitioner(pr);
                	practitionerList.add(ppr);
                }

            }catch(WebApplicationException x) {
                prResponse = x.getResponse();
                log.error("refreshVaccinationSchedule() error status = "+prResponse.getStatus()+"  when getting practitioners from FhirServer");
                log.error("refreshVaccinationSchedule() error meesage = "+IOUtils.toString((InputStream)prResponse.getEntity(), "UTF-8"));
                throw x;
            }finally{
                prResponse.close();
            }
            
    
            // TO-DO:  As per FIHR R4 Immunization.manufacturer,  maybe this should be implemented as a list of FHIR Organizations ?
            // https://issues.redhat.com/browse/NAPSSS-87
            List<VaccineType> vaccineTypeList = List.of(VaccineType.values());
    
    
            // TO-DO:  Seems as of this should be made configurable
            LocalDate windowStartDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            int windowDaysLength = 5;
            LocalTime dayStartTime = LocalTime.of(9, 0);
            int injectionDurationInMinutes = 30;
            int injectionsPerLinePerDay = (int) (MINUTES.between(dayStartTime, LocalTime.of(17, 0)) / injectionDurationInMinutes);
    
    
            // TO-DO:  https://issues.redhat.com/browse/NAPSSS-84
            int lineTotal = vaccinationCenterList.stream().mapToInt(PlanningVaccinationCenter::getLineCount).sum();
            List<LocalDateTime> timeslotDateTimeList = new ArrayList<>(windowDaysLength * injectionsPerLinePerDay);
            for (int dayIndex = 0; dayIndex < windowDaysLength; dayIndex++) {
                LocalDate date = windowStartDate.plusDays(dayIndex);
                for (int timeIndex = 0; timeIndex < injectionsPerLinePerDay; timeIndex++) {
                    LocalTime time = dayStartTime.plusMinutes(injectionDurationInMinutes * timeIndex);
                    timeslotDateTimeList.add(LocalDateTime.of(date, time));
                }
            }
            
    
            // TO-DO:  What is the FHIR equivalent of injection supply at each vaccination center ?
            // https://issues.redhat.com/browse/NAPSSS-86
            Random random = new Random(17);
            List<PlanningInjection> injectionList = new ArrayList<PlanningInjection>();
            long injectionId = 0L;
            for (PlanningVaccinationCenter vaccinationCenter : vaccinationCenterList) {
                for(PlanningPractitionerRole ppr : pRoleList) {
                	PlanningInjection pi = null;
                	String planningId = ppr.getVaccinationCenterId();
                	String fhirId = vaccinationCenter.getId();
                	// Check whether practitioner is associated with the vaccination center
                	if(StringUtils.equals(planningId, fhirId)) {
                    	log.info("Generating time slots for Vaccination Center: "+ppr.getVaccinationCenterName()+
                    			" and Practitioner: "+ppr.getPractitionerName()+" planning or Id: "+planningId+" equals fhir Id : "+fhirId);
			            for (int dayIndex = 0; dayIndex < windowDaysLength; dayIndex++) {
			                LocalDate date = windowStartDate.plusDays(dayIndex);
			                for (int lineIndex = 0; lineIndex < vaccinationCenter.getLineCount(); lineIndex++) {
			                    VaccineType vaccineType = pickVaccineType(random);
			                    for (int timeIndex = 0; timeIndex < injectionsPerLinePerDay; timeIndex++) {
			                        LocalTime time = dayStartTime.plusMinutes(injectionDurationInMinutes * timeIndex);
				                            pi = new PlanningInjection(
				                                injectionId++, vaccinationCenter, ppr, lineIndex,
				                                LocalDateTime.of(date, time), vaccineType);
				                            if(log.isDebugEnabled())
				                            	log.debug("^^^^^ Planning Injection: "+ReflectionToStringBuilder.toString(pi));
				                            injectionList.add(pi);
	                        	}
	                        }
	                    }
                        
                    }
                }
            }
            log.info("Planning injections created: "+injectionList.size());
            vSchedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList, timeslotDateTimeList, personList, injectionList);
            return vSchedule;
        }

    }
    

    public VaccinationSchedule getVaccinationSchedule() {

        try {
            if(vSchedule == null)
                this.refreshVaccinationSchedule();
        }catch(IOException x){
            throw new RuntimeException(x);
        }
            
        return vSchedule;
    }

        
    /*
        Given a new VaccinationSchedule, post Appointments to FHIR Server

        TO-DO:  Probably best to place a lock on this function
    */
    public void saveVaccinationSchedule(VaccinationSchedule vSchedule) {

        this.vSchedule = vSchedule;
        log.info("saveVaccinationSchedule() solver status = "+vSchedule.getSolverStatus().name());

        
        if(this.vSchedule!=null && !this.vSchedule.getInjectionList().isEmpty()) {
            List<PlanningInjection> injections = vSchedule.getInjectionList();
            log.info("saveVaccinationSchedule() Persisting injection list of size: "+injections.size());
            try {
                for(PlanningInjection i : injections) { 
                    if(i!=null && i.getPerson()!=null && i.getId()!=null) {
                        log.trace("Persisting appointment for injection with id: "+i.getId());
    
                        Appointment aObj = fhirMapper.fromPlanningInjectionToFhirAppointment(i);
                        Response response = null;
                        try {
                            String aJson = fhirCtx.newJsonParser().encodeResourceToString(aObj);
                            log.trace("appointment = \n\n"+aJson+"\n\n");
                            response = fhirClient.postAppointment(aJson);
                        }catch(WebApplicationException x){
                            response = x.getResponse();
                            log.error("saveVaccinationSchedule() error status = "+response.getStatus()+"  when posting Appointment to FhirServer: "+aObj.getId());
                            log.error("saveVaccinationSchedule() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
                            throw x;
                        }finally {
                            if(response != null)
                              response.close();
                        }
                        
                    }else {
                        throw new RuntimeException("saveVaccinationSchedule() unknown Injection "+i);
                    }
                }
            }catch(Exception x) {
                throw new RuntimeException(x);
            }
        }
    }
    
    public void handleException(Long l, Throwable t) {
        t.printStackTrace();
    }


    public VaccineType pickVaccineType(Random random) {
        double randomDouble = random.nextDouble();
        if (randomDouble < 0.50) {
            return VaccineType.PFIZER;
        } else if (randomDouble < 0.90) {
            return VaccineType.MODERNA;
        } else {
            return VaccineType.ASTRAZENECA;
        }
    }

}
