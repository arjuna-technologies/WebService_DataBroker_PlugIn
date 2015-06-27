/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.tests.ckan.filestore;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import static org.junit.Assert.*;

public class CKANAPIProperties
{
    public CKANAPIProperties(String ckanAPIPropertiesFilename)
    {
        _ckanAPIProperties = new Properties();

        try
        {
            FileReader ckanAPIFileReader = new FileReader(ckanAPIPropertiesFilename);
            _ckanAPIProperties.load(ckanAPIFileReader);
            ckanAPIFileReader.close();
            _loaded = true;
        }
        catch (IOException ioException)
        {
            _ckanAPIProperties = null;
            _loaded = false;
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public String getCKANRootURL()
    {
        if (_ckanAPIProperties != null)
        {
            String ckanRootURL = _ckanAPIProperties.getProperty("ckanrooturl");

            if (ckanRootURL != null)
                return ckanRootURL;
            else
            {
                fail("Failed to obtain \"ckanrooturl\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"ckanrooturl\" property, no property file");
            return null;
        }
    }

    public String getPackageId()
    {
        if (_ckanAPIProperties != null)
        {
            String packageId = _ckanAPIProperties.getProperty("package_id");

            if (packageId != null)
                return packageId;
            else
            {
                fail("Failed to obtain \"package_id\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"package_id\" property, no property file");
            return null;
        }
    }

    public String getAPIKey()
    {
        if (_ckanAPIProperties != null)
        {
            String apiKey = _ckanAPIProperties.getProperty("apikey");

            if (apiKey != null)
                return apiKey;
            else
            {
                fail("Failed to obtain \"apikey\" property");
                return null;
            }
        }
        else
        {
            fail("Failed to obtain \"apikey\" property, no property file");
            return null;
        }
    }

    private boolean    _loaded;
    private Properties _ckanAPIProperties = new Properties();
}
