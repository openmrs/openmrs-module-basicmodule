/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.growthchart.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


public class GrowthChartValidator implements Validator {
	
	protected final Log log = LogFactory.getLog(getClass());
  
  public boolean supports(Class clazz) {
    return Encounter.class.isAssignableFrom(clazz);
  }


  public void validate(Object obj, Errors e) {
    
    Encounter growthEncounter = (Encounter) obj;
    
    // Obs are validated via javascript on the page 
    // Spring support for Sets is not working properly
    // so the Obs are not being saved in the command object until after validation
 
    if (growthEncounter.getEncounterDatetime().equals("")) {
      e.rejectValue("encounterDatetime", "growthchart.encounterDatetime.empty");
    } 
    
    // this is a hack because the native method doesn't work properly (with openmrs tags??)
    if (growthEncounter.getProvider() == null) {
        e.rejectValue("provider", "growthchart.provider.empty");
      } 
    if (growthEncounter.getLocation() == null) {
        e.rejectValue("location", "growthchart.location.empty");
      } 
  }
}
