/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice.xmlmonitor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.w3c.dom.Document;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataService;
import com.arjuna.databroker.data.jee.annotation.DataConsumerInjection;
import com.arjuna.databroker.data.jee.annotation.DataProviderInjection;

public class XMLMonitorDataService implements DataService
{
    private static final Logger logger = Logger.getLogger(XMLMonitorDataService.class.getName());

    public static final String XMLMONITORID_PROPERTYNAME = "XML Monitor ID";

    public XMLMonitorDataService()
    {
        logger.log(Level.FINE, "XMLMonitorDataService");
    }

    public XMLMonitorDataService(String name, Map<String, String> properties)
    {
        logger.log(Level.FINE, "XMLMonitorDataService: " + name + ", " + properties);

        _name       = name;
        _properties = properties;

        _endpointId = properties.get(XMLMONITORID_PROPERTYNAME);

        try
        {
            _providerWebServiceJunction = (XMLMonitorJunction) new InitialContext().lookup("java:global/webservice-plugin-ear-1.0.0p2m1/xmlmonitor-1.0.0p2m1/XMLMonitorJunction");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "ProviderXMLMonitorDataService: no providerWebServiceJunction found", throwable);
        }
    }

    @Override
    public DataFlow getDataFlow()
    {
        return _dataFlow;
    }

    @Override
    public void setDataFlow(DataFlow dataFlow)
    {
        _dataFlow = dataFlow;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public void setName(String name)
    {
        _name = name;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(_properties);
    }

    @Override
    public void setProperties(Map<String, String> properties)
    {
        _properties = properties;
    }

    public void consume(Document data)
    {
        logger.log(Level.FINE, "XMLMonitorDataService.consume");

        if (_providerWebServiceJunction != null)
            _providerWebServiceJunction.deposit(_endpointId, data);
        else
            logger.log(Level.WARNING, "XMLMonitorDataService.consume: no providerWebServiceJunction");
    }

    @Override
    public Collection<Class<?>> getDataConsumerDataClasses()
    {
        Set<Class<?>> dataConsumerDataClasses = new HashSet<Class<?>>();

        dataConsumerDataClasses.add(Document.class);

        return dataConsumerDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataConsumer<T> getDataConsumer(Class<T> dataClass)
    {
        if (dataClass == Document.class)
            return (DataConsumer<T>) _dataConsumer;
        else
            return null;
    }

    @Override
    public Collection<Class<?>> getDataProviderDataClasses()
    {
        Set<Class<?>> dataProviderDataClasses = new HashSet<Class<?>>();

        dataProviderDataClasses.add(Document.class);

        return dataProviderDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataProvider<T> getDataProvider(Class<T> dataClass)
    {
        if (dataClass == Document.class)
            return (DataProvider<T>) _dataProvider;
        else
            return null;
    }

    private String _endpointId;

    private DataFlow               _dataFlow;
    private String                 _name;
    private Map<String, String>    _properties;
    @DataConsumerInjection(methodName="consume")
    private DataConsumer<Document> _dataConsumer;
    @DataProviderInjection
    private DataProvider<Document> _dataProvider;

    private XMLMonitorJunction _providerWebServiceJunction;
}
