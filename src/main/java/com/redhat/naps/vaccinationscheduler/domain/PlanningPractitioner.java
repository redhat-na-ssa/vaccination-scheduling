package com.redhat.naps.vaccinationscheduler.domain;

public class PlanningPractitioner {
    
    private String name;
    
    public PlanningPractitioner(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}
