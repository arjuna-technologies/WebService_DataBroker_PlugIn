/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbplugins.webservice.xmlmonitor.endpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import com.arjuna.dbplugins.webservice.xmlmonitor.CommonDefs;
import com.arjuna.dbplugins.webservice.xmlmonitor.XMLMonitorJunction;

@WebServlet(CommonDefs.XMLMONITOR_SERVICE_PATH)
public class XMLMonitorServlet extends HttpServlet
{
    private static final long serialVersionUID = -4965307617653722876L;

    private static final Logger logger = Logger.getLogger(XMLMonitorServlet.class.getName());

    public XMLMonitorServlet()
    {
        logger.log(Level.FINE, "XMLMonitorServlet");
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException
    {
        logger.log(Level.FINE, "XMLMonitorServlet.doGet");

        try
        {
            if (_xmlMonitorJunction != null)
            {
                String id = httpServletRequest.getPathInfo().substring(1);

                logger.log(Level.FINE, "XMLMonitorServlet.doGet: id = " + id);

                if (id != null)
                {
                    Document document = _xmlMonitorJunction.withdraw(id);

                    httpServletResponse.setContentType("text/plain");
                    if (document != null)
                    {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                        PrintWriter printWriter = new PrintWriter(httpServletResponse.getWriter());
                        printWriter.println("Date: " + dateFormat.format(new Date()));
                        printWriter.println();
                        printWriter.flush();

                        DOMSource    domSource    = new DOMSource(document);
                        StreamResult streamResult = new StreamResult(httpServletResponse.getWriter());

                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer        transformer        = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                        transformer.transform(domSource, streamResult);
                        printWriter.close();
                    }
                    else
                        logger.log(Level.FINE, "XMLMonitorServlet no content");
                }
                else
                {
                    logger.log(Level.WARNING, "XMLMonitorServlet no id");
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            else
            {
                logger.log(Level.WARNING, "XMLMonitorServlet no provider XML feed junction");
                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        catch (IOException ioException)
        {
            logger.log(Level.WARNING, "XMLMonitorServlet ", ioException);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "XMLMonitorServlet ", throwable);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @EJB
    private XMLMonitorJunction _xmlMonitorJunction;
}
