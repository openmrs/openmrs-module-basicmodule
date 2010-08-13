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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;


/**
 *
 */
public class GrowthChartTableGraphController extends ParameterizableViewController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	
	protected String formViewName;

	/**
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	Map<String, Object> model = new HashMap<String, Object>();
    	
    	// Get the Patient
    	Patient patient = null;
		if (Context.isAuthenticated()) {
	        String patientId = request.getParameter("patientId");
	        if (patientId == null)
	            patientId = request.getParameter("patientId");
	        log.info("patientId: " + patientId);
	        if (patientId == null)
	            throw new Exception(
	                    "Integer 'patientId' is a required parameter");
	
	        PatientService ps = Context.getPatientService();
	        Integer id = null;
	
	        try {
	            id = Integer.valueOf(patientId);
	            patient = ps.getPatient(id);
	        } catch (NumberFormatException numberError) {
	            log.warn("Invalid patientId supplied: '" + patientId + "'",
	                    numberError);
	        } catch (org.springframework.orm.ObjectRetrievalFailureException noPatientEx) {
	            log.warn("There is no patient with id: '" + patientId + "'",
	                    noPatientEx);
	        }
	
	        if (patient == null)
	            throw new Exception("There is no patient with id: '"
	                    + patientId + "'");
		} else {
			patient = new Patient();
		}
        
		Concept weightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.weight"));
		Concept heightConcept = Context.getConceptService().getConceptByIdOrName(
		    Context.getAdministrationService().getGlobalProperty("concept.height"));
		
		// Determine if Encounter already entered Today
		Calendar obsDate = Calendar.getInstance();
		Date today = obsDate.getTime();
		
		Calendar midnight = Calendar.getInstance();
		midnight.set(Calendar.HOUR, -12);
		midnight.set(Calendar.MINUTE, 0);
		midnight.set(Calendar.SECOND, 0);
		Date midnightObsDate = midnight.getTime();
		
		// Check today's existing Encounters to see if height and weight have already been entered
		EncounterService encounterService = Context.getEncounterService();
		EncounterType clinicianEntered = new EncounterType(Integer.parseInt(Context.getAdministrationService()
	        .getGlobalProperty("growthchart.encountertype.clinician")));
		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		encounterTypes.add(clinicianEntered);
		List<Encounter> encountersWithinADay = encounterService.getEncounters(patient, null, midnightObsDate, today, null,
		    encounterTypes, false);
		
		boolean encounterComplete = false;

		for (Encounter e : (List<Encounter>) encountersWithinADay) {
			boolean heightComplete = false;
			boolean weightComplete = false;
			for (Obs o : e.getAllObs()) {
				if (o.getValueNumeric() != null && o.getConcept().getConceptId().equals(heightConcept.getConceptId())) {
					heightComplete = true;
				} else if (o.getValueNumeric() != null && o.getConcept().getConceptId().equals(weightConcept.getConceptId())) {
					weightComplete = true;
				}
			}
			if (heightComplete && weightComplete) { // Encounter has already been entered today and is not being edited
				encounterComplete = true;
				break;
			}
		}
		
		if(!encounterComplete) {
			log.debug("%%%%%%%%%%%%%%%%%% Encounter NOT Complete");
			
			return new ModelAndView(new RedirectView(request.getContextPath() + formViewName + "?patientId="+patient.getPatientId()), model);
			
		} else {
			log.debug("%%%%%%%%%%%%%%%%%% Encounter Complete");
    	
	    	// Gather graph data
			List<DataSeries> weightSeries = formatGraphData(patient, "weightForAgeGraph", "Weight", weightConcept);        
	        model.put("weightForAgeSeries", weightSeries);
	        
			List<DataSeries> heightSeries = formatGraphData(patient, "heightForAgeGraph", "Height", heightConcept);      
	        model.put("heightForAgeSeries", heightSeries);
	        
	        // Gather table data
	        List<TableRow> weightRows = formatTableData(patient, "Weight", weightConcept);
	        model.put("weightTable", weightRows);
	        
	        List<TableRow> heightRows = formatTableData(patient, "Height", heightConcept);
	        model.put("heightTable", heightRows);
	        
	        model.put("patient", patient);
	        
		    return new ModelAndView(getViewName(), model);
		}
    }
    
    private static final long MS_PER_DAY = 1000l * 60 * 60 * 24;
    private static final double DAYS_PER_MONTH = 365.2422 / 12;
    
    public class DataSeries {
        boolean showPoints;
        String name;
        String color;
        public List<DataPoint> data;
        public DataSeries(String name, String color, boolean showPoints) {
            this.name = name;
            this.color = color;
            this.showPoints = showPoints;
            data = new ArrayList<DataPoint>();
        }
        public void addPoint(DataPoint p) {
            data.add(p);
        }
        public void addPoint(double x, double y) {
            data.add(new DataPoint(x, y));
        }
        public String getName() {
            return name;
        }
        public String getColor() {
            return color;
        }
        public boolean isShowPoints() {
            return showPoints;
        }
        public List<DataPoint> getData() {
            return data;
        }
    }
    
    public class DataPoint {
        public double x;
        public double y;
        public DataPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public double getX() {
            return x;
        }
        public double getY() {
            return y;
        }
    }
    
    public class TableRow {
		public Obs o;
		public double months;
    	public TableRow(Obs o, double months) {
    		this.o = o;
    		this.months = months;
    	}
    	public double getMonths() {
            return months;
        }
    	public Obs getObs() {
            return o;
        }
    }
    
    public List<TableRow> formatTableData(Patient patient, String conceptName, Concept concept) {
    	
    	List<TableRow> observations = new ArrayList<TableRow>();
    	
    	long birthdate = (patient.getBirthdate()).getTime();
        ObsService obsService = Context.getObsService();
        java.util.List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        for (Obs o : (List<Obs>) patientObs) {
            if (o.getValueNumeric() != null && o.getConcept().getConceptId().equals(concept.getConceptId())) { 
                double days = (o.getObsDatetime().getTime() - birthdate) / MS_PER_DAY;
                double months = days / DAYS_PER_MONTH;
                
                // Data for the Tables
                TableRow tablerow = new TableRow(o, months);
                
                observations.add(tablerow);
            }
        }
    	
    	return observations; 
    }
    
    public List<DataSeries> formatGraphData(Patient patient, String graphType, String conceptName, Concept concept) {
    	List<DataSeries> series = new ArrayList<DataSeries>();
        
        try {
        	
            List<DataSeries> background = new ArrayList<DataSeries>();
            
            String prop = null;
            
            if (patient.getGender().toLowerCase().equals("m") && patient.getAge() <= 3) {
                prop = Context.getAdministrationService().getGlobalProperty("growthchart."+graphType+".background.child.male");
            } else if (patient.getGender().toLowerCase().equals("f") && patient.getAge() <= 3) {
                prop = Context.getAdministrationService().getGlobalProperty("growthchart."+graphType+".background.child.female");
            } else if (patient.getGender().toLowerCase().equals("m") && patient.getAge() < 20) {
                prop = Context.getAdministrationService().getGlobalProperty("growthchart."+graphType+".background.adolescent.male");
            } else if (patient.getGender().toLowerCase().equals("f") && patient.getAge() < 20) {
                prop = Context.getAdministrationService().getGlobalProperty("growthchart."+graphType+".background.adolescent.female");
            }
            if (StringUtils.hasText(prop)) {
                // treat as TSV
                String[] rows = prop.split("\n");
                // header row first
                {
                    String[] row = rows[0].replace("\r", "").split("\t");
                    // skip the first cell in the header row
                    for (int i = 1; i < row.length; ++i) {
                    	String lineColor = "#a0a0a0";
                    	// java switch statement for line color
                    	switch (i) {
	                        case 1:  lineColor = "#dd0000"; break;
	                        case 2:  lineColor = "#ffff00"; break;
	                        case 3:  lineColor = "#009900"; break;
	                        case 4:  lineColor = "#0000cc"; break;
	                        case 5:  lineColor = "#6600cc"; break;
	                        default: lineColor = "#a0a0a0";break;
                    	}

                        background.add(new DataSeries(row[i], lineColor, false));
                    }
                }
                // now the data rows
                for (int i = 1; i < rows.length; ++i) {
                    String[] row = rows[i].replace("\r", "").split("\t");
                    Double ageInMonths = Double.valueOf(row[0]);
                    for (int j = 1; j < row.length; ++j) {
                        if (StringUtils.hasText(row[j])) {
                            Double val = Double.valueOf(row[j]);
                            background.get(j - 1).addPoint(ageInMonths, val);
                        }
                    }
                }
                series.addAll(background);
            }
        } catch (Exception ex) {
            log.error("Error reading "+graphType+" background data", ex);
        }
        DataSeries foreground = new DataSeries(conceptName, "#000000", true);
        long birthdate = (patient.getBirthdate()).getTime();
        ObsService obsService = Context.getObsService();
        java.util.List<Obs> patientObs = obsService.getObservationsByPerson(patient);
    
        for (Obs o : (List<Obs>) patientObs) {
            if (o.getValueNumeric() != null && o.getConcept().getConceptId().equals(concept.getConceptId())) { 
                double days = (o.getObsDatetime().getTime() - birthdate) / MS_PER_DAY;
                double months = days / DAYS_PER_MONTH;
                foreground.addPoint(months, o.getValueNumeric());
            }
        }
        series.add(foreground);
        
        return series;
    }
    
	
	/**
	 * @return the formViewName
	 */
	public String getFormViewName() {
		return formViewName;
	}
	
	/**
	 * @param formViewName the formView to set
	 */
	public void setFormViewName(String formViewName) {
		this.formViewName = formViewName;
	}
}
