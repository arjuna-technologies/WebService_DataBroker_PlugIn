/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.jboss.resteasy.util.GenericType;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataProvider;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.jee.annotation.DataProviderInjection;
import com.arjuna.databroker.data.jee.annotation.PostActivated;
import com.arjuna.databroker.data.jee.annotation.PostConfig;
import com.arjuna.databroker.data.jee.annotation.PostCreated;
import com.arjuna.databroker.data.jee.annotation.PreDeactivated;
import com.arjuna.databroker.data.jee.annotation.PreDelete;

public class PullJSONWebServiceDataSource implements DataSource
{
    private static final Logger logger = Logger.getLogger(PullJSONWebServiceDataSource.class.getName());

    public static final String SERVICEURL_PROPERTYNAME     = "Service URL";
    public static final String SCHEDULEDELAY_PROPERTYNAME  = "Schedule Delay (ms)";
    public static final String SCHEDULEPERIOD_PROPERTYNAME = "Schedule Period (ms)";
    public static final String USERNAME_PROPERTYNAME       = "User Name";
    public static final String PASSWORD_PROPERTYNAME       = "Password";

    public PullJSONWebServiceDataSource()
    {
        logger.log(Level.FINE, "PullJSONWebServiceDataSource");
    }

    public PullJSONWebServiceDataSource(String name, Map<String, String> properties)
    {
        logger.log(Level.FINE, "PullJSONWebServiceDataSource: " + name + ", " + properties);

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
        logger.log(Level.FINER, "PullJSONWebServiceDataSource.setup");
        _serviceURL     = _properties.get(SERVICEURL_PROPERTYNAME);
        _scheduleDelay  = Long.parseLong(_properties.get(SCHEDULEDELAY_PROPERTYNAME));
        _schedulePeriod = Long.parseLong(_properties.get(SCHEDULEPERIOD_PROPERTYNAME));
        _username       = _properties.get(USERNAME_PROPERTYNAME);
        _password       = _properties.get(PASSWORD_PROPERTYNAME);
    }

    @PostActivated
    public void activate()
    {
        logger.log(Level.FINER, "PullJSONWebServiceDataSource.activate");
        _timer               = new Timer(true);
        _invocationTimerTask = new InvocationTimerTask();
        _timer.scheduleAtFixedRate(_invocationTimerTask, _scheduleDelay, _schedulePeriod);

    }

    @PreDeactivated
    public void deactivate()
    {
        logger.log(Level.FINER, "PullJSONWebServiceDataSource.deactivate");
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
            logger.log(Level.FINER, "PullJSONWebServiceDataSource.InvocationTimerTask.run");

            String json = null;
            try
            {
            	String        token = _username + ":" + _password;
                String        base64Token = Base64.encodeBytes(token.getBytes(StandardCharsets.UTF_8));
                ClientRequest request = new ClientRequest(_serviceURL);
                request.accept(MediaType.APPLICATION_JSON);
                request.header("Authorization", "Basic " + base64Token);

                ClientResponse<String> response = request.get(new GenericType<String>() {});

                if (response.getStatus() == HttpResponseCodes.SC_OK)
                    json = response.getEntity();
                else
                {
                    logger.log(Level.WARNING, "Problem in getting 'json' " + response.getStatus());
                }
            }
            catch (Throwable throwable)
            {
                logger.log(Level.WARNING, "Problems with web service invoke", throwable);
            }

            if (json != null)
                _dataProvider.produce(json);
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

        dataProviderDataClasses.add(String.class);
        
        return dataProviderDataClasses;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataProvider<T> getDataProvider(Class<T> dataClass)
    {
        if (dataClass.isAssignableFrom(String.class))
            return (DataProvider<T>) _dataProvider;
        else
            return null;
    }

    private String _serviceURL;
    private Long   _scheduleDelay;
    private Long   _schedulePeriod;
    private String _username;
    private String _password;

    private Timer               _timer;
    private InvocationTimerTask _invocationTimerTask;
    
    private String               _name;
    private Map<String, String>  _properties;
    private DataFlow             _dataFlow;
    @DataProviderInjection
    private DataProvider<String> _dataProvider;
}
