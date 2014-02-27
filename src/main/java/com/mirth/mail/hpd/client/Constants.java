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

public class Constants {

    //Entity Types
    public static final int    ENTITY_TYPE_INDIVIDUAL_KEY      = 1;
    public static final String ENTITY_TYPE_INDIVIDUAL          = "1";
    public static final int    ENTITY_TYPE_ORG_KEY             = 2;
    public static final String ENTITY_TYPE_ORG                 = "2";
    
    //LDAP OU Constants
    public static final String ENTITY_TYPE_ORG_RDN_OU          = "HCRegulatedOrganization";
    public static final String ENTITY_TYPE_INDIVIDUAL_RDN_OU   = "HCProfessional";
    public static final String ENTITY_TYPE_CREDENTIAL_RDN_OU   = "HPDCredential";
    public static final String ENTITY_TYPE_RELATIONSHIP_RDN_OU = "HPDProviderMembership";    
    public static final String ENTITY_TYPE_SERVICE_RDN_OU      = "HPDElectronicService";     
     
    //Provider Directory Auth types
    public static final int    PROVIDER_DIR_AUTH_TYPE_NONE     = 0;
    public static final int    PROVIDER_DIR_AUTH_TYPE_BASIC    = 1;
    public static final int    PROVIDER_DIR_AUTH_TYPE_TLS      = 2;    
 
    //DSML Constants
    public static final String DSML_ERROR_RESPONSE_RESULT      = "errorResponse";
    public static final String DSML_REQUEST_MALFORMED_RESULT   = "malformedRequest";
}