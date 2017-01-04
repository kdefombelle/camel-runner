package fr.kdefombelle.formatter;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author kdefombelle
 */
@Component
public class FormatterRouteBuilder extends RouteBuilder{

	public static String ROUTE_FORMATTER="RouteFormatter";
	
    @Autowired
    private FreemarkerXmlModelCreator freemarkerXmlModelCreator = new FreemarkerXmlModelCreator();

    @Override
    public void configure() throws Exception {
        onException(Exception.class).log("Exception TradeId [${in.header.TradeId}]");
    	
    	from("file://{{input.folder}}?charset=UTF-8&noop=true").to("seda:processing").routeId("RouteReadFiles");
        
        from("seda:processing").routeId(ROUTE_FORMATTER)           
        		.log("Processing file [${in.header.CamelFileName}]")
                .choice()
	            	.when(simple("'xml' == '{{input.type}}'"))
	            		//from http://www.davsclaus.com/2011/11/splitting-big-xml-files-with-apache.html
	            		.split().tokenizeXML(simple("{{input.xml.split}}").getText()).streaming()
	            		.threads(20)
	            		.bean(freemarkerXmlModelCreator).id("ModelCreator")
		                .to("freemarker:{{template.file}}")
		                .choice()
		                	.when(simple("'append' == '{{output.mode}}'"))
		                		.log(LoggingLevel.DEBUG,"Formatter configured in [{{output.mode}}] mode")
		                		.log("TradeId [${in.header.TradeId}]")
			                	.setHeader(Exchange.OVERRULE_FILE_NAME, simple("report.{{output.extension}}"))
				                .to("file:{{output.folder}}?charset=UTF-8&fileExist=Append")
				            .when(simple("'override' == '{{output.mode}}'"))
			                	.log(LoggingLevel.DEBUG,"Formatter configured in [{{output.mode}}] mode")
			                	.setHeader(Exchange.OVERRULE_FILE_NAME, simple("${headers.tradeId}.{{output.extension}}"))
			                	.log("TradeId [${in.header.TradeId}]")
			                	.to("file:{{output.folder}}?charset=UTF-8&fileExist=Override")
			        		.otherwise().throwException(new IllegalStateException("output mode not supported"))
        			.otherwise().throwException(new IllegalStateException("input.type not supported"));
        
    }
}
