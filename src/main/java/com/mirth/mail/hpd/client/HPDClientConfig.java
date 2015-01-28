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

import com.mirth.mail.hpd.models.HPDInstanceModel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HPDClientConfig {
    
    final static Logger log = Logger.getLogger(HPDClientConfig.class.getName());
    
    //Default envelope to use for all DSML requests.  This can be overriden by the container or by the value in the HPDInstanceModel()
    //When this client is built by Mirth Mail from its configuration data, this is overridden by the configuration option com.mirth.mail.hpd.stdHPDSOAPRequestBody
    private static String DEFAULT_SOAP_REQUEST_TEMPLATE =
              "<?xml version='1.0' encoding='UTF-8'?> "
            + "<soap-env:Envelope xmlns:soap-env='http://schemas.xmlsoap.org/soap/envelope/' xmlns:a=\'http://www.w3.org/2005/08/addressing\'> "
            + "   <soap-env:Header> "
            + "      <a:Action soap-env:mustUnderstand='0'>urn:ihe:iti:hpd:2010:ProviderInformationQueryRequest</a:Action> "
            + "      <a:MessageID soap-env:mustUnderstand='0'>urn:uuid:%s</a:MessageID> "
            + "       <a:ReplyTo><a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address></a:ReplyTo> "
            + "       <a:To soap-env:mustUnderstand='1'>%s</a:To> "
            + "   </soap-env:Header>"
            + "   <soap-env:Body> "
            + "   <batchRequest xmlns='urn:oasis:names:tc:DSML:2:0:core' requestID='%s'> "
            + "     %s "
            + "   </batchRequest> "
            + "   </soap-env:Body> "
            + "</soap-env:Envelope>";
            
    public static Integer DEFAULT_REQUEST_TIMEOUT_MS    = 4000;  //4 seconds
    public static String DEFAULT_CLIENT_HOME_DIR        = "/opt/mirthmail/";
    
    private Integer defaultRequestTimeoutMS;
    private String soapRequestTemplate;
    private String clientHomeDir;

    private List<HPDInstanceModel> instances;
    
    public HPDClientConfig() {     
        this.defaultRequestTimeoutMS = DEFAULT_REQUEST_TIMEOUT_MS;
        this.soapRequestTemplate = DEFAULT_SOAP_REQUEST_TEMPLATE;
        this.clientHomeDir = DEFAULT_CLIENT_HOME_DIR;
    }
    
    public HPDClientConfig(String clientHomeDir) {     
        this.defaultRequestTimeoutMS = DEFAULT_REQUEST_TIMEOUT_MS;
        this.soapRequestTemplate = DEFAULT_SOAP_REQUEST_TEMPLATE;
        this.clientHomeDir = clientHomeDir;
    }    

    public List<HPDInstanceModel> getInstances() {
        if (instances==null) {
            instances = new ArrayList<HPDInstanceModel>();
        }
        return instances;
    }

    public void setInstances(List<HPDInstanceModel> instances) {
        this.instances = instances;
    }
    
    public void addInstance(HPDInstanceModel instance) {
        if (instance==null) return;
        //Guard against adding the same instance
        for (HPDInstanceModel existingInstance : getInstances()) {
            if (existingInstance.getId().equalsIgnoreCase(instance.getId())) {
                log.warning("HPDClientConfig.addInstance:  attempted to add an existing instance");
                return;
            }
        }
        getInstances().add(instance);
    }

    public String getSoapRequestTemplate() {
        return soapRequestTemplate;
    }

    public void setSoapRequestTemplate(String soapRequestTemplate) {
        this.soapRequestTemplate = soapRequestTemplate;
    }

    public Integer getDefaultRequestTimeoutMS() {
        return defaultRequestTimeoutMS;
    }

    public void setDefaultRequestTimeoutMS(Integer defaultRequestTimeoutMS) {
        this.defaultRequestTimeoutMS = defaultRequestTimeoutMS;
    }

    private String getClientHomeDir() {
        return clientHomeDir;
    }

    private void setClientHomeDir(String clientHomeDir) {
        this.clientHomeDir = clientHomeDir;
    }
    
    public String getHPDKeyStorePath() {
        return getClientHomeDir() + "/hpdkeystore.jks";
    }
    
    public String getHPDTrustStorePath() {
        return getClientHomeDir() + "/hpdtruststore.jks";
    }
    
}