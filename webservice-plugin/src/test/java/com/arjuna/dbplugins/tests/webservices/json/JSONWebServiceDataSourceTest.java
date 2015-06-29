/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.tests.webservices.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import com.arjuna.databroker.data.connector.ObservableDataProvider;
import com.arjuna.databroker.data.connector.ObserverDataConsumer;
import com.arjuna.databroker.data.core.DataFlowNodeLifeCycleControl;
import com.arjuna.dbplugins.webservice.PullJSONWebServiceDataSource;
import com.arjuna.dbutils.testsupport.dataflownodes.dummy.DummyDataSink;
import com.arjuna.dbutils.testsupport.dataflownodes.lifecycle.TestJEEDataFlowNodeLifeCycleControl;

public class JSONWebServiceDataSourceTest
{
    private static final Logger logger = Logger.getLogger(JSONWebServiceDataSourceTest.class.getName());

    @Test
    public void createResourceAsString()
    {
        try
        {
            JSONWebServiceProperties jsonWebServiceProperties = new JSONWebServiceProperties("jsonwebservice.properties");

            if (! jsonWebServiceProperties.isLoaded())
            {
                logger.log(Level.INFO, "SKIPPING TEST 'createResourceAsString', no propertiles file");
                return;
            }

            DataFlowNodeLifeCycleControl dataFlowNodeLifeCycleControl = new TestJEEDataFlowNodeLifeCycleControl();

            String              name       = "PullJSONWebServiceDataSource";
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(PullJSONWebServiceDataSource.SERVICEURL_PROPERTYNAME, jsonWebServiceProperties.getServiceURL());
            properties.put(PullJSONWebServiceDataSource.SCHEDULEDELAY_PROPERTYNAME, jsonWebServiceProperties.getScheduleDelay());
            properties.put(PullJSONWebServiceDataSource.SCHEDULEPERIOD_PROPERTYNAME, jsonWebServiceProperties.getSchedulePeriod());
            properties.put(PullJSONWebServiceDataSource.USERNAME_PROPERTYNAME, jsonWebServiceProperties.getUserName());
            properties.put(PullJSONWebServiceDataSource.PASSWORD_PROPERTYNAME, jsonWebServiceProperties.getPassword());

            PullJSONWebServiceDataSource pullJSONWebServiceDataSource = new PullJSONWebServiceDataSource(name, properties);
            DummyDataSink                dummyDataSink                = new DummyDataSink("Dummy Data Sink", Collections.<String, String>emptyMap());

            dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), pullJSONWebServiceDataSource, null);
            dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), dummyDataSink, null);

            ((ObservableDataProvider<String>) pullJSONWebServiceDataSource.getDataProvider(String.class)).addDataConsumer((ObserverDataConsumer<String>) dummyDataSink.getDataConsumer(String.class));

            Thread.sleep(10000);
            List<Object> receivedData = dummyDataSink.receivedData();

            dataFlowNodeLifeCycleControl.removeDataFlowNode(dummyDataSink);
            dataFlowNodeLifeCycleControl.removeDataFlowNode(pullJSONWebServiceDataSource);
            
            assertEquals("Unexpected received data number", 1, receivedData.size());
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem in 'createResource'", throwable);
            fail("Problem in 'createResource': " + throwable);
        }
    }
}
