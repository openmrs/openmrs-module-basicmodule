package org.bahmni.module.hip.model;

import org.openmrs.Obs;

public class PatientObs {
    protected String units;
    protected Obs obs;

    public PatientObs(String units, Obs obs){
        this.units = units;
        this.obs = obs;
    }

    public void setUnits(String units){
        this.units = units;
    }

    public String getUnits(){
        return this.units;
    }

    public void setObs(Obs obs){
        this.obs = obs;
    }

    public Obs getObs() {
        return obs;
    }


}
