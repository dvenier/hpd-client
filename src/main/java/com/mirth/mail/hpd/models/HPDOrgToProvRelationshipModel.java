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
package com.mirth.mail.hpd.models;

import com.mirth.mail.hpd.client.HPDUtil;
import com.mirth.mail.hpd.client.exceptions.HPDObjectWithNoUIDException;
import com.mirth.mail.hpd.client.exceptions.UnexpectedLDAPObjectException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.NodeList;

public class HPDOrgToProvRelationshipModel extends HPDBaseModel implements IHPDModel {
    
    private String id;                                  //This is the id of the Relationship (the hpdmemberid attribute)
    private String hasAnOrgDN;                          //This is the DN of the Org we are related to                
    private HPDEntityModel relatedOrg;                  //This is the Org we are related to
    private String hasAProviderDN;                      //This is the DN of "Me" when attached to an HPDEntityModel, so somewhat redundant but maybe useful
    private List<String> hasAServiceDN;                 //These are "my" service DNs for the relationship to an ORG
    private List<HPDElectronicServiceModel> services;   //These are the electronic services attached/associated with this relationsip
    private List<HPDContactModel> contacts;             // Stores the telephone, Pager, mobile, and Email attributes
 
    
    public HPDOrgToProvRelationshipModel() {
    }
    
    public HPDOrgToProvRelationshipModel(HPDInstanceModel directoryInstance, String dn, NodeList entity) throws UnexpectedLDAPObjectException, HPDObjectWithNoUIDException {
        //Set the directory information for the HPD this Entity came from
        setDirectoryId(directoryInstance.getId());
        setDirectoryName(directoryInstance.getName());
        setDN(dn);
        setEntityAttrsFromEntity(entity);
        
        //Now for the Relationship specific attributes
        id = getSimpleAttrValue("hpdMemberId"); 
        hasAnOrgDN = getSimpleAttrValue("hpdHasAnOrg");
        hasAProviderDN = getSimpleAttrValue("hpdHasAProvider");
        
        hasAServiceDN = getMultiValueAttr("hpdHasAService");
        
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_TELEPHONE, getSimpleAttrValue("telephoneNumber"), HPDUtil.getUnqualifiedUIDFromDN(hasAnOrgDN), "ORG NAME PreWeave", hasAnOrgDN));
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_TELEPHONE, getSimpleAttrValue("mobile"), HPDUtil.getUnqualifiedUIDFromDN(hasAnOrgDN), "ORG NAME PreWeave", hasAnOrgDN));        
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_FACIMILE, getSimpleAttrValue("facsimileTelephoneNumber"), HPDUtil.getUnqualifiedUIDFromDN(hasAnOrgDN), "ORG NAME PreWeave", hasAnOrgDN));
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_EMAIL, getSimpleAttrValue("mail"), HPDUtil.getUnqualifiedUIDFromDN(hasAnOrgDN), "ORG NAME PreWeave", hasAnOrgDN));
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_EMAIL, getSimpleAttrValue("Email"), HPDUtil.getUnqualifiedUIDFromDN(hasAnOrgDN), "ORG NAME PreWeave", hasAnOrgDN));     
        
    }
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHasAnOrgDN() {
        return hasAnOrgDN;
    }

    public void setHasAnOrgDN(String hasAnOrgDN) {
        this.hasAnOrgDN = hasAnOrgDN;
    }

    public String getHasAProviderDN() {
        return hasAProviderDN;
    }

    public void setHasAProviderDN(String hasAProviderDN) {
        this.hasAProviderDN = hasAProviderDN;
    }

    public List<String> getHasAServiceDN() {
        return hasAServiceDN;
    }

    public void setHasAServiceDN(List<String> hasAServiceDN) {
        this.hasAServiceDN = hasAServiceDN;
    }
    

    public final List<HPDContactModel> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<HPDContactModel>();
        }
        return contacts;
    }

    public final void setContacts(List<HPDContactModel> contacts) {
        this.contacts = contacts;
    }

    public final void addContact(HPDContactModel contactModel) {
        if (contactModel != null && HPDUtil.isNotBlank(contactModel.getContactAddress())) {
            getContacts().add(contactModel);
        }
    }
    
    public final HPDContactModel getContactByType(String contactTypeId) {
        for (HPDContactModel contact : getContacts()) {
            if (contact.getContactTypeId().equalsIgnoreCase(contactTypeId)) {
                return contact;
            }
        }
        return null;
    } 
    

    public final List<HPDElectronicServiceModel> getServices() {
        if (services == null) {
            services = new ArrayList<HPDElectronicServiceModel>();
        }
        return services;
    }

    public final void setServices(List<HPDElectronicServiceModel> services) {
        this.services = services;
    }

    public final void addService(HPDElectronicServiceModel serviceModel) {
        if (serviceModel != null) {
            getServices().add(serviceModel);
        }
    }
    
    public final String getRelatedProviderEntityUID() {
        return HPDUtil.getUnqualifiedUIDFromDN(getHasAProviderDN());
        
    }
    
    public final String getRelatedOrgEntityUID() {
        return HPDUtil.getUnqualifiedUIDFromDN(getHasAnOrgDN());
    }

    public HPDEntityModel getRelatedOrg() {
        return relatedOrg;
    }

    public void setRelatedOrg(HPDEntityModel relatedOrg) {
        if (relatedOrg==null) {
            throw new RuntimeException("null related org!");
        }
        this.relatedOrg = relatedOrg;
    }
    
}
