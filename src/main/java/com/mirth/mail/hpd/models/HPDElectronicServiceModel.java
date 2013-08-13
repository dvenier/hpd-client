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

import com.mirth.mail.hpd.client.exceptions.HPDObjectWithNoUIDException;
import com.mirth.mail.hpd.client.exceptions.UnexpectedLDAPObjectException;
import org.w3c.dom.NodeList;

public class HPDElectronicServiceModel extends HPDBaseModel {
    
    private String serviceId;
    private String serviceAddress;
    private String contentProfile;
    private String integrationProfile;
    public String securityProfile;
    private byte[] certificate;
    
    public HPDElectronicServiceModel() {
    }    

    public HPDElectronicServiceModel(HPDInstanceModel directoryInstance, String dn, NodeList entity) throws UnexpectedLDAPObjectException, HPDObjectWithNoUIDException {
        //Set the directory information for the HPD this Entity came from
        setDirectoryId(directoryInstance.getId());
        setDirectoryName(directoryInstance.getName());
        setDN(dn);
        setEntityAttrsFromEntity(entity);
        
        this.serviceId = getSimpleAttrValue("hpdserviceid");
        this.serviceAddress = getSimpleAttrValue("hpdServiceAddress");
        this.integrationProfile = getSimpleAttrValue("hpdIntegrationProfile");
        this.securityProfile = getSimpleAttrValue("hpdSecurityProfile");   
        
        //Handle the certificate bytes later...
    }  
    
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getContentProfile() {
        return contentProfile;
    }

    public void setContentProfile(String contentProfile) {
        this.contentProfile = contentProfile;
    }

    public String getIntegrationProfile() {
        return integrationProfile;
    }

    public void setIntegrationProfile(String integrationProfile) {
        this.integrationProfile = integrationProfile;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public String getSecurityProfile() {
        return securityProfile;
    }

    public void setSecurityProfile(String securityProfile) {
        this.securityProfile = securityProfile;
    }
}
