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
import java.util.Date;
import org.w3c.dom.NodeList;

public class HPDCredentialModel extends HPDBaseModel implements IHPDModel {
    
    private String id;
    private String name;
    private String number;
    private String type;
    private String status;
    private String description;
    private Date issueDate;
    private Date rewewalDate;
    
    public HPDCredentialModel() {
    }
    
    public HPDCredentialModel(HPDInstanceModel directoryInstance, String dn, NodeList entity) throws UnexpectedLDAPObjectException, HPDObjectWithNoUIDException {
        //Set the directory information for the HPD this Entity came from
        setDirectoryId(directoryInstance.getId());
        setDirectoryName(directoryInstance.getName());
        setDN(dn);
        setEntityAttrsFromEntity(entity);
        
        name = getSimpleAttrValue("credentialName");
        number = getSimpleAttrValue("credentialNumber");      
        type = getSimpleAttrValue("credentialType");    
        status = getSimpleAttrValue("credentialStatus");   
        description = getSimpleAttrValue("description");  
        id = getSimpleAttrValue("hpdCredentialId"); 
        //TODO:  Process renewelDate and issueDate
    }
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getRewewalDate() {
        return rewewalDate;
    }

    public void setRewewalDate(Date rewewalDate) {
        this.rewewalDate = rewewalDate;
    }

    
}
