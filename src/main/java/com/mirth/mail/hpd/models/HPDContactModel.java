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

import java.io.Serializable;

public class HPDContactModel implements Serializable {

    private static final long serialVersionUID = 1L;
    public static String CONTACT_TYPE_TELEPHONE   = "1";
    public static String CONTACT_TYPE_FACIMILE    = "2";
    public static String CONTACT_TYPE_EMAIL       = "3";
    public static String CONTACT_TYPE_DIRECT_ADDR = "4";    
    
    private String contactTypeId;
    private String contactAddress;
    private String affiliatedOrgId;
    private String affiliatedOrgName;
    private String affiliatedOrgLDAPDn;
    
    public HPDContactModel() {
        
    }
    
    public HPDContactModel(String contactTypeId, String contactAddress) {
        this.contactTypeId = contactTypeId;
        this.contactAddress = contactAddress;
    }
    
    public HPDContactModel(String contactTypeId, String contactAddress, String affiliatedOrgId, String affiliatedOrgName, String affiliatedOrgLDAPDn) {
        this.contactTypeId = contactTypeId;
        this.contactAddress = contactAddress;
        this.affiliatedOrgId = affiliatedOrgId;
        this.affiliatedOrgName = affiliatedOrgName;
        this.affiliatedOrgLDAPDn = affiliatedOrgLDAPDn;
    }    

    public String getAffiliatedOrgId() {
        return affiliatedOrgId;
    }

    public void setAffiliatedOrgId(String affiliatedOrgId) {
        this.affiliatedOrgId = affiliatedOrgId;
    }
    
    public String getAffiliatedOrgLDAPDn() {
        return affiliatedOrgLDAPDn;
    }

    public void setAffiliatedOrgLDAPDn(String affiliatedOrgLDAPDn) {
        this.affiliatedOrgLDAPDn = affiliatedOrgLDAPDn;
    }

    public String getContactTypeId() {
        return contactTypeId;
    }

    public void setContactTypeId(String contactType) {
        this.contactTypeId = contactType;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getAffiliatedOrgName() {
        return affiliatedOrgName;
    }

    public void setAffiliatedOrgName(String affiliatedOrgName) {
        this.affiliatedOrgName = affiliatedOrgName;
    }
    
    public boolean isServiceAddress() {
        return contactTypeId.equals(CONTACT_TYPE_DIRECT_ADDR);
    }
    
}