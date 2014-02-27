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

import com.mirth.mail.hpd.client.exceptions.HPDConnectionRefusedException;
import com.mirth.mail.hpd.client.exceptions.HPDCertificateException;
import com.mirth.mail.hpd.client.exceptions.HPDObjectWithNoUIDException;
import com.mirth.mail.hpd.client.exceptions.HPDRequestTimeoutException;
import com.mirth.mail.hpd.client.exceptions.HPDTLSException;
import com.mirth.mail.hpd.client.exceptions.HPDUnknownHostException;
import com.mirth.mail.hpd.client.exceptions.MalformedOrInvalidHPDRequestResponse;
import com.mirth.mail.hpd.client.exceptions.UnexpectedHPDCallException;
import com.mirth.mail.hpd.client.exceptions.UnexpectedLDAPObjectException;
import com.mirth.mail.hpd.models.HPDBaseModel;
import com.mirth.mail.hpd.models.HPDContactModel;
import com.mirth.mail.hpd.models.HPDCredentialModel;
import com.mirth.mail.hpd.models.HPDElectronicServiceModel;
import com.mirth.mail.hpd.models.HPDInstanceModel;
import com.mirth.mail.hpd.models.HPDEntityModel;
import com.mirth.mail.hpd.models.HPDOrgToProvRelationshipModel;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.security.validator.ValidatorException;

public class HPDClient {

    final static Logger log = Logger.getLogger(HPDClient.class.getName());

    private static HPDClientConfig config;
    private static final String PING_SEARCH_ORG_NAME = "Mercy Hospital";
    private static final int MIRTH_MAIL_HPD_STATUS_ACTIVE_ID = 24;

    public HPDClient() {
    }

    public HPDClient(List<HPDInstanceModel> instances) {
        config = new HPDClientConfig();
        for (HPDInstanceModel instance : instances) {
            config.addInstance(instance);
        }

    }
    //Takes a file stream or the String Stream of the config.xml for the service

    public HPDClient(InputStream configStream) {
        XStream x = new XStream();
        setConfig((HPDClientConfig) x.fromXML(configStream));
    }

    //Takes a JDBC Connection URL, username and password and initializes the client based on the connection created
    public HPDClient(String connectionURL, String user, String password) throws ClassNotFoundException, SQLException {

        // Load the Driver class.
        Class.forName("org.postgresql.Driver");

        //Create the connection using the static getConnection method
        Connection con = DriverManager.getConnection(connectionURL, user, password);

        initialize(con);
    }

    //Takes a JDBC Connection URL and initializes the client based on the connection created from the URL
    public HPDClient(String connectionURL) throws ClassNotFoundException, SQLException {

        // Load the Driver class.
        Class.forName("org.postgresql.Driver");

        //Create the connection using the static getConnection method
        Connection con = DriverManager.getConnection(connectionURL);

        initialize(con);
    }

    //Takes a JDBC Connection Connection initializes the client based on connecting to that database and fetching the ProviderDirectory rows
    //This constructor and the initialize method below assume the existence of a provider_directory table with a structure like the same table in Mirth Mail
    //The home directory is used to load keystores for TLS
    public HPDClient(String homeDirectoryPath, Connection con) throws ClassNotFoundException, SQLException {
        initialize(homeDirectoryPath, con);
    }

    private void initialize(Connection con) throws ClassNotFoundException, SQLException {
        initialize(null, con);
    }
    
