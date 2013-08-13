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

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HPDResult implements Serializable {
    private static final long serialVersionUID = -4199500639759397197L;

    static final Logger log = Logger.getLogger(HPDResult.class.getName());
    
    public static final int HPD_OPERATION_SUCCESS = 0;
    public static final int HPD_CONNECT_ERROR = -1;
    public static final int HPD_IO_ERROR = -2;
    public static final int HPD_INVALID_SERVICE_URL = -3;
    public static final int HPD_NOT_FOUND_IN_CONFIG_BY_ID = -4;
    public static final int HPD_UNEXPECTED_HTTP_STATUS_CODE = -5;
    public static final int HPD_RESPONSE_PARSE_ERROR = -6;
    public static final int HPD_MALFORMED_OR_INVALID_DSML_REQUEST = -7;
    public static final int HPD_CONNECT_TIMEOUT = -8;
    public static final int HPD_RESPONSE_TIMEOUT = -9;
    public static final int HPD_UNKNOWN_HOST = -10;
    public static final int HPD_NO_SEARCH_CRITERIA_SPECIFIED = -11;
    public static final int HPD_NO_DIRECTORY_SPECIFIED_ON_SEARCH = -12;
    public static final int HPD_MISSING_OR_INVALID_CERT = -13;
    public static final int HPD_CONNECTION_REFUSED = -14;
    public static final int HPD_TLS_ERROR = -15;
    
    private String resultMsg;
    private int resultCode;
    private String responseDSML;
    
    public HPDResult() {        
    }
    
    public HPDResult(int resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        log.log(Level.INFO, "HPDResult: resultCode={0} resultMsg={1}", new Object[]{resultCode,resultMsg});
    }
    
    public HPDResult(int resultCode, String resultMsg, String responseDSML) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.responseDSML = responseDSML;
        log.log(Level.INFO, "HPDResult: resultCode={0} resultMsg={1} responseDSML={2}", new Object[]{resultCode,resultMsg,responseDSML});
    }    

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResponseDSML() {
        return responseDSML;
    }

    public void setResponseDSML(String responseDSML) {
        this.responseDSML = responseDSML;
    }
    
}
