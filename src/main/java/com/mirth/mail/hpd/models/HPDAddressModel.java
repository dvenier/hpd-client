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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlTransient;

public class HPDAddressModel implements Serializable {

    private static final long serialVersionUID = 1L;
    public static String ADDRESS_TYPE_PRACTICE = "1";
    public static String ADDRESS_TYPE_MAILING = "2";
    public static String ADDRESS_TYPE_BILLING = "3";
    public static String ADDRESS_TYPE_LEGAL = "4";
    
    private String addressTypeId;
    private String addressTypeLabel;
    private String addressLine1;
    private String addressLine2;
    private String locality;
    private String state;
    private String postal;
    private String countryCode;
    private String statusId;
    private String affiliatedOrgId;
    private String affiliatedOrgLDAPDn;
    private String streetName;
    private String streetNumber;

    public HPDAddressModel() {
    }

    public HPDAddressModel(String addressTypeId, String addressTypeLabel, String hpdLDAPAddressAttr) {
        this.addressTypeId = addressTypeId;
        this.addressTypeLabel = addressTypeLabel;
        if (hpdLDAPAddressAttr == null) {
            return;
        }
        //Hack:  If they're doing comma delimited, tolerate it and convert them to $ and pray they don't use too many of them ;)
        if (!hpdLDAPAddressAttr.contains("$") && hpdLDAPAddressAttr.contains(",")) {
            hpdLDAPAddressAttr = hpdLDAPAddressAttr.replace(",", "$");
        }
        //Ok, do the splits on $ which is the official DSML separator.
        String[] addrTokens = hpdLDAPAddressAttr.split("\\$");
        if (addrTokens.length > 1) {
            List<String> addrPartList = Arrays.asList(addrTokens);
            for (String addrPartNVP : addrPartList) {
                String[] addrPart = addrPartNVP.split("=");
                if (addrPart.length > 1) {
                    setAddrPart(addrPart[0], addrPart[1]);
                }
            }
        }
        //Numbskulls like GSI apparently like sending the address information as StreetNumber and StreetName inconsistently... idiots
        if (StringUtils.isBlank(addressLine1) && (!StringUtils.isBlank(streetName) || !StringUtils.isBlank(streetNumber))) {
            addressLine1 = (StringUtils.isBlank(streetNumber) ? "" : streetNumber) + " " + (StringUtils.isBlank(streetName) ? "" : streetName);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[addressTypeId=" + addressTypeId + ", addressLine1=" + addressLine1 + ", addressLine2=" + addressLine2 + ", locality=" + locality + ", state=" + state + ", postal=" + postal + "]";
    }    
    
    private void setAddrPart(String varName, String varValue) {
        varName = varName.trim();
        if (!StringUtils.isBlank(varValue)) {
            varValue = varValue.trim();

            if (varName.equalsIgnoreCase("status")) {
                this.statusId = varValue;
            }
            if (varName.equalsIgnoreCase("addr1")) {
                this.addressLine1 = varValue;
            }
            if (varName.equalsIgnoreCase("addr2")) {
                this.addressLine2  = varValue;
            }
            if (varName.equalsIgnoreCase("city")) {
                this.locality = varValue;
            }
            if (varName.equalsIgnoreCase("postalCode")) {
                this.postal = varValue;
            }
            if (varName.equalsIgnoreCase("state")) {
                this.state = varValue;
            }
            if (varName.equalsIgnoreCase("countryCode")) {
                this.countryCode = varValue;
            }
            if (varName.equalsIgnoreCase("streetName")) {
                this.streetName = varValue;
            }
            if (varName.equalsIgnoreCase("streetNumber")) {
                this.streetNumber = varValue;
            }
        }
    }

    public boolean equals(HPDAddressModel address) {
        if (address == null) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(this.getAddressTypeId(), address.getAddressTypeId())
                && StringUtils.equalsIgnoreCase(this.getAddressLine1(), address.getAddressLine1())
                && StringUtils.equalsIgnoreCase(this.getAddressLine2(), address.getAddressLine2())
                && StringUtils.equalsIgnoreCase(this.getStreetName(), address.getStreetName())       
                && StringUtils.equalsIgnoreCase(this.getStreetNumber(), address.getStreetNumber())                  
                && StringUtils.equalsIgnoreCase(this.getLocality(), address.getLocality())
                && StringUtils.equalsIgnoreCase(this.getState(), address.getState())
                && StringUtils.equalsIgnoreCase(this.getPostal(), address.getPostal())
                && StringUtils.equalsIgnoreCase(this.getCountryCode(), address.getCountryCode());
    }

    public boolean hasAddress() {
        return StringUtils.isNotBlank(addressTypeId) && (StringUtils.isNotBlank(addressLine1) || StringUtils.isNotBlank(streetName) || StringUtils.isNotBlank(streetNumber) || StringUtils.isNotBlank(locality) || StringUtils.isNotBlank(postal) || StringUtils.isNotBlank(state));
    }

    public String getAddressTypeId() {
        return addressTypeId;
    }

    public void setAddressTypeId(String addressTypeId) {
        this.addressTypeId = addressTypeId;
    }

    public String getAddressTypeLabel() {
        return addressTypeLabel;
    }

    public void setAddressTypeLabel(String addressTypeLabel) {
        this.addressTypeLabel = addressTypeLabel;
    }

    public final String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public final String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }
    
    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (state != null) {
            state = state.toUpperCase();
        }
        this.state = state;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getAffiliatedOrgId() {
        return affiliatedOrgId;
    }

    public void setAffiliatedOrgId(String affiliatedOrgId) {
        this.affiliatedOrgId = affiliatedOrgId;
    }

    public String getAffiliatedOrgLDAPDn() {
        return affiliatedOrgLDAPDn;
    }

    public void setAffiliatedOrgLDAPDn(String affiliatedOrgLDAPDn) {
        this.affiliatedOrgLDAPDn = affiliatedOrgLDAPDn;
    }
    
    /**
     * Helper Function
     *
     * @return
     */
    @XmlTransient
    public String getFullAddress() {
        return getFullAddress(this);
    }

    /**
     * Helper Function
     *
     * @return
     */
    @XmlTransient
    public static String getFullAddress(HPDAddressModel model) {
        StringBuilder address = new StringBuilder();
        if (StringUtils.isNotBlank(model.getAddressLine1())) {
            address.append(model.getAddressLine1());
        }
        if (StringUtils.isNotBlank(model.getAddressLine2())) {
            address.append(" ");
            address.append(model.getAddressLine2());
        }
        if (StringUtils.isNotBlank(model.getLocality())) {
            address.append(" ");
            address.append(model.getLocality());
        }
        if (StringUtils.isNotBlank(model.getState())) {
            if (StringUtils.isNotBlank(model.getLocality())) {
                address.append(",");
            }
            address.append(" ");
            address.append(model.getState());
        }
        if (StringUtils.isNotBlank(model.getPostal())) {
            address.append(" ");
            address.append(model.getPostal());
        }
        return address.toString().trim();
    }

}