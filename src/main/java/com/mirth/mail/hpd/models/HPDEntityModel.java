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

import com.mirth.mail.hpd.client.Constants;
import com.mirth.mail.hpd.client.exceptions.HPDObjectWithNoUIDException;
import com.mirth.mail.hpd.client.HPDUtil;
import com.mirth.mail.hpd.client.exceptions.UnexpectedLDAPObjectException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.NodeList;

public class HPDEntityModel extends HPDBaseModel implements Serializable, Comparable<HPDEntityModel> {

    private static final long serialVersionUID = 1L;

    private String entityTypeId = Constants.ENTITY_TYPE_INDIVIDUAL;

    private String uid;
    private String authorityId;
    private String entityUID;
    private String displayName;
    private String orgName;
    private String givenName;
    private String middleName;
    private String title;
    private String surname;
    private String status;
    private String profession;
    private List<HPDContactModel> contacts;
    private List<HPDAddressModel> addresses;
    private List<HPDOrgToProvRelationshipModel> relationships;
    private List<HPDCredentialModel> credentials;
    private List<HPDElectronicServiceModel> services;
    private List<HPDSpecialtyModel> specialties;
    
    //Dns for related LDAP entries
    private List<String> serviceDNs;
    private List<String> credentialDNs;

    public HPDEntityModel() {
    }

