/**
 * Copyright (c) 2006-2013 Mirth Corporation.
 * All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains, the
 * property of Mirth Corporation. The intellectual and technical
 * concepts contained herein are proprietary and confidential to
 * Mirth Corporation and may be covered by U.S. and Foreign
 * Patents, patents in process, and are protected by trade secret
 * and/or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from Mirth Corporation.
 */
package com.mirth.mail.hpd.client;

import com.mirth.mail.hpd.models.HPDCredentialModel;
import com.mirth.mail.hpd.models.HPDElectronicServiceModel;
import com.mirth.mail.hpd.models.HPDEntityModel;
import com.mirth.mail.hpd.models.HPDOrgToProvRelationshipModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HPDSearchResult extends HPDResult {
    private static final long serialVersionUID = -223470930028307572L;
    
    //The list of entities returned from a federated search
    private List<HPDEntityModel> entities;
    private List<HPDCredentialModel> credentials;
    private List<HPDOrgToProvRelationshipModel> relationships;
    private List<HPDElectronicServiceModel> services;

    //The detailed level result map indexed by the HPD Id with HPDResult results from each Federated HPD searched 
    private HashMap<String, HPDResult> directorySearchResults;
    
    public HPDSearchResult() {
    }

    public HPDSearchResult(int resultCode, String resultMsg) {
        super(resultCode, resultMsg);
    }
    
    public HPDSearchResult(int resultCode, String resultMsg, String resultDSML) {
        super(resultCode, resultMsg, resultDSML);
    }
    
    public HPDSearchResult(int resultCode, ProcessedHPDResponseDoc responseDocEntities) {
         setResultCode(resultCode);
         setResponseDSML(HPDUtil.toXMLString(responseDocEntities.getOrignalResponseDoc(), false, true));
         setResultMsg("Search returned " + responseDocEntities.getEntities().size() + " entities and " + responseDocEntities.getCredentials().size() + " credentials and " + responseDocEntities.getRelationships().size() + " relationships and " + responseDocEntities.getServices().size());
         this.entities = responseDocEntities.getEntities();
         this.credentials = responseDocEntities.getCredentials();
         this.relationships = responseDocEntities.getRelationships();
         this.services = responseDocEntities.getServices();
    }

    public List<HPDEntityModel> getEntities() {
        if (entities == null) {
            entities = new ArrayList<HPDEntityModel>();
        }
        return entities;
    }

    public void setEntities(List<HPDEntityModel> entities) {
        this.entities = entities;
    }

    public void addEntity(HPDEntityModel entity) {
        getEntities().add(entity);
    }
    
    public List<HPDCredentialModel> getCredentials() {
        if (credentials == null) {
            credentials = new ArrayList<HPDCredentialModel>();
        }
        return credentials;
    }

    public void setCredentials(List<HPDCredentialModel> credentials) {
        this.credentials = credentials;
    }

    public void addCredential(HPDCredentialModel credential) {
        getCredentials().add(credential);
    }
    
    public List<HPDOrgToProvRelationshipModel> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<HPDOrgToProvRelationshipModel>();
        }
        return relationships;
    }

    public void setRelationships(List<HPDOrgToProvRelationshipModel> relationships) {
        this.relationships = relationships;
    }

    public void addRelationship(HPDOrgToProvRelationshipModel relationship) {
        getRelationships().add(relationship);
    }
    
    public List<HPDElectronicServiceModel> getServices() {
        if (services == null) {
            services = new ArrayList<HPDElectronicServiceModel>();
        }
        return services;
    }
    
    public void setServices(List<HPDElectronicServiceModel> services) {
        this.services = services;
    }

    public void addServices(HPDElectronicServiceModel service) {
        getServices().add(service);
    }  
    
    public HashMap<String, HPDResult> getDirectorySearchResults() {
        if (directorySearchResults == null) {
            directorySearchResults = new HashMap<String, HPDResult>();
        }        
        return directorySearchResults;
    }

    public void setDirectorySearchResults(HashMap<String, HPDResult> directoryResults) {
        this.directorySearchResults = directoryResults;
    }
    
    public void addDirectorySearchResult(String directoryId,HPDResult directoryResult) {
        getDirectorySearchResults().put(directoryId, directoryResult);
    }
    
    public void updateHPDResult(String directoryId, HPDSearchResult result) {
        if (result.getResultCode()<0 || result.getResultMsg()==null) {
            setResultCode(result.getResultCode());
            setResultMsg(result.getResultMsg());
        }
        addDirectorySearchResult(directoryId, result);
    }
    
//    public void merge(String directoryId, HPDSearchResult result) {
//        this.getEntities().addAll(result.getEntities());
//        if (result.getResultCode()<0 || result.getResultMsg()==null) {
//            setResultCode(result.getResultCode());
//            setResultMsg(result.getResultMsg());
//        }
//        addDirectorySearchResult(directoryId, result);
//    }

}
