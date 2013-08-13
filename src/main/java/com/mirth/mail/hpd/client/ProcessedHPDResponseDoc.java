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

import com.mirth.mail.hpd.models.HPDContactModel;
import com.mirth.mail.hpd.models.HPDCredentialModel;
import com.mirth.mail.hpd.models.HPDElectronicServiceModel;
import com.mirth.mail.hpd.models.HPDEntityModel;
import com.mirth.mail.hpd.models.HPDOrgToProvRelationshipModel;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;

public class ProcessedHPDResponseDoc {    
    
    private Document orignalResponseDoc;
    private List<HPDEntityModel> entities;
    private List<HPDCredentialModel> credentials;
    private List<HPDOrgToProvRelationshipModel> relationships;
    private List<HPDElectronicServiceModel> services;
 
    public ProcessedHPDResponseDoc() {
    }
    
    public ProcessedHPDResponseDoc(Document orignalResponseDoc) {
        this.orignalResponseDoc = orignalResponseDoc;
    }
    

    public Document getOrignalResponseDoc() {
        return orignalResponseDoc;
    }

    public void setOrignalResponseDoc(Document orignalResponseDoc) {
        this.orignalResponseDoc = orignalResponseDoc;
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
    
    public void addCredentialToEntity(String entityUID, HPDCredentialModel credential) {
        findEntityByUID(entityUID).addCredential(credential);
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
    
    public void addRelationshipToEntity(String entityUID, HPDOrgToProvRelationshipModel relationship) {
        findEntityByUID(entityUID).addRelationship(relationship);
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
    
    public void addServiceToEntity(String entityUID, HPDElectronicServiceModel service) {
        findEntityByUID(entityUID).addService(service);
    }      
    
    public void addContactToEntity(String entityUID, HPDContactModel contact) {
        findEntityByUID(entityUID).addContact(contact);
    }
    
    public void addServiceToRelationship(String relationshipDN, HPDElectronicServiceModel service) {
        findRelationshipByDN(relationshipDN).addService(service);
    }
    
    public void addOrgModelToAffiliatedRelationships(HPDEntityModel orgModel) {
        for (HPDEntityModel entity : getEntities()) {
            for (HPDOrgToProvRelationshipModel relationship : entity.getRelationships()) {
                if (relationship.getHasAnOrgDN().toLowerCase().equals(orgModel.getDN().toLowerCase())) {
                    relationship.setRelatedOrg(orgModel);
                    for (HPDContactModel contact : relationship.getContacts()) {
                        if (contact.getAffiliatedOrgLDAPDn().equals(orgModel.getDN())) {
                            contact.setAffiliatedOrgId(orgModel.getEntityUID());
                            contact.setAffiliatedOrgName(orgModel.getName());
                        }
                    }
                }
            }
        }
    }
    
    private HPDEntityModel findEntityByUID(String entityUID) {
        for (HPDEntityModel entity : getEntities()) {
            if (entity.getEntityUID().equalsIgnoreCase(entityUID)) {
                return entity;
            }
        }
        throw new RuntimeException("findEntityByUID: Entity not found by EntityUID '" + entityUID + "'");
    }
    
    private HPDOrgToProvRelationshipModel findRelationshipByDN(String relationshipDN) {
        for (HPDEntityModel entity : getEntities()) {
            for (HPDOrgToProvRelationshipModel relationship : entity.getRelationships()) {
                if (relationship.getDN().equalsIgnoreCase(relationshipDN)) {
                    return relationship;
                }
            }
        }
        throw new RuntimeException("findRelationshipByDN: Relationship not found by relationshipDN '" + relationshipDN + "'");
    }   
    
    public int getTotalEntiesFound() {
        return getCredentials().size() + getEntities().size() + getRelationships().size() + getServices().size();
    }

}
