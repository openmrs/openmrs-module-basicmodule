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
package org.openmrs.module.growthchart.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PatientEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller backs the /web/module/basicmoduleForm.jsp page. This controller is tied to that
 * jsp page in the /metadata/moduleApplicationContext.xml file
 */

public class GrowthChartFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * This class returns the form backing object. This can be a string, a boolean, or a normal java
	 * pojo. The type can be set in the /config/moduleApplicationContext.xml file or it can be just
	 * defined by the return type of this method
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		Patient patient = null;
		if (Context.isAuthenticated()) {
			String patientId = request.getParameter("patientId");
			if (patientId == null)
				patientId = request.getParameter("patientId");
			log.info("patientId: " + patientId);
			if (patientId == null)
				throw new Exception("Integer 'patientId' is a required parameter");
			
			PatientService ps = Context.getPatientService();
			Integer id = null;
			
			try {
				id = Integer.valueOf(patientId);
				patient = ps.getPatient(id);
			}
			catch (NumberFormatException numberError) {
				log.warn("Invalid patientId supplied: '" + patientId + "'", numberError);
			}
			catch (org.springframework.orm.ObjectRetrievalFailureException noPatientEx) {
				log.warn("There is no patient with id: '" + patientId + "'", noPatientEx);
			}
			
			if (patient == null)
				throw new Exception("There is no patient with id: '" + patientId + "'");
		} else {
			patient = new Patient();
		}
		
		Concept weightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.weight"));
		Concept heightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.height"));
		
		// Create new Encounter Command Object
		Encounter growthEncounter;
		
		String sObsId = request.getParameter("obsId");
		Integer obsId = null;
		
		if (sObsId != null) {
			obsId = Integer.valueOf(sObsId);
		}
		
		if (obsId != null) { // Editing Encounter
			EncounterService encounterService = Context.getEncounterService();
			ObsService obsService = Context.getObsService();
			
			Obs obs = obsService.getObs(obsId);
			growthEncounter = obs.getEncounter();
			
		} else { // New Encounter
			EncounterType clinicianEntered = new EncounterType(Integer.parseInt(Context.getAdministrationService()
			        .getGlobalProperty("encountertype.clinician")));
			List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
			encounterTypes.add(clinicianEntered);
			
			growthEncounter = new Encounter();
			growthEncounter.setEncounterType(clinicianEntered);
			growthEncounter.setPatient(patient);
			growthEncounter.setEncounterDatetime(new Date());
			
			// Get Obs for Encounter
			Obs weightObs = new Obs();
			weightObs.setPerson(patient);
			weightObs.setConcept(weightConcept);
			
			Obs heightObs = new Obs();
			heightObs.setPerson(patient);
			heightObs.setConcept(heightConcept);
			
			// add observations
			growthEncounter.addObs(weightObs);
			growthEncounter.addObs(heightObs);
		}
		
		return growthEncounter;
	}
	
	/**
	 * Returns any extra data in a key-->value pair kind of way
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		
		
		Map<String, Object> model = new HashMap<String, Object>();
		
		// Get Encounter Command Object from Form Backing
		Encounter growthEncounter = (Encounter) obj;
		Patient patient = growthEncounter.getPatient();
		String sObsId = request.getParameter("obsId");		
		
		Concept weightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.weight"));
		Concept heightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.height"));
		
		// Add data needed for form
		if(sObsId != null && !sObsId.equals("")) {
			for (Obs o : growthEncounter.getAllObs()) {
				if (o.getConcept().equals(heightConcept)) {
					model.put("heightValue", o.getValueNumeric());
				} else if (o.getConcept().equals(weightConcept)) {
					model.put("weightValue", o.getValueNumeric());
				}
			}
		}
		
		String heightValue = request.getParameter("heightValue");
		String weightValue = request.getParameter("weightValue");
				
		// if validation fails and the form is shown again, bind the values of the height and weight fields
		if(heightValue != null && !heightValue.equals("")) {
			model.put("heightValue", heightValue);
			log.info("height inserted");
		}
		if(weightValue != null && !weightValue.equals("")) {
			model.put("weightValue", weightValue);
			log.info("weight inserted");
		}
		
		model.put("obsId", sObsId);
		model.put("patient", patient);
		
		return model;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(User.class, new UserEditor());
		binder.registerCustomEditor(Location.class, new LocationEditor());
		binder.registerCustomEditor(Patient.class, new PatientEditor());
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(OpenmrsUtil.getDateFormat(), true));
	}
	
	@Override
	protected void onBind(HttpServletRequest request, Object obj, BindException errors) {
		
		Concept weightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.weight"));
		Concept heightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.height"));
		
		Encounter growthEncounter = (Encounter) obj;
		
		String heightValue = request.getParameter("heightValue");
		String weightValue = request.getParameter("weightValue");
		
		// Validate and Bind Obs to Command
		for (Obs o : growthEncounter.getAllObs()) {
			if (o.getConcept().equals(heightConcept)) {
				
				if (StringUtils.hasText(heightValue)) {
					o.setValueNumeric(Double.valueOf(heightValue));
				}
				
			} else if (o.getConcept().equals(weightConcept)) {
				
				if (StringUtils.hasText(heightValue)) {
					o.setValueNumeric(Double.valueOf(weightValue));
				}
			}
			Location location = growthEncounter.getLocation();
			Date date = growthEncounter.getEncounterDatetime();
			
			o.setLocation(location);
			o.setObsDatetime(date);
			
			growthEncounter.addObs(o);
		}
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object,
	                                BindException exceptions) throws Exception {
		
		Encounter growthEncounter = (Encounter) object;
		Integer patientId = growthEncounter.getPatientId();
		
		// save the encounter
		EncounterService encounterService = Context.getEncounterService();
		encounterService.saveEncounter(growthEncounter);
		
		return new ModelAndView(new RedirectView(getSuccessView() + "?patientId=" + patientId));
	}
}
