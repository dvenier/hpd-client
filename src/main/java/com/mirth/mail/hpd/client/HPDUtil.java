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

import com.mirth.mail.hpd.models.HPDCredentialModel;
import com.mirth.mail.hpd.models.HPDEntityModel;
import com.mirth.mail.hpd.models.HPDInstanceModel;
import com.mirth.mail.hpd.models.HPDOrgToProvRelationshipModel;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class HPDUtil {

    public static void dumpSearchResultEntities(HPDSearchResult result) {
        System.out.println(String.format("\nSearchResult: code=%s msg=%s",result.getResultCode(),result.getResultMsg().substring(80)));
        System.out.println("DirectoryId     UID Authority   UID                            DisplayName                              Type Status          PracticeAddress");
        System.out.println("=============== =============== ============================== ======================================== ==== =============== ======================================================");
        for (HPDEntityModel entity : (List<HPDEntityModel>)result.getEntities()) {
            System.out.print(StringUtils.rightPad(entity.getDirectoryId(),16));     
            System.out.print(StringUtils.rightPad(entity.getAuthorityId(),16));             
            System.out.print(StringUtils.rightPad(entity.getEntityUID(),31));            
            System.out.print(StringUtils.rightPad(entity.getName(),41));
            System.out.print(StringUtils.rightPad(entity.getEntityTypeId(),5));
            System.out.print(StringUtils.rightPad(entity.getStatus(),16));  
            System.out.println(StringUtils.rightPad(entity.getPracticeAddressString(),50));
            if (entity.getCredentials().size()>0) {
                for (HPDCredentialModel credential : entity.getCredentials()) {
                    System.out.println("  Credential: " + credential.getId() + ":" + credential.getNumber());
                }
            }
            if (entity.getRelationships().size()>0) {
                for (HPDOrgToProvRelationshipModel relationship : entity.getRelationships()) {
                    System.out.println("  Relationship: " + relationship.getId());
                }
            }            
        }
        System.out.println("\n");
    }
    
    public static void dumpSearchResultDetailed(HPDSearchResult result) {
        System.out.println(String.format("\nSearchResult: code=%s msg=%s",result.getResultCode(),result.getResultMsg()));
        for (HPDEntityModel entity : (List<HPDEntityModel>)result.getEntities()) {
            System.out.print(entity.toXML());
        }
        System.out.println("\n");            
    }
    
    public static String generateRequestId() {
        int randomNbr = (int)(Math.random() * 100000);
        return new Integer(randomNbr).toString();
    }
    
    public static boolean isBlank(String str) {
        return !isNotBlank(str);
    }
    
    public static boolean isNotBlank(String str) {
        return (str!=null && str.trim()!=null && str.trim().length()>0);
    }
    
    /**
     * 
     * @param rawPhoneNumber A String that contains a telephone number (e.g., 213-555-1212
     * @return A String containing a phone number that conforms to LDAP requirements
     */
    public static String toDSMLTelephone(String rawPhoneNumber) {

        if (StringUtils.isBlank(rawPhoneNumber)) {
            return null;
        }
        
        rawPhoneNumber = rawPhoneNumber.replaceAll("[^0-9]", "");

        StringBuilder nbr = new StringBuilder();
        if (StringUtils.isBlank(rawPhoneNumber)) {
            return null;
        }

        int len = rawPhoneNumber.length();

        // strip out the country code
        if (len > 10) {
            if (rawPhoneNumber.startsWith("1")) {
                rawPhoneNumber = rawPhoneNumber.substring(1, len);
                len = rawPhoneNumber.length();
            }
        }

        if (len == 7) {
            nbr.append(rawPhoneNumber.substring(0, 3));
            nbr.append("-");
            nbr.append(rawPhoneNumber.substring(3, len));
            return nbr.toString();
        }
        if (len > 0) {
            if (len > 7) {
                nbr.append(" ");
            }
            nbr.append(rawPhoneNumber.substring(0, (len < 3 ? len : 3)));
            if (len > 7) {
                nbr.append(" ");
            }
        }
        if (len > 3) {
            if (len > 6) {
                nbr.append(" ");
            }
            nbr.append(rawPhoneNumber.substring(3, (len < 6 ? len : 6)));
        }
        if (len > 6) {
            nbr.append("-");
            nbr.append(rawPhoneNumber.substring(6, (len < 10 ? len : 10)));
        }
        // account for extensions
        if (len > 10) {
            nbr.append(" ");
            nbr.append(rawPhoneNumber.substring(10, len));
        }
        nbr.insert(0, "+1 ");
        return nbr.toString();

    }    

    public static Document toDocument(String xmlText) {
        return toDocument(xmlText, false);
    }

    public static Document toDocument(String xmlText, boolean namespaceAware) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(namespaceAware);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            StringReader sr = new StringReader(xmlText);
            InputSource source = new InputSource(sr);
            doc = dBuilder.parse(source);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new RuntimeException("Error trying to parse XML in to document.", e);
        }
        return doc;
    }

    public static String toXMLString(Document doc, boolean includeXMLDecl, boolean indent) {
        String xmlString = null;
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, includeXMLDecl ? "yes" : "no");
            trans.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            xmlString = sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error trying to render DOM to XML String");
        }
        return xmlString;
    }
    
    public static String getIdFromDN(String dn) {
        String id = null;
        String[] dnParts = dn.split(",");
        if (dnParts.length>0) {
            String idPart = dnParts[0];
            if (idPart.split("=").length>0) {
                id = idPart.split("=")[1];
            }
        }        
        return id;
    }
    
    public static String getUnqualifiedUIDFromDN(String dn) {
        //Assums a DN in the form "uid=XXX:YYYY,ou=ZZZ,..."
        String unqualifiedUID = null;
        //Split the DN by ","
        String[] dnParts = dn.split(",");
        if (dnParts.length>0) {
            //Grab the first token which should be the uid=XXX:YYY part of the DN
            String uidPart = dnParts[0];
            //Split that by the =
            if (uidPart.split("=").length>0) {
                //Take the second token of that split and this is the XXX:YYY part of the UID
                String qualifiedUID = uidPart.split("=")[1];
                //Split the resulting token by the : to get the ID part of the UID minus the qualifier
                unqualifiedUID = qualifiedUID.split(":")[1];
            }
        }        
        return unqualifiedUID;
    }
    
    public static String getQualifiedUIDFromDN(String dn) {
        //Assums a DN in the form "uid=XXX:YYYY,ou=ZZZ,..."
        String qualifiedUID = null;
        //Split the DN by ","
        String[] dnParts = dn.split(",");
        if (dnParts.length>0) {
            //Grab the first token which should be the uid=XXX:YYY part of the DN
            String uidPart = dnParts[0];
            //Split that by the =
            if (uidPart.split("=").length>0) {
                //Take the second token of that split and this is the XXX:YYY part of the UID
                qualifiedUID = uidPart.split("=")[1];
            }
        }        
        return qualifiedUID;
    }    
    
    public static String buildQualifiedUID(HPDInstanceModel hpdInstance, String uid) {
        String rawUID = uid;       
        boolean hasQualifier = rawUID.contains(":");
        //If we need th qualifier but it's not there, add it
        if (!hasQualifier) {
            rawUID = hpdInstance.getId() + ":" + uid;
        }
        return rawUID;
    }    
    
    public static String buildQualifiedDN(HPDInstanceModel hpdInstance, String uid, String typeId) {
        String qualifiedUID = buildQualifiedUID(hpdInstance, uid);
        String bucket = typeId.equalsIgnoreCase(Constants.ENTITY_TYPE_ORG) ? Constants.ENTITY_TYPE_ORG_RDN_OU : Constants.ENTITY_TYPE_INDIVIDUAL_RDN_OU;
        String dn = String.format("uid=%s,ou=%s,%s", qualifiedUID, bucket,hpdInstance.getBaseDN());
        
        return dn;
    }
}