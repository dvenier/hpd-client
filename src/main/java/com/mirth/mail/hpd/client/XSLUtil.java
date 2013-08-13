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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

public class XSLUtil {

    public static Node transform(String xslSource, Node original) {
        StringReader sr = new StringReader(xslSource);
        DOMResult result = new DOMResult();
        doTransform(new StreamSource(sr), new DOMSource(original), result);
        return result.getNode();
    }

    public static String tranform(String xslSource, String original) {
        StringReader sr = new StringReader(xslSource);
        StringReader sro = new StringReader(original);
        StringWriter result = new StringWriter();
        doTransform(new StreamSource(sr), new StreamSource(sro), new StreamResult(result));
        return result.toString();
    }

    private static void doTransform(Source xslSource, Source xmlSource, Result xslResult) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(xslSource);
            transformer.transform(xmlSource, xslResult);
        } catch (TransformerFactoryConfigurationError ex) {
            throw new RuntimeException(ex);
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }
}
