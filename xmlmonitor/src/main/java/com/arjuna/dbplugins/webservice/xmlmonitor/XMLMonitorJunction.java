/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice.xmlmonitor;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import org.w3c.dom.Document;

@Singleton
public class XMLMonitorJunction
{
    private static final Logger logger = Logger.getLogger(XMLMonitorJunction.class.getName());

    public XMLMonitorJunction()
    {
        logger.log(Level.FINE, "XMLMonitorJunction");

        _syncObject  = new Object();
        _documentMap = new HashMap<String, Document>();
    }

    public void deposit(String id, Document document)
    {
        synchronized (_syncObject)
        {
            logger.log(Level.FINE, "XMLMonitorJunction.deposit: " + id);

            _documentMap.put(id, document);
        }
    }

    public Document withdraw(String id)
    {
        synchronized (_syncObject)
        {
            logger.log(Level.FINE, "XMLMonitorJunction.withdraw: " + id);

            return _documentMap.get(id);
        }
    }

    private Object                _syncObject;
    private Map<String, Document> _documentMap;
}
