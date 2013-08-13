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
import com.mirth.mail.hpd.client.HPDClientConfig;
import java.io.Serializable;

public class HPDInstanceModel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String id;                      //Required: Globally Unique Identifier for an HPD instance.  This might be an OID or a DNS entry such as hpd.careaccord.org, etc
    private String name;                    //Required: A label or title for a given HPD instance.  Example:  HealthBridge Public Provider Directory, etc
    private Integer isActive;               //Is this Directory active or inactive.  If inactive, it will not be searched
    private String descr;                   //Optional: A narrative description of a provider directory 
    private String baseDN;                  //Required: The Base DN to begin all HPD searches at within the remote HPD instance LDAP tree
    private String serviceURL;              //Required: The Service URL for for remote HPD instance.  
    private Integer authType;               //The authorization type for this HPD instance
    private String username;                //The username to use for basic Auth
    private String password;                //The password to use for basic Auth
    private String customHPDRequestXSLT;    //Do we need to modify the DSML request with a custom XSLT before we send it out
    private Integer requestTimeoutMS;       //Timeout to wait for HPD requests in milliseconds
    
    public HPDInstanceModel() {     
        this.authType = Constants.PROVIDER_DIR_AUTH_TYPE_NONE;
        this.requestTimeoutMS = HPDClientConfig.DEFAULT_REQUEST_TIMEOUT_MS; 
        this.isActive = 1;
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

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }
    
    public boolean getIsActive() {
        return isActive != null && isActive != 0;
    }

    public boolean isActive() {
        return getIsActive();
    }
    
    public void setIsActive(boolean isActive) {
        this.isActive = isActive ? 1 : 0;
    }
    
    public String getCustomHPDRequestXSLT() {
        return customHPDRequestXSLT;
    }

    public void setCustomHPDRequestXSLT(String customHPDRequestXSLT) {
        this.customHPDRequestXSLT = customHPDRequestXSLT;
    }    
    
    public Integer getRequestTimeoutMS() {
        return requestTimeoutMS;
    }

    public void setRequestTimeoutMS(Integer requestTimoutMS) {
        this.requestTimeoutMS = requestTimoutMS;
    }    
    
}