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

import com.mirth.mail.hpd.client.HPDUtil;
import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlTransient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HPDBaseModel implements IHPDModel {
    private HashMap<String, List<String>> entityAttrs = new HashMap<String, List<String>>();
    private String dn;
    private String directoryId;
    private String directoryName;    
    
    @XmlTransient    
    public void setEntityAttrsFromEntity(NodeList entity) {
        //Ok, we need to iterate through the NodeList to get all the Attributes of the Entity create a NVP list on them
        for (int i = 0; i < entity.getLength(); i++) {
            Node attrNode = entity.item(i);
            //Sometimes we get a node with no Attributes.  Skip those.
            if (attrNode.getAttributes()!=null && attrNode.getAttributes().getNamedItem("name")!=null) {            
                String id = attrNode.getAttributes().getNamedItem("name").getNodeValue();
                NodeList values = attrNode.getChildNodes();
                List<String> valuesList = new ArrayList<String>();
                for (int j = 0; j < values.getLength(); j++) {
                    //Skip empty Values
                    if (values.item(j)!=null && HPDUtil.isNotBlank(values.item(j).getTextContent())) {
                        valuesList.add(values.item(j).getTextContent());
                    }
                }
                //If we got 1 or more values, we can add it to the list.
                //NOTE: We could decide later to allow the Attr but with no value indicating that the remote system
                //Stored the Attribute, but no Values related to the Attribute.
                if (valuesList.size()>0) {
                    entityAttrs.put(id, valuesList);
                }
            }
        }        
    }
    
    @XmlTransient
    public String getSimpleAttrValue(String attrId) {
        for (Map.Entry<String,List<String>> attrsEntry : getEntityAttrs().entrySet()) {
            if (attrsEntry.getKey().equalsIgnoreCase(attrId)) {
                return attrsEntry.getValue().get(0);
            }
        }
        return "";
    }

    @XmlTransient
    public List<String> getMultiValueAttr(String attrId) {
        for (Map.Entry<String,List<String>> attrsEntry : getEntityAttrs().entrySet()) {
            if (attrsEntry.getKey().equalsIgnoreCase(attrId)) {
                return attrsEntry.getValue();
            }        
        }
        return new ArrayList<String>();
    }

    @XmlTransient
    public boolean hasObjectClass(String objectClassId) {
        List<String> objectClasses = getMultiValueAttr("objectClass");
        for (String objectClass : objectClasses) {
            if (objectClassId.equalsIgnoreCase(objectClass)) {
                return true;
            }
        }
        return false;
    }    
    
    
    @Override
    public String toXML() {
        XStream xs = new XStream();
        return xs.toXML(this);
    }

    @Override
    public HashMap<String, List<String>> getEntityAttrs() {
        return entityAttrs;
    }

    @Override
    public void setEntityAttrs(HashMap<String, List<String>> entityAttrs) {
        this.entityAttrs = entityAttrs;
    }    
    
    @Override
    public String getDN() {
        return dn;
    }
    
    public void setDN(String dn) {
        this.dn = dn;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }  

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }    
}