    private void initialize(String homeDirectoryPath, Connection con) throws ClassNotFoundException, SQLException {
        config = new HPDClientConfig(homeDirectoryPath);

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select identifier,label,description,base_dn,service_url,auth_type,auth_username,auth_password,custom_hpd_request_xslt,request_timeout_ms,status_type_key from provider_directory");

        while (rs.next()) {
            HPDInstanceModel hpdInstance = new HPDInstanceModel();
            hpdInstance.setId(rs.getString("identifier"));
            hpdInstance.setName(rs.getString("label"));
            hpdInstance.setDescr(rs.getString("description"));
            hpdInstance.setBaseDN(rs.getString("base_dn"));
            hpdInstance.setServiceURL(rs.getString("service_url"));
            hpdInstance.setAuthType(rs.getInt("auth_type"));
            hpdInstance.setUsername(rs.getString("auth_username"));
            hpdInstance.setPassword(rs.getString("auth_password"));
            hpdInstance.setCustomHPDRequestXSLT(rs.getString("custom_hpd_request_xslt"));
            hpdInstance.setRequestTimeoutMS(rs.getInt("request_timeout_ms"));
            hpdInstance.setIsActive(rs.getInt("status_type_key") == MIRTH_MAIL_HPD_STATUS_ACTIVE_ID);
            config.addInstance(hpdInstance);
        }
        rs.close();
        //Get the default SOAP envelope
        rs = stmt.executeQuery("select config_value from configuration where label='com.mirth.mail.hpd.stdHPDSOAPRequestBody'");
        rs.next();
        config.setSoapRequestTemplate(rs.getString(1));
        rs.close();
        rs = stmt.executeQuery("select config_value from configuration where label='com.mirth.mail.hpd.defaultRequestTimeout'");
        rs.next();
        config.setDefaultRequestTimeoutMS(rs.getInt(1));
        rs.close();
        stmt.close();
        con.close();
    }

    //Generates a config.xml file from the information located in the MirthMail repository.  
    //Useful for generating a config.xml from an existing MirthMail instance for testing the library in standalone mode.
    private String buildConfigXMLFromConfig() {
        XStream x = new XStream();
        String configString = x.toXML(config);
        return configString;
    }

    public String getConfigXMLFromConfig() {
        return buildConfigXMLFromConfig();
    }

    public void dumpConfigXMLtoFilePath(String filePath) throws IOException {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        FileWriter fw = new FileWriter(f);
        fw.write(buildConfigXMLFromConfig());
        fw.close();
    }

    public HPDSearchResult searchHPD(HPDSearchRequest request) {

        HPDSearchResult result = search(request);

        return result;
    }

    public List<HPDInstanceModel> listDirectories() {
        return config.getInstances();
    }

    public String searchAsynch(HPDSearchRequest request) {
        return null;
    }

    public AsynchSearchStatus getAsynchSearchStatus(String searchRequestId) {
        return null;
    }

    public static HPDClientConfig getConfig() {
        return config;
    }

    public static void setConfig(HPDClientConfig aConfig) {
        config = aConfig;
    }

    public HPDInstanceModel getInstanceById(String instanceId) {
        for (HPDInstanceModel instance : config.getInstances()) {
            if (instance.getId().equalsIgnoreCase(instanceId)) {
                return instance;
            }
        }
        return null;
    }

    public HPDResult pingInstanceById(String instanceId) {
        //Build a request that searches in the Org tree in the HPD for an org named "Mercy Hospital" 
        log.log(Level.INFO, "Pinging {0}...", instanceId);
        HPDSearchRequest pingRequest = new HPDSearchRequest(instanceId, HPDSearchRequest.HPDSearchScope.OrgsOnly, HPDSearchRequest.HPDSearchMode.Equality, PING_SEARCH_ORG_NAME);
        HPDSearchResult result = search(pingRequest);
        return (HPDResult) result;
    }

    private HPDSearchResult search(HPDSearchRequest searchRequest) {
        //If we have no Directories to search, we return
        if (searchRequest.getDirectoryIds() == null || searchRequest.getDirectoryIds().isEmpty()) {
            return new HPDSearchResult(HPDSearchResult.HPD_NO_DIRECTORY_SPECIFIED_ON_SEARCH, "HPDSearchRequest contained no Directories to search.");
        }
        if (!searchRequest.hasSearchCriteria()) {
            return new HPDSearchResult(HPDSearchResult.HPD_NO_SEARCH_CRITERIA_SPECIFIED, "HPDSearchRequest contained no search criteria.  Cannot execute open ended searches.");
        }
        //TODO:  We'll thread here.  For now do them synchronously, one at a time.
        HPDSearchResult result = new HPDSearchResult();
        
        RoundRobinChain<HPDEntityModel> chain = new RoundRobinChain<HPDEntityModel>();
        
        for (String dirId : searchRequest.getDirectoryIds()) {
            HPDSearchResult searchResult = searchDirectory(dirId, searchRequest);
            result.updateHPDResult(dirId, searchResult);
            
            List<HPDEntityModel> entities = searchResult.getEntities();
            if (entities != null && (! entities.isEmpty())) {
                chain.addIterator(entities.iterator());
            }
        }
        
        List<HPDEntityModel> entities = chain.toList();
        result.setEntities(entities);

        return result;
    }
    
