package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.ObsDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Obs;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class ObsDaoImpl implements ObsDao {

    private SessionFactory sessionFactory;

    private String sqlGetObsUnits= "select units from concept_numeric as cn join obs on cn.concept_id=obs.concept_id where obs_id = :obsId ; ";

    @Override
    public List<String> getObsUnits(Obs obs){
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetObsUnits);
        query.setParameter("obsId", obs.getObsId());

        return query.list();
    }
}
