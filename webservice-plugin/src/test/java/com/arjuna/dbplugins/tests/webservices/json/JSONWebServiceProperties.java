/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.tests.webservices.json;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import static org.junit.Assert.*;

public class JSONWebServiceProperties
{
    public JSONWebServiceProperties(String jsonWebServicePropertiesFilename)
    {
        _jsonWebServiceProperties = new Properties();

        try
        {
            FileReader jsonWebServiceFileReader = new FileReader(jsonWebServicePropertiesFilename);
            _jsonWebServiceProperties.load(jsonWebServiceFileReader);
            jsonWebServiceFileReader.close();
            _loaded = true;
        }
        catch (IOException ioException)
        {
            _jsonWebServiceProperties = null;
            _loaded = false;
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public String getServiceURL()
    {
        if (_jsonWebServiceProperties != null)
        {
            String serviceURL = _jsonWebServiceProperties.getProperty("serviceurl");

            if (serviceURL != null)
                return serviceURL;
            else
            {
                fail("Failed to obtain \"serviceurl\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"serviceurl\" property, no property file");
            return null;
        }
    }

    public String getScheduleDelay()
    {
        if (_jsonWebServiceProperties != null)
        {
            String scheduleDelay = _jsonWebServiceProperties.getProperty("scheduledelay");

            if (scheduleDelay != null)
                return scheduleDelay;
            else
            {
                fail("Failed to obtain \"scheduledelay\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"scheduledelay\" property, no property file");
            return null;
        }
    }

    public String getSchedulePeriod()
    {
        if (_jsonWebServiceProperties != null)
        {
            String schedulePeriod = _jsonWebServiceProperties.getProperty("scheduleperiod");

            if (schedulePeriod != null)
                return schedulePeriod;
            else
            {
                fail("Failed to obtain \"scheduleperiod\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"scheduleperiod\" property, no property file");
            return null;
        }
    }

    public String getUserName()
    {
        if (_jsonWebServiceProperties != null)
        {
            String userName = _jsonWebServiceProperties.getProperty("username");

            if (userName != null)
                return userName;
            else
            {
                fail("Failed to obtain \"username\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"username\" property, no property file");
            return null;
        }
    }

    public String getPassword()
    {
        if (_jsonWebServiceProperties != null)
        {
            String password = _jsonWebServiceProperties.getProperty("password");

            if (password != null)
                return password;
            else
            {
                fail("Failed to obtain \"password\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"password\" property, no property file");
            return null;
        }
    }

    private boolean    _loaded;
    private Properties _jsonWebServiceProperties = new Properties();
}