    private ProcessedHPDResponseDoc executeAndParseSearch(HPDInstanceModel hpd, HPDSearchRequest searchRequest) throws Exception {
        Document responseDoc = submitHPDDSMLSearchRequest(hpd, searchRequest);

        return parseResponseDocToHPDEntities(hpd, responseDoc);        
    }
    
    private HPDSearchResult searchDirectory(String instanceId, HPDSearchRequest searchRequest) {
        HPDInstanceModel hpd = getInstanceById(instanceId);
        if (hpd == null) {
            return new HPDSearchResult(HPDResult.HPD_NOT_FOUND_IN_CONFIG_BY_ID, String.format("No HPD instance found for Id '%s'.", instanceId));
        }

        try {
            //PHASE 1 - Search for Entities
            //Build snd submit a DSML search based on our HPDSearchRequest model     
            ProcessedHPDResponseDoc entitySearchResp = executeAndParseSearch(hpd, searchRequest);

            if (entitySearchResp.getTotalEntiesFound()==0) {
                return new HPDSearchResult(HPDResult.HPD_OPERATION_SUCCESS, entitySearchResp);
            }

            if (searchRequest.isProvidersAffiliatedToOrgSearch()) {
                HPDSearchRequest affiliatedProvidersRequest = new HPDSearchRequest(HPDSearchRequest.HPDSearchScope.IndividualProvidersOnly, instanceId);    
                for (HPDOrgToProvRelationshipModel rel : entitySearchResp.getRelationships()) {
                    affiliatedProvidersRequest.addIndividualProviderDN(rel.getHasAProviderDN());
                }
                return new HPDSearchResult(HPDResult.HPD_OPERATION_SUCCESS, executeAndParseSearch(hpd, affiliatedProvidersRequest));                
            }
            
            //Create some maps to assist us when we weave in more content below
            HashMap<String, String> serviceToEntityMap = new HashMap<String, String>();    //Map<ServiceDN, EntityUID>
            HashMap<String, String> serviceToRelMap = new HashMap<String, String>();       //Map<ServiceDN, RelationshipDN>
            List<String> relatedOrgDNs = new ArrayList<String>();
            List<String> relatedServiceDNs = new ArrayList<String>(); 
            List<String> foundOrgs = new ArrayList<String>();

            //PHASE 2a - For all the entities we just got back, we need to get any relationships they participate in
            //This translates to searching ou=Relationship for any hpdHasAProvider                
            HPDSearchRequest relationshipRequest = new HPDSearchRequest(HPDSearchRequest.HPDSearchScope.HasAProviderRelationshipsOnly, instanceId);

            List<String> entityDNs = new ArrayList<String>();
            for (HPDEntityModel entityModel : entitySearchResp.getEntities()) {
                    entityDNs.add(entityModel.getDN());
                    if (!entityModel.getServiceDNs().isEmpty()) {
                        relatedServiceDNs.addAll(entityModel.getServiceDNs());
                        for (String serviceDN : entityModel.getServiceDNs()) {
                            serviceToEntityMap.put(serviceDN.toLowerCase(), entityModel.getEntityUID());
                        }
                    }
                    if (!entityModel.isOrg()) {
                        foundOrgs.add(entityModel.getDN());
                    }
            }                
            relationshipRequest.setRelationshipProviderDNs(entityDNs);

            //Some services we know because they are directly added to the Entity entity themselves

            //Execute the DSML search for the relationships for all the Entities that came back
            ProcessedHPDResponseDoc relationshipSearchEntities = executeAndParseSearch(hpd, relationshipRequest);  


            //Ok, if we got any back, we want to work through them and weave data coming back into the Entities that came back in PHASE 1
            if (!relationshipSearchEntities.getRelationships().isEmpty()) {
                for (HPDOrgToProvRelationshipModel relationship : relationshipSearchEntities.getRelationships()) {
                    if (!relatedOrgDNs.contains(relationship.getHasAnOrgDN())) {
                        relatedOrgDNs.add(relationship.getHasAnOrgDN());
                    }
                    for (String serviceDN : relationship.getHasAServiceDN()) {
                        String normalServiceDN = serviceDN.toLowerCase();
                        if (!relatedServiceDNs.contains(normalServiceDN)) {
                            relatedServiceDNs.add(normalServiceDN);
                        }
                        if (!serviceToEntityMap.containsKey(normalServiceDN)) {
                            serviceToEntityMap.put(normalServiceDN, HPDUtil.getUnqualifiedUIDFromDN(relationship.getHasAProviderDN()));
                        }
                        if (!serviceToRelMap.containsKey(normalServiceDN)) {
                            serviceToRelMap.put(normalServiceDN, relationship.getDN());
                        }
                    }
                    //Add the relationship here to the Provider that is related to the Org
                    entitySearchResp.addRelationshipToEntity(relationship.getRelatedProviderEntityUID(), relationship);
                    //Add contacts that came in from the relatsionship to the contact list
                    for (HPDContactModel relContact : relationship.getContacts()) {
                        entitySearchResp.addContactToEntity(relationship.getRelatedProviderEntityUID(), relContact);
                    }
                }
                //Search for related Orgs and weave that information into the Entity here...     
                if (!relatedOrgDNs.isEmpty()) {
                    //PHASE 2b - For all the entities we just got back, we need to get any orgs that were referenced by the relationship
                    //This translates to searching ou=HCRegulatedOrganization for any UID we referenced                            
                    HPDSearchRequest serviceRequest = new HPDSearchRequest(HPDSearchRequest.HPDSearchScope.OrgsOnly, instanceId); 
                    serviceRequest.setOrganizationDNs(relatedOrgDNs);
                    ProcessedHPDResponseDoc orgSearchEntities = executeAndParseSearch(hpd, serviceRequest); 
                    //If we got orgs back, and we should, we need to  weave those orgs into the relationships that referred to them
                    if (!orgSearchEntities.getEntities().isEmpty()) {
                        for (HPDEntityModel orgModel : orgSearchEntities.getEntities()) {
                            entitySearchResp.addOrgModelToAffiliatedRelationships(orgModel);
                        }
                    }
                }                    
                //Search for related Services here and weave those contacts into the contact list
                if (!relatedServiceDNs.isEmpty()) {
                    //PHASE 2c - For all the relationships we just got back, we need to get any services that were referenced by the relationship
                    //This translates to searching ou=Service for any hpdserviceid                            
                    HPDSearchRequest serviceRequest = new HPDSearchRequest(HPDSearchRequest.HPDSearchScope.ServicesOnly, instanceId); 
                    serviceRequest.setServiceDNs(relatedServiceDNs);
                    ProcessedHPDResponseDoc serviceSearchEntities = executeAndParseSearch(hpd, serviceRequest);
                    if (!serviceSearchEntities.getServices().isEmpty()) {
                        for (HPDElectronicServiceModel service : serviceSearchEntities.getServices()) {
                            String entityId = serviceToEntityMap.get(service.getDN().toLowerCase());
                            if (entityId!=null) {
                                entitySearchResp.addServiceToEntity(entityId, service);
                            }
                            String relationshipId = serviceToRelMap.get(service.getDN().toLowerCase());
                            if (relationshipId!=null) {
                                entitySearchResp.addServiceToRelationship(relationshipId, service);
                            }
                        }
                    }
                }

            }

            //PHASE 3 - Fetch all the hpdCredential entries from the remote LDAP server based on the references from entities returned from PHASE 1
            //Create a map of EntityUID and hpdCredential DN reference to help when we get responses back and map them back to the entity
            HashMap<String, String> entityCredentialsMap = new HashMap<String, String>();
            List<String> credentialDNs = new ArrayList<String>();
            for (HPDBaseModel baseModel : entitySearchResp.getEntities()) {
                if (baseModel instanceof HPDEntityModel) {
                    HPDEntityModel entityModel = (HPDEntityModel) baseModel;

                    for (String credentialDN : entityModel.getCredentialDNs()) {
                        entityCredentialsMap.put(credentialDN, entityModel.getEntityUID());
                        credentialDNs.add(credentialDN);
                    }
                }
            }
            if (!credentialDNs.isEmpty()) {
                //Build a searchQuery to get all the Credentials referenced from all the entities we returned in Phase 1
                HPDSearchRequest credentialRequest = new HPDSearchRequest(HPDSearchRequest.HPDSearchScope.CredentialsOnly, instanceId);
                credentialRequest.setCredentialDNs(credentialDNs);
                //Execute the DSML search for the Credentials for all the Entities that came back in Phase 1                 
                ProcessedHPDResponseDoc credentialSearchEntities = executeAndParseSearch(hpd, credentialRequest);
                //Ok, we should have got a list of credentials back from the remote server...
                //Weave these credentials into the Entities we returned from Phase 1
                if (!credentialSearchEntities.getCredentials().isEmpty()) {
                    for (HPDCredentialModel credential : credentialSearchEntities.getCredentials()) {
                        if (entityCredentialsMap.containsKey(credential.getDN())) {
                            String entityUID = entityCredentialsMap.get(credential.getDN());
                            //Find the entity from Phase 1 with this entityUID and add this credential                                
                            entitySearchResp.addCredentialToEntity(entityUID, credential);
                        }
                    }
                }
            }

            return new HPDSearchResult(HPDResult.HPD_OPERATION_SUCCESS, entitySearchResp);

        } catch (UnexpectedHPDCallException x) {
            return new HPDSearchResult(HPDResult.HPD_UNEXPECTED_HTTP_STATUS_CODE, x.getMessage() + (x.statusCode > -1 ? String.format(", StatusCode=%s", x.statusCode) : ""));
        } catch (MalformedOrInvalidHPDRequestResponse m) {
            return new HPDSearchResult(HPDResult.HPD_MALFORMED_OR_INVALID_DSML_REQUEST, m.getMessage());
        } catch (HPDRequestTimeoutException rte) {
            return new HPDSearchResult(HPDResult.HPD_RESPONSE_TIMEOUT, rte.getMessage());
        } catch (HPDUnknownHostException uhe) {
            return new HPDSearchResult(HPDResult.HPD_UNKNOWN_HOST, uhe.getMessage());
        } catch (ConnectTimeoutException cte) {
            return new HPDSearchResult(HPDResult.HPD_CONNECT_TIMEOUT, cte.getMessage());
        } catch (HPDCertificateException ce) {
            return new HPDSearchResult(HPDResult.HPD_MISSING_OR_INVALID_CERT, ce.getMessage());
        } catch (HPDConnectionRefusedException cr) {
            return new HPDSearchResult(HPDResult.HPD_CONNECTION_REFUSED, cr.getMessage());
        } catch (HPDTLSException tls) {
            return new HPDSearchResult(HPDResult.HPD_TLS_ERROR, tls.getMessage());
        } catch (Exception se) {
            log.severe(se.getMessage());
            return new HPDSearchResult(HPDResult.HPD_RESPONSE_PARSE_ERROR, se.getMessage());
        }
    }

