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

import java.util.HashMap;
import java.util.List;

public interface IHPDModel {
 
    public String getDN();
    
    public HashMap<String, List<String>> getEntityAttrs();
    
    public void setEntityAttrs(HashMap<String, List<String>> entityAttrs);
    
    public String toXML();    
}
