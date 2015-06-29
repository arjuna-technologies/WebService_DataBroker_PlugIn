/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Document;

import com.arjuna.databroker.data.DataConsumer;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataSink;
import com.arjuna.databroker.data.jee.annotation.DataConsumerInjection;

public class PushSOAPWebServiceDataSink implements DataSink
{
    private static final Logger logger = Logger.getLogger(PushSOAPWebServiceDataSink.class.getName());

    public static final String SERVICEURL_PROPERTYNAME         = "Service URL";
    public static final String OPERATIONNAMESPACE_PROPERTYNAME = "Operation Namespace";
    public static final String OPERATIONNAME_PROPERTYNAME      = "Operation Name";

    public PushSOAPWebServiceDataSink()
    {
        logger.log(Level.FINE, "PushWebServiceDataSink");
    }

    public PushSOAPWebServiceDataSink(String name, Map<String, String> properties)
    {
        logger.log(Level.FINE, "PushWebServiceDataSink: " + name + ", " + properties);

        _name       = name;
        _properties = properties;

        _serviceURL         = properties.get(SERVICEURL_PROPERTYNAME);
        _operationNamespace = properties.get(OPERATIONNAMESPACE_PROPERTYNAME);
        _operationName      = properties.get(OPERATIONNAME_PROPERTYNAME);
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

    public void consume(Document data)
    {
        logger.log(Level.FINE, "PushWebServiceDataSink.consume");

        try
        {
            MessageFactory messageFactory  = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPMessage    request         = messageFactory.createMessage();
            SOAPPart       requestPart     = request.getSOAPPart();
            SOAPEnvelope   requestEnvelope = requestPart.getEnvelope();
            SOAPBody       requestBody     = requestEnvelope.getBody();
            requestEnvelope.addNamespaceDeclaration("oper", _operationNamespace);
            requestBody.addBodyElement(requestEnvelope.createQName(_operationName, "oper"));
            requestBody.addDocument(data);

            if (logger.isLoggable(Level.FINE))
            {
                ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream();
                request.writeTo(requestOutputStream);
                logger.log(Level.FINE, "Request: " + requestOutputStream.toString());
                requestOutputStream.close();
            }

            SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection        connection        = connectionFactory.createConnection();

            SOAPMessage responce = connection.call(request, _serviceURL);

            if (logger.isLoggable(Level.FINE))
            {
                ByteArrayOutputStream responceOutputStream = new ByteArrayOutputStream();
                responce.writeTo(responceOutputStream);
                logger.log(Level.FINE, "Responce: " + responceOutputStream.toString());
                responceOutputStream.close();
            }
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problems with web service invoke", throwable);
        }
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

    private String _serviceURL;
    private String _operationNamespace;
    private String _operationName;

    private String                 _name;
    private Map<String, String>    _properties;
    private DataFlow               _dataFlow;
    @DataConsumerInjection(methodName="consume")
    private DataConsumer<Document> _dataConsumer;
}
