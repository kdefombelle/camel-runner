Welcome to Formatter!
===================


#### Introduction

 **Formatter** transforms your input file into any format using templates.
 It currently supports  XML input and can be easily enriched.
 It is leveraging [Apache Camel][1] framework and [Apache Freemarker][2] template engine.

 Application is assembled with [Spring framework][3].


#### Quick start
> **Configure your formatter.properties:**
```
input.type=xml
input.folder=src/test/resources/input/irs
output.extension=csv
output.folder=src/test/resources/output/irs
output.mode=override
template.file=template/irs.ftl
```
> **Run:**
```
bin/formatter.sh -p 1617 -c start
```

#### Commands
##### Start
 To start **Formatter** on Linux or Mac OS
 ```
 bin/formatter.sh -p 1617 -c start
 ```
##### Stop
 To stop **Formatter** on Linux or Mac OS
 ```
 bin/formatter.sh -p 1617 -c stop
 ```

##### Help
To get below **Formatter** help message on Linux or Mac OS
```
bin/formatter.sh -h

Option              Description
------              -----------
-c, --command       command start/stop
-h, --help          print this help message
--host, --hostname  host name
-p, --port          jmx port

----------
```

[1]: http://camel.apache.org/ "Apache Camel"
[2]: http://freemarker.org/ "Apache Freemarker"
[3]: https://projects.spring.io/spring-framework/ "Spring framework"
ECHO is on.
