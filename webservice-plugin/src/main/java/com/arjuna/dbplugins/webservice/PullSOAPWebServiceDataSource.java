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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Document;

import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.jee.annotation.DataProviderInjection;
import com.arjuna.databroker.data.jee.annotation.PostActivated;
import com.arjuna.databroker.data.jee.annotation.PostConfig;
import com.arjuna.databroker.data.jee.annotation.PostCreated;
import com.arjuna.databroker.data.jee.annotation.PreDeactivated;
import com.arjuna.databroker.data.jee.annotation.PreDelete;

public class PullSOAPWebServiceDataSource implements DataSource
{
    private static final Logger logger = Logger.getLogger(PullSOAPWebServiceDataSource.class.getName());

    public static final String SERVICEURL_PROPERTYNAME         = "Service URL";
    public static final String OPERATIONNAMESPACE_PROPERTYNAME = "Operation Namespace";
    public static final String OPERATIONNAME_PROPERTYNAME      = "Operation Name";
    public static final String SCHEDULEDELAY_PROPERTYNAME      = "Schedule Delay (ms)";
    public static final String SCHEDULEPERIOD_PROPERTYNAME     = "Schedule Period (ms)";
    public static final String USERNAME_PROPERTYNAME           = "User Name";
    public static final String PASSWORD_PROPERTYNAME           = "Password";

    public PullSOAPWebServiceDataSource()
    {
        logger.log(Level.FINE, "PullWebServiceDataSource");
    }

    public PullSOAPWebServiceDataSource(String name, Map<String, String> properties)
    {
        logger.log(Level.FINE, "PullWebServiceDataSource: " + name + ", " + properties);

        _name          = name;
        _properties    = properties;
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

    @PostCreated
    @PostConfig
    public void setup()
    {
        _serviceURL         = _properties.get(SERVICEURL_PROPERTYNAME);
        _operationNamespace = _properties.get(OPERATIONNAMESPACE_PROPERTYNAME);
        _operationName      = _properties.get(OPERATIONNAME_PROPERTYNAME);
        _scheduleDelay      = Long.parseLong(_properties.get(SCHEDULEDELAY_PROPERTYNAME));
        _schedulePeriod     = Long.parseLong(_properties.get(SCHEDULEPERIOD_PROPERTYNAME));
        _username           = _properties.get(USERNAME_PROPERTYNAME);
        _password           = _properties.get(PASSWORD_PROPERTYNAME);
    }

    @PostActivated
    public void activate()
    {
        _timer               = new Timer(true);
        _invocationTimerTask = new InvocationTimerTask();
        _timer.scheduleAtFixedRate(_invocationTimerTask, _scheduleDelay, _schedulePeriod);

    }

    @PreDeactivated
    public void deactivate()
    {
        _invocationTimerTask.stop();
        _timer               = null;
        _invocationTimerTask = null;
    }

    @PreDelete
    public void shutdown()
    {
    }

    private class InvocationTimerTask extends TimerTask
    {
        public InvocationTimerTask()
        {
        }

        @Override
        public void run()
        {
            logger.log(Level.FINE, "InvocationTimerTask.run");

            Document result = null;
            try
            {
                MessageFactory messageFactory  = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
                SOAPMessage    request         = messageFactory.createMessage();
                SOAPPart       requestPart     = request.getSOAPPart();
                SOAPEnvelope   requestEnvelope = requestPart.getEnvelope();
                SOAPBody       requestBody     = requestEnvelope.getBody();
                requestEnvelope.addNamespaceDeclaration("oper", _operationNamespace);
                SOAPBodyElement operationElement = requestBody.addBodyElement(requestEnvelope.createQName(_operationName, "oper"));
                if ((_username != null) && (! _username.trim().equals("")))
                {
                    SOAPElement usernameElement = operationElement.addChildElement("UserName", "oper");
                    usernameElement.setTextContent(_username);
                }
                if ((_password != null) && (! _password.trim().equals("")))
                {
                    SOAPElement passwordElement = operationElement.addChildElement("Password", "oper");
                    passwordElement.setTextContent(_password);
                }

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

                SOAPPart     responcePart     = responce.getSOAPPart();
                SOAPEnvelope responceEnvelope = responcePart.getEnvelope();
                SOAPBody     responceBody     = responceEnvelope.getBody();

                result = responceBody.extractContentAsDocument();
            }
            catch (Throwable throwable)
            {
                logger.log(Level.WARNING, "Problems with web service invoke", throwable);
            }

            if (result != null)
                _dataProvider.produce(result);
        }

        public void stop()
        {
            cancel();
        }
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

    private String _serviceURL;
    private String _operationNamespace;
    private String _operationName;
    private Long   _scheduleDelay;
    private Long   _schedulePeriod;
    private String _username;
    private String _password;

    private Timer               _timer;
    private InvocationTimerTask _invocationTimerTask;

    private String                 _name;
    private Map<String, String>    _properties;
    private DataFlow               _dataFlow;
    @DataProviderInjection
    private DataProvider<Document> _dataProvider;
}
