package org.bahmni.module.hip.api.dao;

import org.openmrs.Obs;

import java.util.List;

public interface ObsDao {
    List<String> getObsUnits(Obs obs);

}