    public HPDEntityModel(HPDInstanceModel directoryInstance, String dn, NodeList entity) throws UnexpectedLDAPObjectException, HPDObjectWithNoUIDException {
        //Set the directory information for the HPD this Entity came from
        setDirectoryId(directoryInstance.getId());
        setDirectoryName(directoryInstance.getName());
        setDN(dn);
        setEntityAttrsFromEntity(entity);

        //Now let's do the mapping starting with the UID which correlates to our EntityUID
        //In HPD, the UID is qualified by a Authority in the format of authorityid:uid
        uid = getSimpleAttrValue("uid");
        //Patch for now until UID gets into Orgs:  Grab the hpdOrgId
        if (StringUtils.isBlank(uid)) {
            uid = getSimpleAttrValue("hpdOrgId");
        }
        String[] uidTokens = uid.split(":");
        //Something is up with the UID.  If the knuckleheads return a simple ID not qualified by an authority, let it go
        if (uidTokens.length < 2) {
            if (uidTokens.length == 1) {
                uid = getDirectoryId() + ":" + uid;
                uidTokens = uid.split(":");
            }
            if (uidTokens.length < 2) {
                if (!hasObjectClass("HPDProvider") && !hasObjectClass("hcRegulatedOrganization")) {
                    throw new UnexpectedLDAPObjectException("Skipping structural or non-Entity result. CN=" + getSimpleAttrValue("cn"));
                } else {             
                    throw new HPDObjectWithNoUIDException("Unable to contruct HPD entry:  missing or invalid UID.  CN=" + getSimpleAttrValue("cn"));
                }
            }
        }
        authorityId = uidTokens[0];
        entityUID = uidTokens[1];

        //Figure out the type:
        //If they returned us the ObjectClass list, we can tell by an org has hcRegulatedOrganization
        //If they simply returned us a list of Attributes, technically only Orgs have the 'o' attribute
        entityTypeId = hasObjectClass("hcRegulatedOrganization") || HPDUtil.isNotBlank(getSimpleAttrValue("o")) ? Constants.ENTITY_TYPE_ORG : Constants.ENTITY_TYPE_INDIVIDUAL;
        profession = getSimpleAttrValue("hcProfession");
        status = getSimpleAttrValue("hpdProviderStatus");

        if (entityTypeId.equals(Constants.ENTITY_TYPE_INDIVIDUAL)) {
            givenName = getSimpleAttrValue("givenName");
            surname = getSimpleAttrValue("sn");
            middleName = getSimpleAttrValue("initials");
            title = getSimpleAttrValue("title");
        } else {
            //Handle the name information for Org and for a regular person
            orgName = getSimpleAttrValue("o");            
        }
        displayName = getSimpleAttrValue("displayName");
        //Default displayName to the 'o' attribute if displayName is blank and we got an 'o'
        if (HPDUtil.isBlank(displayName) && !HPDUtil.isBlank(getSimpleAttrValue("o"))) {
            displayName = getSimpleAttrValue("o");
        }
        //Default displayName to the 'cn' attribute if displayName is blank and we got an 'cn'
        if (HPDUtil.isBlank(displayName) && !HPDUtil.isBlank(getSimpleAttrValue("cn"))) {
            displayName = getSimpleAttrValue("cn");
        }        
        
        //Handle Contacts
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_TELEPHONE, getSimpleAttrValue("telephoneNumber")));
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_FACIMILE, getSimpleAttrValue("facsimileTelephoneNumber")));
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_EMAIL, getSimpleAttrValue("mail")));
        addContact(new HPDContactModel(HPDContactModel.CONTACT_TYPE_DIRECT_ADDR, getSimpleAttrValue("hpdMedicalRecordsDeliveryEmailAddress")));

        //Handle Addresses
        addAddress(new HPDAddressModel(HPDAddressModel.ADDRESS_TYPE_BILLING,  "hpdProviderBillingAddress",  getSimpleAttrValue("hpdProviderBillingAddress")));
        addAddress(new HPDAddressModel(HPDAddressModel.ADDRESS_TYPE_MAILING,  "hpdProviderMailingAddress",  getSimpleAttrValue("hpdProviderMailingAddress")));
        addAddress(new HPDAddressModel(HPDAddressModel.ADDRESS_TYPE_PRACTICE, "hpdProviderPracticeAddress", getSimpleAttrValue("hpdProviderPracticeAddress")));

        
        //Store any Service dn-s that we'll use later to fetch the details
        for (String serviceDN : getMultiValueAttr("hpdHasAService")) {
            addServiceDN(serviceDN);
        }
        
        //Store any Credential dn-s that we'll use later to fetch the details
        for (String credentialDN : getMultiValueAttr("hpdCredential")) {
            addCredentialDN(credentialDN);
        }        
        
        //Add any Specialties that come down the pike
        for (String ldapSpecialtyAttribute : getMultiValueAttr("hcSpecialization")) {
            addSpecialty(new HPDSpecialtyModel(ldapSpecialtyAttribute));
        }
        
     }

    public String getEntityUID() {
        return entityUID;
    }

    public void setEntityUID(String entityUID) {
        this.entityUID = entityUID;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOrg() {
        return entityTypeId.equals(Constants.ENTITY_TYPE_ORG);
    }

    public String getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(String authorityId) {
        this.authorityId = authorityId;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public final List<HPDAddressModel> getAddresses() {
        if (addresses == null) {
            addresses = new ArrayList<HPDAddressModel>();
        }
        return addresses;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final void setAddresses(List<HPDAddressModel> addresses) {
        this.addresses = addresses;
    }

    public final void addAddress(HPDAddressModel addressModel) {
        if (addressModel != null && addressModel.hasAddress()) {
            getAddresses().add(addressModel);
        }
    }

    public final HPDAddressModel getAddressByType(String addressTypeId) {
        for (HPDAddressModel addr : getAddresses()) {
            if (addr.getAddressTypeId().equalsIgnoreCase(addressTypeId)) {
                return addr;
            }
        }
        return null;
    }
    
    public final HPDAddressModel getPracticeAddress() {
        return getAddressByType(HPDAddressModel.ADDRESS_TYPE_PRACTICE);
    }    
    
    public final HPDAddressModel getMailingAddress() {
        return getAddressByType(HPDAddressModel.ADDRESS_TYPE_MAILING);
    }  
    
    public final HPDAddressModel getBillingAddress() {
        return getAddressByType(HPDAddressModel.ADDRESS_TYPE_BILLING);
    }  
    
    public final HPDAddressModel getPrimaryAddress() {
        if (getPracticeAddress()!=null) {
            return getPracticeAddress();
        }
        if (getMailingAddress()!=null) {
            return getMailingAddress();
        }
        if (getBillingAddress()!=null) {
            return getBillingAddress();
        }
        return null;
    }  
    
    public final String getPracticeAddressString() {
        HPDAddressModel addr = getAddressByType(HPDAddressModel.ADDRESS_TYPE_PRACTICE);
        if (addr!=null) {
            return addr.getFullAddress();
        }
        return "";
    }
    
    public final String getMailingAddressString() {
        HPDAddressModel addr = getAddressByType(HPDAddressModel.ADDRESS_TYPE_MAILING);
        if (addr!=null) {
            return addr.getFullAddress();
        }
        return "";
    }
    
    public final String getBillingAddressString() {
        HPDAddressModel addr = getAddressByType(HPDAddressModel.ADDRESS_TYPE_BILLING);
        if (addr!=null) {
            return addr.getFullAddress();
        }
        return "";
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
    
    public final List<HPDSpecialtyModel> getSpecialties() {
        if (specialties == null) {
            specialties = new ArrayList<HPDSpecialtyModel>();
        }
        return specialties;
    }

    public final void setSpecialties(List<HPDSpecialtyModel> specialties) {
        this.specialties = specialties;
    }

    public final void addSpecialty(HPDSpecialtyModel specialtyModel) {
        if (specialtyModel != null && HPDUtil.isNotBlank(specialtyModel.getSpecialtyName())) {
            getSpecialties().add(specialtyModel);
        }
    }
    
    public final List<HPDOrgToProvRelationshipModel> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<HPDOrgToProvRelationshipModel>();
        }
        return relationships;
    }

    public final void setRelationships(List<HPDOrgToProvRelationshipModel> relationships) {
        this.relationships = relationships;
    }

    public final void addRelationship(HPDOrgToProvRelationshipModel relationshipModel) {
        if (relationshipModel != null) {
            getRelationships().add(relationshipModel);
        }
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
    
    public final List<HPDCredentialModel> getCredentials() {
        if (credentials == null) {
            credentials = new ArrayList<HPDCredentialModel>();
        }
        return credentials;
    }

    public final void setCrednetials(List<HPDCredentialModel> credentials) {
        this.credentials = credentials;
    }

    public final void addCredential(HPDCredentialModel credential) {
        if (credential != null) {
            getCredentials().add(credential);
        }
    }
    
    public final List<String> getServiceDNs() {
        if (serviceDNs == null) {
            serviceDNs = new ArrayList<String>();
        }
        return serviceDNs;
    }

    public final void setServiceDNs(List<String> serviceDNs) {
        this.serviceDNs = serviceDNs;
    }

    public final void addServiceDN(String serviceDN) {
        if (HPDUtil.isNotBlank(serviceDN) && !getServiceDNs().contains(serviceDN)) {
            getServiceDNs().add(serviceDN);
        }
    }   
    
    public final List<String> getCredentialDNs() {
        if (credentialDNs == null) {
            credentialDNs = new ArrayList<String>();
        }
        return credentialDNs;
    }

    public final void setCredentialDNs(List<String> credentialDNs) {
        this.credentialDNs = credentialDNs;
    }

    public final void addCredentialDN(String credentialDN) {
        if (HPDUtil.isNotBlank(credentialDN) && !getCredentialDNs().contains(credentialDN)) {
            getCredentialDNs().add(credentialDN);
        }
    }     
    
    @XmlTransient
    public String getName() {
        if (entityTypeId.equalsIgnoreCase(Constants.ENTITY_TYPE_INDIVIDUAL)) {
            return surname + ", " + givenName;
        } else {
            return getOrgName();
        }
    }
    
    public List<HPDEntityModel> getAffiliatedOrgs() {
        List<HPDEntityModel> affiliatedOrgs = new ArrayList<HPDEntityModel>();
        for (HPDOrgToProvRelationshipModel relationship : getRelationships()) {
            if (relationship.getRelatedOrg()==null) {
                Logger.getLogger(HPDEntityModel.class.getName()).log(Level.WARNING, "HPDOrgToProvRelationshipModel with no relatedOrg (null org model).  relationshipDN={0}", relationship.getDN());                
            } else {
                if (!affiliatedOrgs.contains(relationship.getRelatedOrg())) {
                    affiliatedOrgs.add(relationship.getRelatedOrg());
                }
            }
        }
        return affiliatedOrgs;
    }

    @Override
    public int compareTo(HPDEntityModel that) {

        String thisName = "";
        String thatName = "";

        if (this.isOrg()){
            thisName = this.getName();
        } else {
            thisName = this.getSurname();
        }

        if (that.isOrg()){
            thatName = that.getName();
        } else {
            thatName = that.getSurname();
        }

        return thisName.compareTo(thatName);
    }

}