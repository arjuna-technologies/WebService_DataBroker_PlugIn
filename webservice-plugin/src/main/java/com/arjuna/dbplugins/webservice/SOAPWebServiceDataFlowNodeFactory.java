/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.arjuna.databroker.data.DataFlowNode;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataSink;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.InvalidClassException;
import com.arjuna.databroker.data.InvalidMetaPropertyException;
import com.arjuna.databroker.data.InvalidNameException;
import com.arjuna.databroker.data.InvalidPropertyException;
import com.arjuna.databroker.data.MissingMetaPropertyException;
import com.arjuna.databroker.data.MissingPropertyException;

public class SOAPWebServiceDataFlowNodeFactory implements DataFlowNodeFactory
{
    public SOAPWebServiceDataFlowNodeFactory(String name, Map<String, String> properties)
    {
        _name       = name;
        _properties = properties;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return _properties;
    }

    @Override
    public List<Class<? extends DataFlowNode>> getClasses()
    {
        List<Class<? extends DataFlowNode>> classes = new LinkedList<Class<? extends DataFlowNode>>();

        classes.add(DataSource.class);
        classes.add(DataProcessor.class);
        classes.add(DataSink.class);

        return classes;
    }

    @Override
    public <T extends DataFlowNode> List<String> getMetaPropertyNames(Class<T> dataFlowNodeClass)
        throws InvalidClassException
    {
        if (dataFlowNodeClass.equals(DataSource.class) || dataFlowNodeClass.equals(DataProcessor.class) || dataFlowNodeClass.equals(DataSink.class))
            return Collections.emptyList();
        else
            throw new InvalidClassException("Unsupported class", dataFlowNodeClass.getName());
    }

    @Override
    public <T extends DataFlowNode> List<String> getPropertyNames(Class<T> dataFlowNodeClass, Map<String, String> metaProperties)
        throws InvalidClassException, InvalidMetaPropertyException, MissingMetaPropertyException
    {
        if (dataFlowNodeClass.equals(DataSource.class))
        {
            if (metaProperties.isEmpty())
            {
                List<String> propertyNames = new LinkedList<String>();

                propertyNames.add(PullSOAPWebServiceDataSource.SERVICEURL_PROPERTYNAME);
                propertyNames.add(PullSOAPWebServiceDataSource.OPERATIONNAMESPACE_PROPERTYNAME);
                propertyNames.add(PullSOAPWebServiceDataSource.OPERATIONNAME_PROPERTYNAME);
                propertyNames.add(PullSOAPWebServiceDataSource.SCHEDULEDELAY_PROPERTYNAME);
                propertyNames.add(PullSOAPWebServiceDataSource.SCHEDULEPERIOD_PROPERTYNAME);
                propertyNames.add(PullSOAPWebServiceDataSource.USERNAME_PROPERTYNAME);
                propertyNames.add(PullSOAPWebServiceDataSource.PASSWORD_PROPERTYNAME);

                return propertyNames;
            }
            else
                throw new InvalidMetaPropertyException("Unexpecting meta property", null, null);
        }
        else if (dataFlowNodeClass.equals(DataProcessor.class))
        {
            if (metaProperties.isEmpty())
                return Collections.emptyList();
            else
                throw new InvalidMetaPropertyException("Unexpecting meta property", null, null);
        }
        else if (dataFlowNodeClass.equals(DataSink.class))
        {
            if (metaProperties.isEmpty())
            {
                List<String> propertyNames = new LinkedList<String>();

                propertyNames.add(PushSOAPWebServiceDataSink.SERVICEURL_PROPERTYNAME);
                propertyNames.add(PushSOAPWebServiceDataSink.OPERATIONNAMESPACE_PROPERTYNAME);
                propertyNames.add(PushSOAPWebServiceDataSink.OPERATIONNAME_PROPERTYNAME);

                return propertyNames;
            }
            else
                throw new InvalidMetaPropertyException("Unexpecting meta property", null, null);
        }
        else
            throw new InvalidClassException("Unsupported class", dataFlowNodeClass.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataFlowNode> T createDataFlowNode(String name, Class<T> dataFlowNodeClass, Map<String, String> metaProperties, Map<String, String> properties)
        throws InvalidClassException, InvalidNameException, InvalidMetaPropertyException, MissingMetaPropertyException, InvalidPropertyException, MissingPropertyException
    {
        if (dataFlowNodeClass.equals(DataSource.class))
        {
            if (metaProperties.isEmpty())
                return (T) new PullSOAPWebServiceDataSource(name, properties);
            else
                throw new InvalidMetaPropertyException("No metaproperties expected", null, null);
        }
        else if (dataFlowNodeClass.equals(DataProcessor.class))
        {
            if (metaProperties.isEmpty())
                return (T) new Document2TextDataProcessor(name, properties);
            else
                throw new InvalidMetaPropertyException("No metaproperties expected", null, null);
        }
        else if (dataFlowNodeClass.equals(DataSink.class))
        {
            if (metaProperties.isEmpty())
                return (T) new PushSOAPWebServiceDataSink(name, properties);
            else
                throw new InvalidMetaPropertyException("No metaproperties expected", null, null);
        }
        else
            throw new InvalidClassException("Unsupported class", dataFlowNodeClass.getName());
    }

    private String              _name;
    private Map<String, String> _properties;
}