    private ProcessedHPDResponseDoc parseResponseDocToHPDEntities(HPDInstanceModel hpdInstanceModel, Document responseDoc) {

        ProcessedHPDResponseDoc rde = new ProcessedHPDResponseDoc(responseDoc);

        //Ok, the request was sucessful.  We need to process the entities that came back
        NodeList searchResponseNodeList = responseDoc.getElementsByTagNameNS("*", "searchResultEntry");

        //Iterate thru the Entities we found
        for (int i = 0; i < searchResponseNodeList.getLength(); i++) {
            Node node = searchResponseNodeList.item(i);
            String entityDN = node.getAttributes().getNamedItem("dn").getNodeValue();
            log.log(Level.FINE, "Found: {0}", entityDN);
            try {
                //If the node is an Credential, then let's consume that and add that to the our result
                if (entityDN.toLowerCase().contains(Constants.ENTITY_TYPE_CREDENTIAL_RDN_OU.toLowerCase())) {
                    HPDCredentialModel credential = new HPDCredentialModel(hpdInstanceModel, entityDN, node.getChildNodes());
                    log.log(Level.FINE, "Found: new Credential {0}", credential);                    
                    rde.getCredentials().add(credential);
                }
                //If the node is an Individual or Org, then let's consume that and add that to the our result                
                if (entityDN.toLowerCase().contains(Constants.ENTITY_TYPE_INDIVIDUAL_RDN_OU.toLowerCase()) || entityDN.contains(Constants.ENTITY_TYPE_ORG_RDN_OU.toLowerCase())) {
                    HPDEntityModel entity = new HPDEntityModel(hpdInstanceModel, entityDN, node.getChildNodes());
                    log.log(Level.FINE, "Found: new Entity {0}", entity);                           
                    rde.getEntities().add(entity);
                }
                //If the node is an Relationship, then let's consume that and add that to the our result                     
                if (entityDN.toLowerCase().contains(Constants.ENTITY_TYPE_RELATIONSHIP_RDN_OU.toLowerCase())) {
                    HPDOrgToProvRelationshipModel rel = new HPDOrgToProvRelationshipModel(hpdInstanceModel, entityDN, node.getChildNodes());
                    log.log(Level.FINE, "Found: new Relationship {0}", rel);                        
                    rde.getRelationships().add(rel);
                }     
                //If the node is an Relationship, then let's consume that and add that to the our result                
                if (entityDN.toLowerCase().contains(Constants.ENTITY_TYPE_SERVICE_RDN_OU.toLowerCase())) {
                    HPDElectronicServiceModel service = new HPDElectronicServiceModel(hpdInstanceModel, entityDN, node.getChildNodes());
                    log.log(Level.FINE, "Found: new Service {0}", service);                     
                    rde.getServices().add(service);
                }

            } catch (UnexpectedLDAPObjectException lox) {
                log.log(Level.WARNING, lox.getMessage());
            } catch (HPDObjectWithNoUIDException ux) {
                log.log(Level.WARNING, ux.getMessage());
            }
        }
        log.log(Level.INFO, "Found {0} LDAP entries", rde.getTotalEntiesFound());
        return rde;
    }

