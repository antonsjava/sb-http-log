
# sb-http-log


Helper utility with springboot configuration for io.github.antonsjava:log-servlet-filter logging utility.

It is used for logging text based request response messages. 

It produces following messages

 - REQ[1] GET "url" vvv
 - REQ[1] GET "url" headers["headers"] payload["payload"]
 - RES[1] GET "url" status: 200 time: 117 headers["headers"] payload["payload"]

First line is on very beggining of request log and other lines are on end of request log.

You can configure to not display any of those lines. You can configure to display or not parts of messages.

Not all headers are accesible to filter defined by this component. 

If you api uses an binary requests/responses plead configure it in a way that payload is not logged. 

depends on your logging settings you can have following messages
```
2025-12-20 14:24:13 [xec-1] DEB httplog: REQ[21] GET /author/123 vvv
....any other log from request processing
2025-92-20 14:24:13 [xec-1] DEB httplog: REQ[21] GET /author/123
2025-92-20 14:24:13 [xec-1] DEB httplog: RES[21] GET /author/123 status: 404 time: 25
```
or
```
2024-12-19 09:42:17 [xec-8] INF c.f.b.AppLog: REQ[250337] POST /foo/bar vvv
....any other log from request processing
2024-12-19 09:42:17 [xec-8] INF c.f.b.AppLog: REQ[250337] POST /foo/bar payload[{"name":"Smith"}] size: 16
2024-12-19 09:42:17 [xec-8] INF c.f.b.AppLog: RES[250337] POST /foo/bar status: 200 time: 1 payload[{"name":"Smith" "address": {"street":"Oak", "number":"123/2"}}] size: 62
```
## usage

add dependency
```
 <dependency>
     <groupId>io.github.antonsjava</groupId>
     <artifactId>sb-http-log</artifactId>
     <version>LATEST</version>
 </dependency>
```
Somewhere in your application configuration add 
```
@Configuration
@Import(sk.antons.httplog.HttpLogConfiguration.class)
public class MyAppConf {....}
```
configure logging in you environment
```
logging:
  level:
    httplog: DEBUG
```

You can configure several different case configurations for different requests.
For each case you must define at least one condition for request or response.
Prefer request condition as it is easier to evaluate them on beginning of request.

### configuration

You can disable whole configuration reading by this
```yaml
httplog:
  enabled: false # if false whole logging is disabled (default: true)
```
you can define filter order by 
```yaml
httplog:
  filterOrder: # order of log filter in processing chain (default: Integer.MAX_VALUE)
               # max value is better for request headers 
               # min value is better for response headers
```
You can define default values for configuration by this
```yaml
httplog:
  logger: http.log # name of slf4j logger to log messages (default: httplog) 
                   # you can append ':<level>' to logger name (default :denug)
  formatter: oneline  # type of output oneline/multiline (default: oneline)
  doNothing: true  # do nothing for case (default: false)

  request:
    expectedLength: 1234 # default size of buffers (default: 4000)
    identity: true # prints request user identity (default: false)
    protocol: true # prints request protocol (default: false)
    remoteAddr: true # prints request remote ip addr (default: false)
    remoteHost: true # prints request remote host name (default: false)
    prefix: REQ # prefix of message (if it is empty message is not printed) (default: REQ)
    startPrefix: REQ # prefix of start message (if it is empty message is not printed) (default: REQ)
    headers: "*" # asterix for all or list of header names coma separated (default: )
    payload: # type of paylod formatting (default: )
      type: asis # type of formatter asis/json/xml (default: )
      cutStringLiterals: 200 # cut literals for xml/json (default: )
      encoding: utf-8 # default encoding (default: utf-8)
      forceOneLine: true # force one line of payload data (default: false)
      indent: "  " # force structured output for json and xml (default: )
  response:
    expectedLength: 1234 # default size of buffers (default: 4000)
    prefix: RES # prefix of message (if it is empty message is not printed) (default: RES)
    headers: "*" # asterix for all or list of header names coma separated (default: )
    payload: # type of paylod formatting (default: )
      type: asis # type of formatter asis/json/xml (default: )
      cutStringLiterals: 200 # cut literals for xml/json (default: )
      encoding: utf-8 # default encoding (default: utf-8)
      forceOneLine: true # force one line of payload data (default: false)
      indent: "  " # force structured output for json and xml (default: )

```
 
You can define set of configuration cases whose are used to print request data.
First in order which match is used 
Each case must define condition (for request or response or both) (condition for 
response is good only complicated as it is evaluated on end of the request and 
whole stuff must be processed before evaluation.)

```yaml
httplog:
  case:
    - # individual setting for case
      condition: 
        request: any # condition for case (default: )
        response: any # condition for case (default: )
        # here you can customize default settings
    - # individual setting for case
      condition: 
        request: any # condition for case (default: )
        response: any # condition for case (default: )
      # here you can customize default settings
    - # individual setting for case
      condition: 
        request: any # condition for case (default: )
        response: any # condition for case (default: )
      # here you can customize default settings
```

example:
```yaml
httplog:
  case:
    - # do nothing
      condition:
        request: path.startsWith(/actuator)
      doNothing: true
    - # any other log just path
      condition:
        request: path.startsWith(/author) or path.startsWith(/book)
      request: 
        payload:
          type: json
          cutStringLiterals: 200 
          forceOneLine: true 
      response: 
        payload:
          type: json
          cutStringLiterals: 200 
          forceOneLine: true 
    - # any other log just path
      condition:
        request: any
```

if you do not configure any case one default is created 
```yaml
httplog:
  case:
    -
      condition:
        request: any
```
(so if you also do not change defaults only path is printed for each message.)


### conditions

 Condition is string with following elements (separated by spaces)

 - '{' bracket (surrounded by space)
 - '}' bracket (surrounded by space)
 - 'not'
 - 'or'
 - 'and'

request condition can have 
 
 - 'any'
 - 'contentType.stringoperator'
 - 'contextPath.stringoperator'
 - 'header(headername).stringoperator'
 - 'localAddr.stringoperator'
 - 'localName.stringoperator'
 - 'method.stringoperator'
 - 'param(paramname).stringoperator'
 - 'path.stringoperator'
 - 'pathInfo.stringoperator'
 - 'pathTranslated.stringoperator'
 - 'queryString.stringoperator'
 - 'remoteAddr.stringoperator'
 - 'remoteHost.stringoperator'
 - 'remoteUser.stringoperator'
 - 'schema.stringoperator'
 - 'servletPath.stringoperator'
 - 'uri.stringoperator'

response condition can have 

 - 'any'
 - 'contentType.stringoperator'
 - 'header(headername).stringoperator'
 - 'status.stringoperator'
 - 'statusOK'

stringoperator can be 

 - 'contains(value)'
 - 'endsWith(value)'
 - 'equals(value)'
 - 'equalsIgnoreCase(value)'
 - 'exists'
 - 'match(value)'
 - 'regexp(value)'
 - 'startsWith(value)'


Example: 
```
  method.equals(POST) and {
    path.startsWith(/foo/bar) 
    or header(My-Token).exists
  }
  
```

