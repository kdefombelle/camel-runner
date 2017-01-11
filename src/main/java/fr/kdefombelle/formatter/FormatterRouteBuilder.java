package fr.kdefombelle.formatter;

import java.util.concurrent.atomic.AtomicInteger;

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

	public static String ROUTE_READ_INPUT_SPLIT_XML="RouteReadXmlFilesSplit";
	public static String ROUTE_SPLIT_XML="RouteSplitXmlFiles";
	public static String ROUTE_READ_INPUT_FORMATTER="RouteReadFilesFormatter";
	public static String ROUTE_FORMATTER="RouteFormatter";
	
    @Autowired
    private FreemarkerXmlModelCreator freemarkerXmlModelCreator = new FreemarkerXmlModelCreator();

    @Override
    public void configure() throws Exception {
        onException(Exception.class).log("Exception TradeId [${in.header.TradeId}]");
    	
    	from("file://{{input.xml.split.folder}}?charset=UTF-8&noop=true")
    	.routeId(ROUTE_READ_INPUT_SPLIT_XML)
    	.log("File to split[${in.header.CamelFileName}]")
    	.to("seda:splitxml");
    	
		from("seda:splitxml")
		.routeId(ROUTE_SPLIT_XML)	
		//from http://www.davsclaus.com/2011/11/splitting-big-xml-files-with-apache.html
    	.split().tokenizeXML(simple("{{input.xml.split.element}}").getText()).streaming()
    	.threads(20)
    	.setHeader("TradeId").xpath("/{{input.xml.split.element}}/TradeId/text()")
       	//TODO:add a counter
       	.setHeader(Exchange.OVERRULE_FILE_NAME, simple("${in.header.TradeId}.xml"))
    	.to("file://{{output.xml.split.folder}}?charset=UTF-8&fileExist=Override");
    	
    	from("file://{{input.folder}}?charset=UTF-8&idempotentRepository=#fileStore")
    	.routeId(ROUTE_READ_INPUT_FORMATTER)
    	.to("seda:formatter");
	
		from("seda:formatter")
		.routeId(ROUTE_FORMATTER)           
    	.log("Transforming file [${in.header.CamelFileName}]")
    	.threads(15)
        .choice()
	        .when(simple("'xml' == '{{input.type}}'"))
	       		//from http://www.davsclaus.com/2011/11/splitting-big-xml-files-with-apache.html
	         	//.split().tokenizeXML(simple("{{input.xml.split.element}}").getText()).streaming()
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
