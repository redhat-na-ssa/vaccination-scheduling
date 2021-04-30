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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.YEARS;

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
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.redhat.naps.vaccinationscheduler.domain.Injection;
import com.redhat.naps.vaccinationscheduler.domain.PlanningLocation;
import com.redhat.naps.vaccinationscheduler.domain.PlanningPerson;
import com.redhat.naps.vaccinationscheduler.domain.PlanningVaccinationCenter;
import com.redhat.naps.vaccinationscheduler.domain.VaccinationSchedule;
import com.redhat.naps.vaccinationscheduler.domain.VaccineType;
import com.redhat.naps.vaccinationscheduler.rest.FhirServerClient;

import org.jboss.logging.Logger;

@ApplicationScoped
public class VaccineSchedulingService {

    private static Logger log = Logger.getLogger(VaccineSchedulingService.class);

    // Latitude and longitude window of the city of Atlanta, US.
    public static final double MINIMUM_LATITUDE = 33.40;
    public static final double MAXIMUM_LATITUDE = 34.10;
    public static final double MINIMUM_LONGITUDE = -84.90;
    public static final double MAXIMUM_LONGITUDE = -83.90;

    public static final String[] PERSON_FIRST_NAMES = {
            "Ann", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Kurt", "Luke", "Mia", "Noa", "Otto", "Paul", "Quin", "Ray", "Sue", "Taj",
            "Uma", "Vix", "Wade", "Xiu" , "Yuna", "Zara"};
    public static final LocalDate MINIMUM_BIRTH_DATE = LocalDate.of(1930, 1, 1);
    public static final int BIRTH_DATE_RANGE_LENGTH = (int) DAYS.between(MINIMUM_BIRTH_DATE, LocalDate.of(2000, 1, 1));

    @Inject
    @RestClient
    FhirServerClient fclient;

    private VaccinationSchedule vSchedule;
    
    /*
    TO-DO: Pull needed Resources from FHIR Server IOT create VaccinationSchedule object
    */
    public VaccinationSchedule refreshVaccinationSchedule() {
        LocalDate windowStartDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        int windowDaysLength = 5;
        LocalTime dayStartTime = LocalTime.of(9, 0);
        int injectionDurationInMinutes = 30;
        int injectionsPerLinePerDay = (int) (MINUTES.between(dayStartTime, LocalTime.of(17, 0))
        / injectionDurationInMinutes);
        
        Random random = new Random(17);
        List<VaccineType> vaccineTypeList = List.of(VaccineType.values());
        List<PlanningVaccinationCenter> vaccinationCenterList = new ArrayList<>();
        vaccinationCenterList.add(new PlanningVaccinationCenter("Alpha center", pickLocation(random), 3));
        vaccinationCenterList.add(new PlanningVaccinationCenter("Beta center", pickLocation(random), 1));
        vaccinationCenterList.add(new PlanningVaccinationCenter("Gamma center", pickLocation(random), 1));
        int lineTotal = vaccinationCenterList.stream().mapToInt(PlanningVaccinationCenter::getLineCount).sum();
        
        List<LocalDateTime> timeslotDateTimeList = new ArrayList<>(windowDaysLength * injectionsPerLinePerDay);
        for (int dayIndex = 0; dayIndex < windowDaysLength; dayIndex++) {
            LocalDate date = windowStartDate.plusDays(dayIndex);
            for (int timeIndex = 0; timeIndex < injectionsPerLinePerDay; timeIndex++) {
                LocalTime time = dayStartTime.plusMinutes(injectionDurationInMinutes * timeIndex);
                timeslotDateTimeList.add(LocalDateTime.of(date, time));
            }
        }
        
        int personListSize = (lineTotal * injectionsPerLinePerDay * windowDaysLength) * 5 / 4; // 25% too many
        List<PlanningPerson> personList = new ArrayList<>(personListSize);
        long personId = 0L;
        for (int i = 0; i < personListSize; i++) {
            int lastNameI = i / PERSON_FIRST_NAMES.length;
            String name = PERSON_FIRST_NAMES[i % PERSON_FIRST_NAMES.length]
            + " " + (lastNameI < 26 ? ((char) ('A' + lastNameI)) + "." : lastNameI + 1);
            PlanningLocation location = pickLocation(random);
            LocalDate birthdate = MINIMUM_BIRTH_DATE.plusDays(random.nextInt(BIRTH_DATE_RANGE_LENGTH));
            int age = (int) YEARS.between(birthdate, windowStartDate);
            boolean firstShotInjected = random.nextDouble() < 0.25;
            VaccineType firstShotVaccineType = firstShotInjected ? pickVaccineType(random) : null;
            if (firstShotInjected && age >= 55 && firstShotVaccineType == VaccineType.ASTRAZENECA) {
                firstShotVaccineType = random.nextDouble() < 0.5 ? VaccineType.PFIZER : VaccineType.MODERNA;
            }
            LocalDate secondShotIdealDate = firstShotInjected ?
            windowStartDate.plusDays(random.nextInt(windowDaysLength))
            : null;
            PlanningPerson person = new PlanningPerson(Long.toString(personId++), name, location,
            birthdate, age, firstShotInjected, firstShotVaccineType, secondShotIdealDate);
            personList.add(person);
        }
        
        List<Injection> injectionList = new ArrayList<>();
        long injectionId = 0L;
        for (PlanningVaccinationCenter vaccinationCenter : vaccinationCenterList) {
            for (int dayIndex = 0; dayIndex < windowDaysLength; dayIndex++) {
                LocalDate date = windowStartDate.plusDays(dayIndex);
                for (int lineIndex = 0; lineIndex < vaccinationCenter.getLineCount(); lineIndex++) {
                    VaccineType vaccineType = pickVaccineType(random);
                    for (int timeIndex = 0; timeIndex < injectionsPerLinePerDay; timeIndex++) {
                        LocalTime time = dayStartTime.plusMinutes(injectionDurationInMinutes * timeIndex);
                        injectionList.add(new Injection(
                            injectionId++, vaccinationCenter, lineIndex,
                            LocalDateTime.of(date, time), vaccineType)
                        );
                    }
                    
                }
            }
        }
        vSchedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList, timeslotDateTimeList, personList, injectionList);
        return vSchedule;
    }
    

    public VaccinationSchedule getVaccinationSchedule(){
        return vSchedule;
    }

        
    /*
        Given a new VaccinationSchedule, post Appointments to FHIR Server
    */
    public void saveVaccinationSchedule(VaccinationSchedule vSchedule) {
        // TO-DO:  Post to FHIR Server
        this.vSchedule = vSchedule;
    }
    
    public void handleException(Long l, Throwable t) {
        log.error("Error during solver execution: "+t.getMessage());
    }

    public PlanningLocation pickLocation(Random random) {
        double latitude = MINIMUM_LATITUDE + (random.nextDouble() * (MAXIMUM_LATITUDE - MINIMUM_LATITUDE));
        double longitude = MINIMUM_LONGITUDE + (random.nextDouble() * (MAXIMUM_LONGITUDE - MINIMUM_LONGITUDE));
        return new PlanningLocation(latitude, longitude);
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
