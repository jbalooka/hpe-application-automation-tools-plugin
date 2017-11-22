
/*
 * MIT License
 * Copyright (c) 2016 Hewlett-Packard Development Company, L.P.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hpe.application.automation.tools.results;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hudson.model.Action;
import hudson.model.Run;

/**
 * Created by betzalel on 28/06/2015.
 */

public class HtmlBuildReportAction implements Action {
    private static final String REPORTMETADATE_XML = "report_metadata.xml";
    private Map<String, Integer> classnames;
    private String displayName;
    private String displayUrlName;
    private Run build;
    private List<ReportMetaData> reportMetaDataList = new ArrayList<ReportMetaData>();


    //public HtmlBuildReportAction(AbstractBuild<?, ?> build, BuildListener listener, List<ReportMetaData> reportMetaData)
    //NOTE: if parameter has BuildListener, the build cannot be serilize normally.

    public HtmlBuildReportAction(Run<?, ?> build) throws IOException, SAXException, ParserConfigurationException {
        this.build = build;
		
        File reportMetaData_XML = new File(build.getRootDir(), REPORTMETADATE_XML);
        if (reportMetaData_XML.exists()) {
            readReportFromXMLFile(reportMetaData_XML.getAbsolutePath(), this.reportMetaDataList);
        }

    }

	@SuppressWarnings("squid:S1452")
    public final Run<?, ?> getBuild() {
        return build;
    }

    protected File reportFile() {
        return getBuildHtmlReport(this.build);
    }

    private File getBuildHtmlReport(Run run) {
		return new File(new File(new File(run.getRootDir(),"archive"), "UFTReport"), "index.html");
    }

    @Override
    public String getDisplayName() {
		return displayName;
    }

    @Override
    public String getUrlName() {
       	return displayUrlName;
    }

    @Override
    public String getIconFileName() {
		return "/plugin/hp-application-automation-tools-plugin/icons/24x24/uft_report.png";
    }

    // other property of the report
    public List<ReportMetaData> getAllReports() {
        return this.reportMetaDataList;
    }


    private void readReportFromXMLFile(String filename, List<ReportMetaData> listReport) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
		builder = dbf.newDocumentBuilder();
		doc = builder.parse(filename);
		classnames = Collections.synchronizedMap(new HashMap<String, Integer>());
		Integer nClassnameOccurences = 0;
		
        Element root = doc.getDocumentElement();
        NodeList reportList = root.getElementsByTagName("report");
        for (int i = 0; i < reportList.getLength(); i++) {
            ReportMetaData reportmetadata = new ReportMetaData();
            Element report = (Element) reportList.item(i);
            String disPlayName = report.getAttribute("disPlayName");
            String urlName = report.getAttribute("urlName");
            String resourceURL = report.getAttribute("resourceURL");
            String dateTime = report.getAttribute("dateTime");
            String status = report.getAttribute("status");
            String isHtmlreport = report.getAttribute("isHtmlreport");
            String classname = report.hasAttribute("classname") ? 
            		report.getAttribute("classname") : "UFT Report " + i;
            
            reportmetadata.setDisPlayName(disPlayName);
            reportmetadata.setUrlName(urlName);
            reportmetadata.setResourceURL(resourceURL);
            reportmetadata.setDateTime(dateTime);
            reportmetadata.setStatus(status);
            reportmetadata.setIsHtmlReport("true".equals(isHtmlreport));
            reportmetadata.setClassname(classname);
            listReport.add(reportmetadata);
            
            nClassnameOccurences = classnames.containsKey(classname) ? classnames.remove(classname) + 1 : 1;
            classnames.put(classname, nClassnameOccurences);
        }
        
        Integer currentMax = 0;
        for (String classname : classnames.keySet()) {
        	nClassnameOccurences = classnames.get(classname);
        	
        	if(nClassnameOccurences > currentMax) {
        		displayName = classname;
        		currentMax = nClassnameOccurences;
        	}
        }

        displayUrlName = displayName.replace('.', '_').replace(' ', '_').replace('%', '%');
    }
}
