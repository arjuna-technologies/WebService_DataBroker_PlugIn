/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice;

import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;
import com.arjuna.dbplugins.webservice.xmlmonitor.XMLMonitorDataFlowNodeFactory;

@Startup
@Singleton
public class WebServiceDataFlowNodeFactoriesSetup
{
    @PostConstruct
    public void setup()
    {
        DataFlowNodeFactory simpleDataFlowNodeFactory    = new WebServiceDataFlowNodeFactory("WebService Data Flow Node Factories", Collections.<String, String>emptyMap());
        DataFlowNodeFactory xmlMonitorDataServiceFactory = new XMLMonitorDataFlowNodeFactory("XML Monitor Data Service Factory", Collections.<String, String>emptyMap());

        _dataFlowNodeFactoryInventory.addDataFlowNodeFactory(simpleDataFlowNodeFactory);
        _dataFlowNodeFactoryInventory.addDataFlowNodeFactory(xmlMonitorDataServiceFactory);
    }

    @PreDestroy
    public void cleanup()
    {
        _dataFlowNodeFactoryInventory.removeDataFlowNodeFactory("WebService Data Flow Node Factories");
        _dataFlowNodeFactoryInventory.removeDataFlowNodeFactory("XML Monitor Data Service Factory");
    }

    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeFactoryInventory")
    private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;
}
