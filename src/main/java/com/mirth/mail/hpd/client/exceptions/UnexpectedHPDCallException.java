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
package com.mirth.mail.hpd.client.exceptions;

public class UnexpectedHPDCallException extends Exception {

    private static final long serialVersionUID = -9026002305149599620L;
    public int statusCode;

    public UnexpectedHPDCallException() {
    }

    public UnexpectedHPDCallException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }
}
