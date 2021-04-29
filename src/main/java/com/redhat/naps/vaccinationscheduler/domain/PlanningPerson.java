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

package com.redhat.naps.vaccinationscheduler.domain;

import java.time.LocalDate;

import org.optaplanner.core.api.domain.lookup.PlanningId;

public class PlanningPerson {

    @PlanningId
    private String id;

    private String name;
    private PlanningLocation homeLocation;
    private LocalDate birthdate;
    private int age;

    private boolean firstShotInjected;
    private VaccineType firstShotVaccineType;
    private LocalDate secondShotIdealDate;
    
    public PlanningPerson() {}

    public PlanningPerson(String id, String name, PlanningLocation homeLocation, LocalDate birthdate, int age) {
        this(id, name, homeLocation, birthdate, age, false, null, null);
    }

    public PlanningPerson(String id, String name, PlanningLocation homeLocation, LocalDate birthdate, int age,
            boolean firstShotInjected, VaccineType firstShotVaccineType, LocalDate secondShotIdealDate) {
        this.id = id;
        this.name = name;
        this.homeLocation = homeLocation;
        this.birthdate = birthdate;
        this.age = age;
        this.firstShotInjected = firstShotInjected;
        this.firstShotVaccineType = firstShotVaccineType;
        this.secondShotIdealDate = secondShotIdealDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlanningLocation getHomeLocation() {
        return homeLocation;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public int getAge() {
        return age;
    }

    public boolean isFirstShotInjected() {
        return firstShotInjected;
    }

    public VaccineType getFirstShotVaccineType() {
        return firstShotVaccineType;
    }

    public LocalDate getSecondShotIdealDate() {
        return secondShotIdealDate;
    }

    @Override
    public String toString() {
        return "PlanningPerson [age=" + age + ", birthdate=" + birthdate + ", firstShotInjected=" + firstShotInjected
                + ", firstShotVaccineType=" + firstShotVaccineType + ", homeLocation=" + homeLocation + ", id=" + id
                + ", name=" + name + ", secondShotIdealDate=" + secondShotIdealDate + "]";
    }

    
}