    private Document submitHPDDSMLSearchRequest(HPDInstanceModel hpd, HPDSearchRequest searchRequest)
            throws NoSuchAlgorithmException, KeyManagementException, MalformedOrInvalidHPDRequestResponse,
            UnexpectedHPDCallException, HPDRequestTimeoutException, HPDUnknownHostException,
            ConnectTimeoutException, HPDCertificateException, HPDConnectionRefusedException,
            HPDTLSException {

        try {
            log.log(Level.INFO, "\n\n" + StringUtils.repeat("=", 120) + "\nSearching HPD\n\tHPD Name: {0}\n\tServiceURL: {1} AuthType: {2}\n\tCriteria: {3}\n\n", new Object[]{hpd.getName(), hpd.getServiceURL(), hpd.getAuthType(), searchRequest.getSearchSummary()});

            DefaultHttpClient client = null;
            HttpParams httpParams = new BasicHttpParams();

            //Set the timeout... the amount of time we'll wait for a connection
            httpParams.setIntParameter("http.connection.timeout", 5000);

            //Set how long we'll wait for a response to our query once connected
            //First we try the value that set for this specific directory and if that's not set, we use the default
            Integer timeoutMS = hpd.getRequestTimeoutMS();
            if (timeoutMS == null || timeoutMS == 0) {
                timeoutMS = config.getDefaultRequestTimeoutMS();
            }
            httpParams.setIntParameter("http.socket.timeout", timeoutMS);

            //Create our POST method based on the Service URL for the HPD
            HttpPost postMethod = new HttpPost(hpd.getServiceURL());
            //It's SOAP 1.2
            postMethod.addHeader("Content-Type", "application/soap+xml;charset=utf-8");

            //Ok, based on our authorization style, we need to configure the client and method
            switch (hpd.getAuthType()) {
                //If the AuthType is Basic, then we want to supply the id and password                
                case Constants.PROVIDER_DIR_AUTH_TYPE_BASIC:
                    String authStr = hpd.getUsername() + ":" + hpd.getPassword();
                    String encodedPassword = new String(new Base64().encode(authStr.getBytes()));
                    postMethod.addHeader("Authorization", "Basic " + encodedPassword.trim());
                    client = new DefaultHttpClient(httpParams);
                    break;
                case Constants.PROVIDER_DIR_AUTH_TYPE_TLS:
                    KeyStore keystore = KeyStore.getInstance("pkcs12");
                    InputStream keystoreInput = new FileInputStream(config.getHPDKeyStorePath());
                    String keystorePassword = "changeit";
                    keystore.load(keystoreInput, keystorePassword.toCharArray());

                    KeyStore truststore = KeyStore.getInstance("jks");
                    InputStream truststoreInput = new FileInputStream(config.getHPDTrustStorePath());
                    truststore.load(truststoreInput, "changeit".toCharArray());

                    SchemeRegistry schemeRegistry = new SchemeRegistry();
                    SSLSocketFactory sf = new SSLSocketFactory(keystore, keystorePassword, truststore);
                    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    schemeRegistry.register(new Scheme("https", sf, 443));

                    client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
                    break;

                case Constants.PROVIDER_DIR_AUTH_TYPE_NONE:
                    client = new DefaultHttpClient(httpParams);                    
                    break;
                default:
                    throw new RuntimeException("Unexpected HPD Authorization Type " + hpd.getAuthType());
            }


            String dsmlSearchRequest = searchRequest.toDSML(hpd, config);
            postMethod.setEntity(new StringEntity(dsmlSearchRequest));
            log.log(Level.FINE, "DSML SearchRequest to HPD: {0}", dsmlSearchRequest);
            //Execute the POST to the remove gateway
            HttpResponse postResp = client.execute(postMethod);
            int statusCode = postResp.getStatusLine().getStatusCode();
            //What did we get back??
            log.log(Level.FINE, "\tHTTP statusCode={0}", statusCode);
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    break;
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    throw new UnexpectedHPDCallException("HPD call failed due to a ServiceUnavailable response. (HTTP-503).", statusCode);
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new UnexpectedHPDCallException("HPD call failed due to missing or invalid authorization credentials. (HTTP-401).", statusCode);
                case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
                    throw new UnexpectedHPDCallException("HPD call failed Unsupported Media Type response. (HTTP-415).", statusCode);
                default:
                    System.out.println("###############################" + statusCode);
                    System.out.println("\n\n" + dsmlSearchRequest + "\n\n");
                    throw new UnexpectedHPDCallException("HPD call failed due to an unexpected HTTP statusCode. (HTTP-" + statusCode + ").", statusCode);
            }
            //get the response that came back from the remote HPD instance and do some cleanup on it if necessary
            String batchResponse = getNormalizedBatchResponse(EntityUtils.toString(postResp.getEntity()));
            log.log(Level.FINE, "DSML Response:" + batchResponse);
            
            //Now, DOM what came back and make sure we got some search results back
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document responseDoc = docBuilder.parse(new InputSource(new StringReader(batchResponse)));

            //Did the DSML request fail withe an errorResponse due to formatting, structure, etc issues
            NodeList errorResponseNodeList = responseDoc.getElementsByTagName(Constants.DSML_ERROR_RESPONSE_RESULT);
            if (errorResponseNodeList != null && errorResponseNodeList.getLength() > 0) {
                String errorType = errorResponseNodeList.item(0).getAttributes().getNamedItem("type").getTextContent();
                if (errorType != null && errorType.equals(Constants.DSML_REQUEST_MALFORMED_RESULT)) {
                    throw new MalformedOrInvalidHPDRequestResponse("The DSML request that was submitted was malformed.");
                } else {
                    throw new MalformedOrInvalidHPDRequestResponse("Unexpected DSML error type encountered.  Type=" + errorType);
                }
            }
            return responseDoc;
        } catch (UnrecoverableKeyException uke) {
            throw new HPDTLSException("Error configuring TLS for HPD Call: " + uke.getMessage());
        } catch (KeyStoreException kse) {
            throw new HPDTLSException("Error configuring TLS for HPD Call: " + kse.getMessage());
        } catch (CertificateException ce) {
            throw new HPDTLSException("Error configuring TLS for HPD Call: " + ce.getMessage());
        } catch (DOMException de) {
            throw new MalformedOrInvalidHPDRequestResponse("Error processing HPD response: " + de.getMessage());
        } catch (SAXException se) {
            throw new MalformedOrInvalidHPDRequestResponse("Error processing HPD response: " + se.getMessage());
        } catch (ParserConfigurationException pce) {
            throw new MalformedOrInvalidHPDRequestResponse("Error processing HPD response: " + pce.getMessage());
        } catch (SocketTimeoutException ste) {
            throw new HPDRequestTimeoutException("Timeout connecting to HPD instance " + hpd.getId());
        } catch (UnknownHostException uhe) {
            throw new HPDUnknownHostException("Unknown Host attempting to connect to server " + hpd.getId() + " at endpoint " + hpd.getServiceURL());
        } catch (ConnectException ce) {
            throw new HPDConnectionRefusedException("Connection refused to server " + hpd.getId() + " at endpoint " + hpd.getServiceURL());
        } catch (IOException io) {
            if (io.getCause() instanceof ValidatorException) {
                throw new HPDCertificateException("Remote HTTPS Cerficate issue: + " + io.getMessage());
            }
            throw new UnexpectedHPDCallException(io.getMessage(), -1);
        } catch (RuntimeException re) {
            throw new UnexpectedHPDCallException(re.getMessage(), -1);
        }
    }

    private String getNormalizedBatchResponse(String rawDSMLResponse) {
        log.log(Level.FINE, "Raw Batch Response \n{0}\n", rawDSMLResponse);

        //HACK WARNING: If we get responses back where the knuckleheads are saying dsml:attribute versus dsml:attr, let's correct that
        //Standardize on a couple other things as well
        rawDSMLResponse = rawDSMLResponse.replaceAll("<dsml:attribute", "<attr");
        rawDSMLResponse = rawDSMLResponse.replaceAll("</dsml:attribute", "</attr");
        rawDSMLResponse = rawDSMLResponse.replaceAll("<attribute", "<attr");
        rawDSMLResponse = rawDSMLResponse.replaceAll("</attribute", "</attr");

        rawDSMLResponse = rawDSMLResponse.replaceAll("<dsml:", "<");
        rawDSMLResponse = rawDSMLResponse.replaceAll("</dsml:", "</");

        //HACK: If we received a <soap-env> wrapper around the response, strip it off.  Need better way to do this
        //Later we can DOM the doc and grab the inner response and then turn that back into the batch response
      if (rawDSMLResponse.toLowerCase().contains("<soap-env") || rawDSMLResponse.toLowerCase().contains("soap-envelope")) {
            if (rawDSMLResponse.indexOf("<batchResponse") > 0) {
                rawDSMLResponse = rawDSMLResponse.substring(rawDSMLResponse.indexOf("<batchResponse"), rawDSMLResponse.indexOf("</batchResponse>") + 16);
            }
        }
        log.log(Level.FINE, "Normalized Batch Response \n{0}\n", rawDSMLResponse);
        return rawDSMLResponse;
    }
}
