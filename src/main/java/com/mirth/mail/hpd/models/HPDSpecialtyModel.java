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

public class HPDSpecialtyModel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    public String authorityOID;
    public String authorityName;
    public String specialtyCode;
    public String specialtyName;
    
    public HPDSpecialtyModel() {
        
    }
    
    public HPDSpecialtyModel(String ldapSpecialtyAttribute) {
        String[] parts = ldapSpecialtyAttribute.split(":");
        //Ok, does this look like a standard HPD 'structured' attribute in the AuthorityOID:AuthorityName:CODE:Label format?
        if (parts.length>2) {
            this.authorityOID = parts[0];
            this.authorityName = parts[1];
            this.specialtyCode = parts[2];
            //GSI Hack until they clean up their act
            if (parts.length==4) {
                this.specialtyName = parts[3];
            } else {
                this.specialtyName = parts[2];
            }
        } else {
            //Hmmm, what the heck did we get... 
            this.specialtyName = ldapSpecialtyAttribute;
        }
    }      

    @Override
    public String toString() {
        return super.toString() + "[authorityOID=" + authorityOID + ", authorityName=" + authorityName + ", specialtyCode=" + specialtyCode + ", specialtyName=" + specialtyName + "]";
    }
    
    public String getAuthorityOID() {
        return authorityOID;
    }

    public void setAuthorityOID(String authorityOID) {
        this.authorityOID = authorityOID;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getSpecialtyCode() {
        return specialtyCode;
    }

    public void setSpecialtyCode(String specialtyCode) {
        this.specialtyCode = specialtyCode;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }

}