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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public class HPDSearchRequest implements Serializable {
    private static final long serialVersionUID = 1481871938253659262L;

    final static Logger log = Logger.getLogger(HPDClient.class.getName());

    public enum HPDSearchScope {
        AllEntities,                    //Typically used when doing a search by name regardless of Org or Provider Type
        OrgsOnly,                       //Used to scope a given search to only Organizations
        IndividualProvidersOnly,        //Used to scope a given search to only Providers
        CredentialsOnly,                //A special mode: used to search for a list of credentials
        HasAProviderRelationshipsOnly,  //A special mode: used to search for the relationships for a given Provider based on that Providers DN
        HasAnOrgRelationshipsOnly,      //A special mode: used to search for the relationships for a given Org based on that Orgs DN
        ServicesOnly,                   //A special mode: used to return a set of Services entries based on a list of DNs
        ProvidersAffiliatedToOrg        //A special mode: used to return entries for Providers affiliated to a particular Org based on that Orgs qualified UID
    };

    //When searching attributes, we typically need a mode to indicate to LDAP on how to do comparisons with the filter data we pass
    public enum HPDSearchMode {Equality, Contains, StartsWith, Exists, NotExists};

    public enum HPDServiceProfile {DirectProjectSMTP, MLLP_HL7_V2, XDS_DOCUMENTREGISTRY_PROVIDE_AND_REGISTER};  //More to come later 

    //This is the body of the DSML search request.  Essentially a template used to wrap the innards of the request formed
    //based on the criteria passed in the search filter
    private final static String searchRequestBody =
            "<searchRequest dn='%s%s' scope='%s' derefAliases='derefFindingBaseObj' requestID='%s'> "
                    + "   <filter>  "
                    + "        %s"
                    + "   </filter>  "
                    + "</searchRequest>";

    private HPDSearchScope searchScope;             //Which Entities do we include in our search.
    private List<String> directoryIds;              //Which Directories do we search.  Defaults to the Local Directory.
    public int resultSizeLimit;                     //How many Providers will we return max

    private String uid;                             //For searches by the 'uid' attribute.  The UID for HPD is by standard comprised of the authorityId + ":" + entityUID
    private HPDSearchMode nameSearchMode;           //For Searches by Name, what mode will we use.  This mode will be applied regardless of whether
    //we are saerching by AllEntities, Orgs or Individuals
    private String nameSearchText;                  //Value used when searching for Orgs or AllEntities
    private String lastName;                        //Value used when searching by IndividualProvidersOnly
    private String firstName;                       //Value used when searching by IndividualProvidersOnly
    private String npi;                             //Used to query/constrain the 'hcIdentifier' attribute.  the 'npi' value is prefixed by '2.16.840.1.113883.4.6:NPI:'
    private String orgId;                           //Used to query/constrain the 'hcIdentifier' attribute.  It will search loosely based on any orgId.'
    private String registeredName;                  //Used to query/constrain the 'hcRegisteredName' attribute.  It will search loosely based on any registeredName.'
    private String description;                     //Used to query/constrain the 'description' attribute.  It will search based on description.'
    private String gender;                          //Used to query/constrain the 'gender' attribute.
    private String languageCode;                    //Used to query/constrain the 'hpdProviderLanguageSupported' attribute
    private String addressSearchText;               //Used to query/constrain the 'hpdProviderPracticeAddress' and 'hpdProviderMailingAddress' and 'hpdProviderBillingAddress' attributes
    private String addressSearchText2;               //Used to query/constrain the 'hpdProviderPracticeAddress' and 'hpdProviderMailingAddress' and 'hpdProviderBillingAddress' attributes
    private String postalCodeText;                  //Used to query/constrain the 'postalCode=' substring of the 'hpdProviderPracticeAddress' and 'hpdProviderMailingAddress' and 'hpdProviderBillingAddress' attributes
    private String stateOrProvinceName;             //Used to query/constrain the 'stateOrProvinceName' attribute.
    private String specializationText;              //Used to query/constrain the 'hcSpecialzation' attribute.  The contents of specializationText must appear somewhere in the attribute value.
    private String specializationCode;              //Used to query/constrain the 'hcSpecialzation' attribute.  
    private String faxNumber;                       //Used to query/constrain the 'facsimileTelephoneNumber' attribute
    private String telephoneNumber;                 //Used to query/constrain the 'telephoneNumber' attribute
    private String emailAddress;                    //Used to query/constrain the 'email' attribute.

    private String serviceAddressText;              //Used to constrain the LDAP query to entries by the 'hpdMedicalRecordsDeliveryEmailAddress' attribute
    private HPDSearchMode serviceSearchMode;        //How should we search for Service Entries (like Direct Address).  This mode allows for searching for Exists and not Exists
    private HPDServiceProfile serviceSearchProfile; //Which type of Address will we be searching for.  For now, we're constrain the query to the hpdMedicalRecordsDeliveryEmailAddress attribute and the DirectProjectSMTP profile

    //EXCLUSIVE Criteria:  The search criteria below can only be used by themselves individually and not in conjunction with any of the critiera above.
    private List<String> serviceDNs;
    private List<String> credentialDNs;             //Used to constrain the search to return a set of HPDCredential entries based on having a hpdcredentialid in the set of DNs in this list.  
    //We take the first token of the DN to do an attribute search

    private List<String> relationshipProviderDNs;   //Used to constrain the search to return a set of HPDProviderMembership entries based on having a hpdHasAProvider in this set of DNs
    private List<String> relationshipOrgDNs;        //Used to constrain the search to return a set of HPDProviderMembership entries based on having a hpdHasAnOrg in this set of DNs    
    private List<String> organizationDNs;           //Used to constrain the search to return a set of hcRegulatedOrganization entries based on having a uid attribute in this set of DNs (pulling the UID from the DN of course)
    private List<String> individualProviderDNs;      //Used to constrain the search to return a set of HCProfessional entries based on having a uid attribute in this set of DNs (pulling the UID from the DN)


    public HPDSearchRequest() {
        init();
    }

    public HPDSearchRequest(HPDSearchScope searchScope, String directoryId) {
        init();
        getDirectoryIds().add(directoryId);
        this.searchScope = searchScope;
    }

    public HPDSearchRequest(String directoryId, HPDSearchScope searchScope, HPDSearchMode nameSearchMode, String nameSearchText) {
        init();
        getDirectoryIds().add(directoryId);

        this.searchScope = searchScope;
        this.nameSearchMode = nameSearchMode;
        this.nameSearchText = nameSearchText;
    }

    public final void init() {
        searchScope = HPDSearchScope.AllEntities;
        nameSearchMode = HPDSearchMode.Contains;
        resultSizeLimit = 10;
        serviceSearchMode = HPDSearchMode.Equality;
        setServiceSearchProfile(HPDServiceProfile.DirectProjectSMTP);
    }

    public String getSearchSummary() {
        String summary = String.format("Scope=%s ",searchScope);
        if (isGeneralEntitySearch()) {
            if (HPDUtil.isNotBlank(uid)) {
                summary = summary.concat(String.format(", uid=%s",uid));
            }
            summary = summary.concat(String.format(", nameSearchMode=%s, nameSearchText=%s", nameSearchMode, nameSearchText));
            if (HPDUtil.isNotBlank(lastName)) {
                summary = summary.concat(String.format(", lastName=%s",lastName));
            }
            if (HPDUtil.isNotBlank(firstName)) {
                summary = summary.concat(String.format(", firstName=%s",firstName));
            }
            if (HPDUtil.isNotBlank(npi)) {
                summary = summary.concat(String.format(", npi=%s",npi));
            }
            if (HPDUtil.isNotBlank(orgId)) {
                summary = summary.concat(String.format(", orgId=%s",orgId));
            }
            if (HPDUtil.isNotBlank(registeredName)) {
                summary = summary.concat(String.format(", registeredName=%s",registeredName));
            }
            if (HPDUtil.isNotBlank(description)) {
                summary = summary.concat(String.format(", description=%s",description));
            }
            if (HPDUtil.isNotBlank(gender)) {
                summary = summary.concat(String.format(", gender=%s",gender));
            }
            if (HPDUtil.isNotBlank(specializationText)) {
                summary = summary.concat(String.format(", specializationText=%s",specializationText));
            }
            if (HPDUtil.isNotBlank(specializationCode)) {
                summary = summary.concat(String.format(", specializationCode=%s",specializationCode));
            }
            if (HPDUtil.isNotBlank(emailAddress)) {
                summary = summary.concat(String.format(", emailAddress=%s",emailAddress));
            }
            if (HPDUtil.isNotBlank(addressSearchText)) {
                summary = summary.concat(String.format(", addressSearchText=%s",addressSearchText));
            }
            if (HPDUtil.isNotBlank(addressSearchText2)) {
                summary = summary.concat(String.format(", addressSearchText2=%s",addressSearchText2));
            }
            if (HPDUtil.isNotBlank(languageCode)) {
                summary = summary.concat(String.format(", languageCode=%s",languageCode));
            }
            if (HPDUtil.isNotBlank(faxNumber)) {
                summary = summary.concat(String.format(", faxNumber=%s",HPDUtil.toDSMLTelephone(faxNumber)));
            }
            if (HPDUtil.isNotBlank(telephoneNumber)) {
                summary = summary.concat(String.format(", telephoneNumber=%s",HPDUtil.toDSMLTelephone(telephoneNumber)));
            }
            if (HPDUtil.isNotBlank(postalCodeText)) {
                summary = summary.concat(String.format(", postalCodeText=%s",postalCodeText));
            }
            if (HPDUtil.isNotBlank(stateOrProvinceName)) {
                summary = summary.concat(String.format(", stateOrProvinceName=%s",stateOrProvinceName));
            }
            if (HPDUtil.isNotBlank(serviceAddressText)) {
                summary = summary.concat(String.format(", serviceSearchMode=%s, serviceSearchProfile=%s, serviceAddressText=%s", serviceSearchMode, serviceSearchProfile, serviceAddressText));
            }
        } else {
            summary = summary.concat(dumpDNList("credentialDN", getCredentialDNs()));
            summary = summary.concat(dumpDNList("relationshipProviderDN", getRelationshipProviderDNs()));
            summary = summary.concat(dumpDNList("relationshipOrgDN", getRelationshipOrgDNs()));
            summary = summary.concat(dumpDNList("serviceDN", getServiceDNs()));
            summary = summary.concat(dumpDNList("organizationDN", getOrganizationDNs()));
            summary = summary.concat(dumpDNList("individualProviderDN", getIndividualProviderDNs()));
        }
        return summary;
    }

    public boolean hasSearchCriteria() {
        return HPDUtil.isNotBlank(uid) || HPDUtil.isNotBlank(nameSearchText) || HPDUtil.isNotBlank(npi) || HPDUtil.isNotBlank(orgId) || HPDUtil.isNotBlank(registeredName) || HPDUtil.isNotBlank(specializationText) ||
                HPDUtil.isNotBlank(specializationCode) || HPDUtil.isNotBlank(gender) || HPDUtil.isNotBlank(lastName) || HPDUtil.isNotBlank(firstName) ||
                HPDUtil.isNotBlank(emailAddress) || HPDUtil.isNotBlank(serviceAddressText) || HPDUtil.isNotBlank(addressSearchText) || HPDUtil.isNotBlank(addressSearchText2) ||
                HPDUtil.isNotBlank(faxNumber) || HPDUtil.isNotBlank(languageCode) || HPDUtil.isNotBlank(telephoneNumber) || HPDUtil.isNotBlank(postalCodeText) ||
                HPDUtil.isNotBlank(stateOrProvinceName) || HPDUtil.isNotBlank(serviceAddressText) || HPDUtil.isNotBlank(description) ||
                (!getCredentialDNs().isEmpty()) ||
                (!getRelationshipProviderDNs().isEmpty()) ||
                (!getRelationshipOrgDNs().isEmpty()) ||
                (!getServiceDNs().isEmpty()) ||
                (!getOrganizationDNs().isEmpty()) ||
                (serviceSearchMode==HPDSearchMode.NotExists || serviceSearchMode==HPDSearchMode.Exists);
    }

    public String toDSMLSearchString(HPDInstanceModel hpdInstance) {
        List<String> filterElements = new ArrayList<String>();

        //If we're searching by UID (simple Org or Provider search) and not as part of an affilatedProvbider Search, go for it...
        if (getUID()!=null && searchScope!=HPDSearchScope.ProvidersAffiliatedToOrg) {
            filterElements.add(buildSearchElement(HPDSearchRequest.HPDSearchMode.Equality, "uid", HPDUtil.buildQualifiedUID(hpdInstance, getUID())));
        }

        if (getNameSearchMode() != null && HPDUtil.isNotBlank(getNameSearchText())) {
            String lastNameElem = buildSearchElement(getNameSearchMode(), "sn", (StringUtils.isBlank(lastName) ? nameSearchText : lastName));
            String firstNameElem = buildSearchElement(getNameSearchMode(), "givenName", (StringUtils.isBlank(lastName) ? nameSearchText : firstName));
            String displayNameElem = buildSearchElement(getNameSearchMode(), "displayName", (StringUtils.isBlank(nameSearchText) ? lastName : nameSearchText));
            String orgNameElem = buildSearchElement(getNameSearchMode(), "o", nameSearchText);
            switch (getSearchScope()) {
                case IndividualProvidersOnly:
                    filterElements.add("<or>\n" + lastNameElem + "\n" + firstNameElem + "\n" + displayNameElem + "\n</or>\n");
                    break;
                case OrgsOnly:
                    filterElements.add("<or>\n" + orgNameElem + "\n" + displayNameElem + "\n</or>\n");
                    break;
                case AllEntities:
                    filterElements.add("<or>\n" + displayNameElem + "\n" + orgNameElem + "\n" + lastNameElem + "\n</or>\n");
                    break;
            }
        }
        if (HPDUtil.isBlank(getNameSearchText()) && HPDUtil.isNotBlank(getLastName())) {
            if (getNameSearchMode()==null) {
                setNameSearchMode(HPDSearchMode.Contains);
            }
            filterElements.add(buildSearchElement(getNameSearchMode(), "sn", getLastName()));
        }
        if (HPDUtil.isBlank(getNameSearchText()) && HPDUtil.isNotBlank(getFirstName())) {
            if (getNameSearchMode()==null) {
                setNameSearchMode(HPDSearchMode.Contains);
            }
            filterElements.add(buildSearchElement(getNameSearchMode(), "givenName", getFirstName()));
        }
        if (HPDUtil.isNotBlank(getNpi())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Equality, "hcIdentifier", HPDSearchRequest.buildQualifiedNPIQuery(getNpi())));
        }
        if (HPDUtil.isNotBlank(getOrgId())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "hcIdentifier", getOrgId()));
        }
        if (HPDUtil.isNotBlank(getRegisteredName())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "hcRegisteredName", getRegisteredName()));
        }
        if (HPDUtil.isNotBlank(getDescription())) {
            filterElements.add(buildSearchElement(getNameSearchMode(), "description", getDescription()));
        }
        if (HPDUtil.isNotBlank(getGender())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Equality, "gender", getGender()));
        }
        if (HPDUtil.isNotBlank(getLanguageCode())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Equality, "hpdProviderLanguageSupported", getLanguageCode()));
        }
        if (HPDUtil.isNotBlank(getEmailAddress())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "mail", getEmailAddress().toLowerCase()));
        }
        if (HPDUtil.isNotBlank(getSpecializationCode())) {
            //NOTE:  This is doing a contains for the taxonomy code in the 'hcSpecialization' field.  Since it can be a structured attribute, this'll do for now
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "hcSpecialization", getSpecializationCode()));
        }
        if (HPDUtil.isNotBlank(getSpecializationText())) {
            //NOTE:  This is doing a contains for some text like 'Chiropractor' in the 'hcSpecialization' field.  Since it can be a structured attribute, this'll do for now
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "hcSpecialization", getSpecializationText()));
        }
        if (HPDUtil.isNotBlank(getFaxNumber())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "facsimileTelephoneNumber", getFaxNumber()));
        }
        if (HPDUtil.isNotBlank(getTelephoneNumber())) {
            filterElements.add(buildSearchElement(HPDSearchMode.Contains, "telephoneNumber", getTelephoneNumber()));
        }

        //For Direct Address searching, at this point we need a partial address or an attempt to search for people Having or Not Having Addresses
        if (HPDUtil.isNotBlank(serviceAddressText) || serviceSearchMode.equals(HPDSearchMode.Exists) || serviceSearchMode.equals(HPDSearchMode.NotExists)) {
            switch (serviceSearchProfile) {
                case DirectProjectSMTP:
                    filterElements.add(buildSearchElement(serviceSearchMode, "hpdMedicalRecordsDeliveryEmailAddress", serviceAddressText));
                    break;
                default:
                    throw new RuntimeException("Unsupported serviceSearchProfile " + serviceSearchProfile.toString() + ".  Only DirectProjectSMTP is supported at this time.");
            }
        }

        if (HPDUtil.isNotBlank(getPostalCodeText())) {
            String postalCodeAttr = buildSearchElement(HPDSearchMode.StartsWith, "postalCode", getPostalCodeText());
            String mPostal = buildSearchElement(HPDSearchMode.Contains, "hpdProviderMailingAddress", HPDSearchRequest.buildQualifiedPostalCodeQuery(getPostalCodeText()));
            String bPostal = buildSearchElement(HPDSearchMode.Contains, "hpdProviderBillingAddress", HPDSearchRequest.buildQualifiedPostalCodeQuery(getPostalCodeText()));
            String pPostal = buildSearchElement(HPDSearchMode.Contains, "hpdProviderPracticeAddress", HPDSearchRequest.buildQualifiedPostalCodeQuery(getPostalCodeText()));
            filterElements.add("<or>\n" + postalCodeAttr + "\n" + mPostal + "\n" + pPostal + "\n" + bPostal + "\n</or>\n");
        }
        if (HPDUtil.isNotBlank(getStateOrProvinceName())) {
            String stateOrProvinceAttr = buildSearchElement(HPDSearchMode.Equality, "stateOrProvinceName", getStateOrProvinceName());
            String mPostal = buildSearchElement(HPDSearchMode.Contains, "hpdProviderMailingAddress", HPDSearchRequest.buildQualifiedStateQuery(getStateOrProvinceName()));
            String bPostal = buildSearchElement(HPDSearchMode.Contains, "hpdProviderBillingAddress", HPDSearchRequest.buildQualifiedStateQuery(getStateOrProvinceName()));
            String pPostal = buildSearchElement(HPDSearchMode.Contains, "hpdProviderPracticeAddress", HPDSearchRequest.buildQualifiedStateQuery(getStateOrProvinceName()));
            filterElements.add("<or>\n" + stateOrProvinceAttr + "\n" + mPostal + "\n" + pPostal + "\n" + bPostal + "\n</or>\n");
        }
        if (HPDUtil.isNotBlank(getAddressSearchText()) || HPDUtil.isNotBlank(getAddressSearchText2())) {
            String addressSearch = "";
            String postalAddressAttr = buildSearchElement(HPDSearchMode.Contains, "postalAddress", getAddressSearchText());
            String mAddr = buildSearchElement(HPDSearchMode.Contains, "hpdProviderMailingAddress", getAddressSearchText());
            String bAddr = buildSearchElement(HPDSearchMode.Contains, "hpdProviderBillingAddress", getAddressSearchText());
            String pAddr = buildSearchElement(HPDSearchMode.Contains, "hpdProviderPracticeAddress", getAddressSearchText());
            addressSearch = "<or>\n" + postalAddressAttr + "\n" + mAddr + "\n" + pAddr + "\n" + bAddr + "\n";

            if(HPDUtil.isNotBlank(getAddressSearchText2()))
            {
                String postalAddressAttr2 = buildSearchElement(HPDSearchMode.Contains, "postalAddress", getAddressSearchText2());
                String mAddr2 = buildSearchElement(HPDSearchMode.Contains, "hpdProviderMailingAddress", getAddressSearchText2());
                String bAddr2 = buildSearchElement(HPDSearchMode.Contains, "hpdProviderBillingAddress", getAddressSearchText2());
                String pAddr2 = buildSearchElement(HPDSearchMode.Contains, "hpdProviderPracticeAddress", getAddressSearchText2());
                addressSearch = addressSearch + "\n" + postalAddressAttr2 + "\n" + mAddr2 + "\n" + pAddr2 + "\n" + bAddr2 + "\n";
            }

            filterElements.add(addressSearch + "\n</or>\n");
        }

        //Handle credential searches -- should only be a simple credential search currently by a list of credential Ids
        //Later, this is probably where we'll support searching by different Credential Criteria, unless that
        //is <and>'d with the other criteria above on a Org or Provider Search
        //Build up a big <or> expression based on all the CredentialIds we've been asked to pull back
        if (searchScope==HPDSearchScope.CredentialsOnly) {
            List<String> credContraintElements = new ArrayList<String>();
            for (String credentialDN : getCredentialDNs()) {
                //DN attributes for an Individual Provider come in this form:  hpdCredential | hpdCredentialId=1833,ou=HPDCredential,dc=hpd,dc=mirth,dc=com
                //We want to get the 1833 from above
                String credentialActualId = HPDUtil.getIdFromDN(credentialDN);
                credContraintElements.add(buildSearchElement(HPDSearchMode.Equality, "hpdCredentialId", credentialActualId));
            }
            if (credContraintElements.size()>0) {
                String credentialDSMLConstraintStr = "";
                for (String constraintElement : credContraintElements) {
                    credentialDSMLConstraintStr = credentialDSMLConstraintStr.concat(constraintElement) + "\n";
                }
                filterElements.add("<or>\n" + credentialDSMLConstraintStr + "\n" + "</or>\n");
            }
        }

        //Handle HPD Relationship searches by Provider -- should only be a simple relationship search currently by a list of hpdHasAProvider DNs
        //These DNs will have likely come from a prior search for Providers and this aspect of the overall search is to 'weave' into those
        //results the relationships tohse Providers have to Orgs
        if (searchScope==HPDSearchScope.HasAProviderRelationshipsOnly) {
            List<String> relContraintElements = new ArrayList<String>();
            for (String relationshipProviderDN : getRelationshipProviderDNs()) {
                //DN attributes for an hpdHasAProvider attribute come in this form:  hpdHasAProvider | uid=XXX:YYY,ou=Relationship,dc=hpd,dc=mirth,dc=com
                //We want to return the entire HPDProviderMembership entry where we match the DN 'value' that is associated with the hpdHasAProvider attribute
                relContraintElements.add(buildSearchElement(HPDSearchMode.Equality, "hpdHasAProvider", relationshipProviderDN));
            }
            if (relContraintElements.size()>0) {
                String relationshipProviderDSMLConstraintStr = "";
                for (String constraintElement : relContraintElements) {
                    relationshipProviderDSMLConstraintStr = relationshipProviderDSMLConstraintStr.concat(constraintElement) + "\n";
                }
                filterElements.add("<or>\n" + relationshipProviderDSMLConstraintStr + "\n" + "</or>\n");
            } else {
                throw new RuntimeException("Expected RelationshipProviderDNs to search on RelationshipsOnly Mode!");
            }
        }

        //Handle HPD Relationship searches by Org -- should only be a simple relationship search currently by a list of Org DNs
        //These DNs will have likely come from a prior search for Orgs and this aspect of the overall search is to 'weave' into those
        //results the relationships those Providers have to Orgs
        if (searchScope==HPDSearchScope.HasAnOrgRelationshipsOnly || searchScope==HPDSearchScope.ProvidersAffiliatedToOrg) {
            if (getUID()!=null) {
                //We're doing a affiliatedOrg Search.  We need to add a RelationshipOrgDNs entry
                addRelationshipOrgDN(HPDUtil.buildQualifiedDN(hpdInstance,getUID(), Constants.ENTITY_TYPE_ORG));
            }
            List<String> relContraintElements = new ArrayList<String>();
            for (String relationshipOrgDN : getRelationshipOrgDNs()) {
                //DN attributes for an hpdHasAnOrg attribute come in this form:  hpdHasAnOrg | uid=AuthorityID:EntityID,ou=HCRegulatedOrganization,dc=hpd,dc=mirth,dc=com
                //We want to return the entire HPDProviderMembership entry where we match the DN 'value' that is associated with the hpdHasAnOrg attribute
                relContraintElements.add(buildSearchElement(HPDSearchMode.Equality, "hpdHasAnOrg", relationshipOrgDN));
            }
            if (relContraintElements.size()>0) {
                String relationshipOrgDSMLConstraintStr = "";
                for (String constraintElement : relContraintElements) {
                    relationshipOrgDSMLConstraintStr = relationshipOrgDSMLConstraintStr.concat(constraintElement) + "\n";
                }
                filterElements.add("<or>\n" + relationshipOrgDSMLConstraintStr + "\n" + "</or>\n");
            } else {
                throw new RuntimeException("Expected getRelationshipOrgDNs to search on RelationshipsOnly Mode!");
            }
        }

        //Handle HPD Relationship searches by Provider -- should only be a simple relationship search currently by a list of hpdHasAProvider DNs
        //These DNs will have likely come from a prior search for Providers and this aspect of the overall search is to 'weave' into those
        //results the relationships tohse Providers have to Orgs
        if (searchScope==HPDSearchScope.ServicesOnly) {
            List<String> serviceContraintElements = new ArrayList<String>();
            for (String serviceDN : getServiceDNs()) {
                //DN attributes for a uid attribute come in this form:  hpdserviceid=xxx
                //We want to return the entire HPDElectronicService entry where we match the 'value' that is associated with the hpdserviceid attribute
                serviceContraintElements.add(buildSearchElement(HPDSearchMode.Equality, "hpdserviceid", HPDUtil.getIdFromDN(serviceDN)));
            }
            if (serviceContraintElements.size()>0) {
                String serviceDSMLConstraintStr = "";
                for (String constraintElement : serviceContraintElements) {
                    serviceDSMLConstraintStr = serviceDSMLConstraintStr.concat(constraintElement) + "\n";
                }
                filterElements.add("<or>\n" + serviceDSMLConstraintStr + "\n" + "</or>\n");
            } else {
                throw new RuntimeException("Expected ServiceDNs to search on ServicesOnly Mode!");
            }

        }

        //Handle HPD Org searches by DN -- should only be a simple Org search currently by a list of Organization DNs from which we grab the UID portion
        //These DNs will have likely come from a prior search for Providers and this aspect of the overall search is to 'weave' into those
        //results the Orgs those Providers are affiliated with
        if (searchScope==HPDSearchScope.OrgsOnly && !getOrganizationDNs().isEmpty()) {
            List<String> orgContraintElement = new ArrayList<String>();
            for (String orgDN : getOrganizationDNs()) {
                //values for the UID attribute come in this form:  uid=xxx:yyyy
                //We want to return the entire hcRegulatedOrganization entry where we match the 'value' that is associated with the uid attribute
                orgContraintElement.add(buildSearchElement(HPDSearchMode.Equality, "uid", HPDUtil.getQualifiedUIDFromDN(orgDN)));
            }
            if (orgContraintElement.size()>0) {
                String orgDSMLConstraintStr = "";
                for (String constraintElement : orgContraintElement) {
                    orgDSMLConstraintStr = orgDSMLConstraintStr.concat(constraintElement) + "\n";
                }
                filterElements.add("<or>\n" + orgDSMLConstraintStr + "\n" + "</or>\n");
            } else {
                throw new RuntimeException("Expected OrgDNs to search on OrgsOnly Mode!");
            }

        }

        //Handle HPD Provider searches by DN -- should only be a simple Provider search currently by a list of Provider DNs from which we grab the UID portion
        if (searchScope==HPDSearchScope.IndividualProvidersOnly && !getIndividualProviderDNs().isEmpty()) {
            List<String> providerContraintElement = new ArrayList<String>();
            for (String providerDN : getIndividualProviderDNs()) {
                //values for the UID attribute come in this form:  uid=xxx:yyyy
                //We want to return the entire HCProfessional entry where we match the 'value' that is associated with the uid attribute
                providerContraintElement.add(buildSearchElement(HPDSearchMode.Equality, "uid", HPDUtil.getQualifiedUIDFromDN(providerDN)));
            }
            if (providerContraintElement.size()>0) {
                String providerDSMLConstraintStr = "";
                for (String constraintElement : providerContraintElement) {
                    providerDSMLConstraintStr = providerDSMLConstraintStr.concat(constraintElement) + "\n";
                }
                filterElements.add("<or>\n" + providerDSMLConstraintStr + "\n" + "</or>\n");
            } else {
                throw new RuntimeException("Expected ProviderDNs to search on IndividualProvidersOnly Mode!");
            }

        }

        boolean addAnd = true;
        for (String filterElement : filterElements) {
            if(filterElement.indexOf("hpdProviderPracticeAddress") > 0)
                addAnd = false;
        }



        boolean doingAnd = filterElements.size() > 1;
        String filterRequest = new String();
        if (doingAnd && addAnd) {
            filterRequest = "<and>\n";
        }
        int i=0;
        for (String filterElement : filterElements) {
            if (i>0 && addAnd) {
                filterRequest = filterRequest.concat("<and>\n");
            }
            filterRequest = filterRequest.concat("\n" + filterElement);
            if (i>0 && addAnd) {
                filterRequest = filterRequest.concat("</and>\n");
            }
            i++;

        }
        if (doingAnd && addAnd) {
            filterRequest = filterRequest.concat("</and>\n");
        }

        return filterRequest;
    }

    private String buildSearchElement(HPDSearchRequest.HPDSearchMode elementSearchMode, String attribute, String value) {
        return String.format(searchModeToDSMLQueryFragment(elementSearchMode), attribute, value);
    }

    private String searchModeToDSMLQueryFragment(HPDSearchRequest.HPDSearchMode mode) {
        switch (mode) {

            case Contains:
                return "         <substrings name='%s'> "
                        + "            <any>%s</any>  "
                        + "         </substrings>";
            case Equality:
                return "         <equalityMatch name='%s'> "
                        + "            <value>%s</value>  "
                        + "         </equalityMatch>";
            case StartsWith:
                return "         <substrings name='%s'> "
                        + "            <initial>%s</initial>  "
                        + "      </substrings>";
            case Exists:
                return "         <present name='%s' /> ";
            case NotExists:
                return "         <not> "
                        + "          <present name='%s' /> "
                        + "      </not>";

            default:
                throw new RuntimeException("Unexpected search mode in searchModeToDSMLType");
        }
    }

    public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public HPDSearchMode getNameSearchMode() {
        return nameSearchMode;
    }

    public void setNameSearchMode(HPDSearchMode nameSearchMode) {
        this.nameSearchMode = nameSearchMode;
    }

    public String getNameSearchText() {
        return nameSearchText;
    }

    public void setNameSearchText(String nameSearchText) {
        this.nameSearchText = nameSearchText;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getNpi() {
        return npi;
    }

    public void setNpi(String npi) {
        this.npi = npi;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public  String getRegisteredName() {
        return registeredName;
    }

    public void setRegisteredName(String registeredName)
    {
        this.registeredName = registeredName;
    }

    public  String getDescription() {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getSpecializationText() {
        return specializationText;
    }

    public void setSpecializationText(String specializationText) {
        this.specializationText = specializationText;
    }

    public HPDSearchScope getSearchScope() {
        return searchScope;
    }

    public void setSearchScope(HPDSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public String getSpecializationCode() {
        return specializationCode;
    }

    public void setSpecializationCode(String specializationCode) {
        this.specializationCode = specializationCode;
    }

    public final List<String> getDirectoryIds() {
        if (directoryIds==null) {
            directoryIds = new ArrayList<String>();
        }
        return directoryIds;
    }

    public final void setDirectoryIds(List<String> directoryIds) {
        this.directoryIds = directoryIds;
    }


    public final void addDirectoryId(String directoryId) {
        if (!getDirectoryIds().contains(directoryId)) {
            getDirectoryIds().add(directoryId);
        }
    }

    public final boolean hasDirectoryId(String directoryId) {
        return getDirectoryIds().contains(directoryId);
    }

    public String getServiceAddressText() {
        return serviceAddressText;
    }

    public void setServiceAddressText(String serviceAddressText) {
        this.serviceAddressText = serviceAddressText;
    }

    public HPDSearchMode getServiceSearchMode() {
        return serviceSearchMode;
    }

    public void setServiceSearchMode(HPDSearchMode serviceSearchMode) {
        this.serviceSearchMode = serviceSearchMode;
    }

    public HPDServiceProfile getServiceSearchProfile() {
        return serviceSearchProfile;
    }

    public void setServiceSearchProfile(HPDServiceProfile serviceSearchProfile) {
        this.serviceSearchProfile = serviceSearchProfile;
    }

    public String getAddressSearchText() {
        return addressSearchText;
    }

    public void setAddressSearchText(String addressSearchText) {
        this.addressSearchText = addressSearchText;
    }

    public String getAddressSearchText2() {
        return addressSearchText2;
    }

    public void setAddressSearchText2(String addressSearchText2) {
        this.addressSearchText2 = addressSearchText2;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPostalCodeText() {
        return postalCodeText;
    }

    public void setPostalCodeText(String postalCodeText) {
        this.postalCodeText = postalCodeText;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public static String buildQualifiedNPIQuery(String npi) {
        return String.format("2.16.840.1.113883.4.6:NPI:%s:Active", npi);
    }

    public static String buildQualifiedPostalCodeQuery(String postalCode) {
        return String.format("postalCode=%s", postalCode);
    }

    public static String buildQualifiedStateQuery(String stateOrProvinceName) {
        return String.format("state=%s", stateOrProvinceName);
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStateOrProvinceName() {
        return stateOrProvinceName;
    }

    public void setStateOrProvinceName(String stateOrProvinceName) {
        this.stateOrProvinceName = stateOrProvinceName;
    }

    public final List<String> getServiceDNs() {
        if (serviceDNs == null) {
            serviceDNs = new ArrayList<String>();
        }
        return serviceDNs;
    }

    public final void setServiceDNs(List<String> serviceIds) {
        this.serviceDNs = serviceIds;
    }

    public final void addServiceDN(String serviceId) {
        if (HPDUtil.isNotBlank(serviceId) && !getServiceDNs().contains(serviceId)) {
            getServiceDNs().add(serviceId);
        }
    }

    public final List<String> getCredentialDNs() {
        if (credentialDNs == null) {
            credentialDNs = new ArrayList<String>();
        }
        return credentialDNs;
    }

    public final void setCredentialDNs(List<String> credentialDNs) {
        this.credentialDNs = credentialDNs;
    }

    public final void addCredentialDN(String credentialDN) {
        if (HPDUtil.isNotBlank(credentialDN) && !getCredentialDNs().contains(credentialDN)) {
            getCredentialDNs().add(credentialDN);
        }
    }

    public List<String> getRelationshipProviderDNs() {
        if (relationshipProviderDNs == null) {
            relationshipProviderDNs = new ArrayList<String>();
        }
        return relationshipProviderDNs;
    }

    public void setRelationshipProviderDNs(List<String> relationshipProviderDNs) {
        this.relationshipProviderDNs = relationshipProviderDNs;
    }

    public final void addRelationshipProviderDN(String relationshipProviderDN) {
        if (HPDUtil.isNotBlank(relationshipProviderDN) && !getRelationshipProviderDNs().contains(relationshipProviderDN)) {
            getRelationshipProviderDNs().add(relationshipProviderDN);
        }
    }

    public List<String> getRelationshipOrgDNs() {
        if (relationshipOrgDNs == null) {
            relationshipOrgDNs = new ArrayList<String>();
        }
        return relationshipOrgDNs;
    }

    public void setRelationshipOrgDNs(List<String> relationshipOrgDNs) {
        this.relationshipOrgDNs = relationshipOrgDNs;
    }

    public final void addRelationshipOrgDN(String relationshipOrgDN) {
        if (HPDUtil.isNotBlank(relationshipOrgDN) && !getRelationshipOrgDNs().contains(relationshipOrgDN)) {
            getRelationshipOrgDNs().add(relationshipOrgDN);
        }
    }

    public List<String> getOrganizationDNs() {
        if (organizationDNs == null) {
            organizationDNs = new ArrayList<String>();
        }
        return organizationDNs;
    }

    public void setOrganizationDNs(List<String> organizationDNs) {
        this.organizationDNs = organizationDNs;
    }

    public final void addOrganizationDN(String organizationDN) {
        if (HPDUtil.isNotBlank(organizationDN) && !getOrganizationDNs().contains(organizationDN)) {
            getOrganizationDNs().add(organizationDN);
        }
    }

    public List<String> getIndividualProviderDNs() {
        if (individualProviderDNs == null) {
            individualProviderDNs = new ArrayList<String>();
        }
        return individualProviderDNs;
    }

    public void setIndividualProviderDNs(List<String> individualProviderDNs) {
        this.individualProviderDNs = individualProviderDNs;
    }

    public final void addIndividualProviderDN(String individualProviderDN) {
        if (HPDUtil.isNotBlank(individualProviderDN) && !getIndividualProviderDNs().contains(individualProviderDN)) {
            getIndividualProviderDNs().add(individualProviderDN);
        }
    }

    public final boolean isProvidersAffiliatedToOrgSearch() {
        return searchScope==HPDSearchScope.ProvidersAffiliatedToOrg;
    }

    public final boolean isGeneralEntitySearch() {
        switch (getSearchScope()) {
            case AllEntities:
            case OrgsOnly:
            case IndividualProvidersOnly:
                if (getOrganizationDNs().isEmpty() && getIndividualProviderDNs().isEmpty()) {
                    return true;
                }
                break;
        }
        return false;
    }

    public final String toDSML(HPDInstanceModel hpd, HPDClientConfig config) {
        String baseDNOffset = new String();
        String dsmlScope = new String();
        switch (getSearchScope()) {
            case AllEntities:
                //Do Nothing for the offset, but the depth needs to be 2 levels
                dsmlScope = "wholeSubtree";
                break;
            case IndividualProvidersOnly:
                baseDNOffset = "ou=" + Constants.ENTITY_TYPE_INDIVIDUAL_RDN_OU + ",";
                dsmlScope = "singleLevel";
                break;

            case OrgsOnly:
                baseDNOffset = "ou=" + Constants.ENTITY_TYPE_ORG_RDN_OU + ",";
                dsmlScope = "singleLevel";
                break;
            case CredentialsOnly:
                baseDNOffset = "ou=" + Constants.ENTITY_TYPE_CREDENTIAL_RDN_OU + ",";
                dsmlScope = "singleLevel";
                break;
            case ProvidersAffiliatedToOrg:
            case HasAProviderRelationshipsOnly:
            case HasAnOrgRelationshipsOnly:
                baseDNOffset = "ou=" + Constants.ENTITY_TYPE_RELATIONSHIP_RDN_OU + ",";
                dsmlScope = "singleLevel";
                break;
            case ServicesOnly:
                baseDNOffset = "ou=" + Constants.ENTITY_TYPE_SERVICE_RDN_OU + ",";
                dsmlScope = "singleLevel";
                break;
            default:
                throw new RuntimeException("Unexpected SearchRequest searchScope: " + getSearchScope());
        }
        //Building the SoapRequest
        //Get the Current template we're going to use
        String requestBody = config.getSoapRequestTemplate();
        //We might need to store this later, we'll see...
        String soapActionRequestUUID = UUID.randomUUID().toString();
        //Generate an random requestId to use for the batchRequest and the searchRequest.  Keeping these the same for now.
        String randomRequestId = HPDUtil.generateRequestId();
        //Generate batchRequestID
        String batchRequestId = randomRequestId;
        //Generate searchRequestID
        String searchRequetID = randomRequestId;
        //Build the batchrequest, merging in the required dynamic aspects via String.format()
        String batchRequest = String.format(requestBody, soapActionRequestUUID, hpd.getServiceURL(), batchRequestId, String.format(searchRequestBody, baseDNOffset, hpd.getBaseDN(), dsmlScope, searchRequetID, toDSMLSearchString(hpd)));
        //If we need to tweak the request for an edge case, apply the XSLT to the rqeuest.
        if (HPDUtil.isNotBlank(hpd.getCustomHPDRequestXSLT())) {
            //Do the XSLT here on the request to conform to whatever honked request the destination needs..
            log.log(Level.FINE, "<<<< HPDRequestBody Transformed via custom XSLT");
            String xsltTransform = hpd.getCustomHPDRequestXSLT();
            log.log(Level.FINE, "PRE:\n{0}", batchRequest);
            batchRequest = XSLUtil.tranform(xsltTransform, batchRequest);
            log.log(Level.FINE, "POST:\n{0}", batchRequest);
        }
        return batchRequest;
    }

    private String dumpDNList(String listLabel, List<String> dnList) {
        String output = "";

        //Dump the lists of credential and service DN-s here...
        if (!dnList.isEmpty()) {
            output = output.concat("credentialDN List: ");
            for (String dn : dnList) {
                output = output.concat(dn) + " ";
            }
        }
        return output;
    }
}