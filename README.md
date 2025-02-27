BreninSul RestTemplate interceptor with Spring Boot starter.

Logging interceptor implementation and stater to auto register it in Spring context

Client is not registered in this starter. Interceptor should be manually added by

````kotlin
    RestTemplateBuilder().interceptors(filtersList)
````


| Parameter                                                                        | Type             | Description                                                             |
|----------------------------------------------------------------------------------|------------------|-------------------------------------------------------------------------|
| `rest-template.logging-interceptor.enabled`                                      | Boolean          | Enable autoconfig for this starter                                      |
| `rest-template.logging-interceptor.logging-level`                                | JavaLoggingLevel | Logging level of messages                                               |
| `rest-template.logging-interceptor.max-body-size`                                | Int              | Max logging body size                                                   |
| `rest-template.logging-interceptor.order`                                        | Int              | Filter order (Ordered interface)                                        |
| `rest-template.logging-interceptor.new-line-column-symbols`                      | Int              | How many symbols in first column (param name)                           |
| `rest-template.logging-interceptor.request.id-included`                          | Boolean          | Is request id included to log message (request)                         |
| `rest-template.logging-interceptor.request.uri-included`                         | Boolean          | Is uri included to log message (request)                                |
| `rest-template.logging-interceptor.request.took-time-included`                   | Boolean          | Is timing included to log message (request)                             |
| `rest-template.logging-interceptor.request.headers-included`                     | Boolean          | Is headers included to log message (request)                            |
| `rest-template.logging-interceptor.request.body-included`                        | Boolean          | Is body included to log message (request)                               |
| `rest-template.logging-interceptor.request.mask.mask-headers`                    | String           | Comma separated headers to mask in logs (request)                       |
| `rest-template.logging-interceptor.request.mask.mask-query-parameters`           | String           | Comma separated query parameters to mask in logs (request/response)     |
| `rest-template.logging-interceptor.request.mask.mask-mask-json-body-keys`        | String           | Comma separated body json keys(fields) to mask in logs (request)        |
| `rest-template.logging-interceptor.request.mask.mask-mask-form-urlencoded-body`  | String           | Comma separated form urlencoded keys(fields) to mask in logs (request)  |
| `rest-template.logging-interceptor.response.id-included`                         | Boolean          | Is request id included to log message (response)                        |
| `rest-template.logging-interceptor.response.uri-included`                        | Boolean          | Is uri included to log message (response)                               |
| `rest-template.logging-interceptor.response.took-time-included`                  | Boolean          | Is timing included to log message (response)                            |
| `rest-template.logging-interceptor.response.headers-included`                    | Boolean          | Is headers included to log message (response)                           |
| `rest-template.logging-interceptor.response.body-included`                       | Boolean          | Is body included to log message (response)                              |
| `rest-template.logging-interceptor.response.mask.mask-headers`                   | String           | Comma separated headers to mask in logs (response)                      |
| `rest-template.logging-interceptor.response.mask.mask-mask-json-body-keys`       | String           | Comma separated body json keys(fields) to mask in logs (response)       |
| `rest-template.logging-interceptor.response.mask.mask-mask-form-urlencoded-body` | String           | Comma separated form urlencoded keys(fields) to mask in logs (response) |



You can additionally configure logging for each request by passing headers from `io.github.breninsul.rest.logging.RestTemplateConfigHeaders` to request


add the following dependency:

````kotlin
dependencies {
//Other dependencies
    implementation("io.github.breninsul:rest-template-logging-interceptor:2.0.0")
//Other dependencies
}

````
### Example of log messages

````
===========================CLIENT RestTemplate Request begin===========================
=ID           : 1613-55
=URI          : POST https://test-c.free.beeceptor.com/
=Headers      : headerfirst:HeaderValueFirst;headersecond:HeaderValueSecond
=Body         : {"someKey":"someval"}
===========================CLIENT RestTemplate Request end  ===========================

===========================CLIENT RestTemplate Response begin===========================
=ID           : 1613-55
=URI          : 200 POST https://test-c.free.beeceptor.com/
=Took         : 5897 ms
=Headers      : access-control-allow-origin:*;alt-svc:h3=":443"; ma=2592000;content-type:text/plain;date:Tue, 18 Jun 2024 07:32:35 GMT;vary:Accept-Encoding
=Body         :
Hey ya! Great to see you here. Btw, nothing is configured for this request path. Create a rule and start building a mock API.

===========================CLIENT RestTemplate Response end  ===========================
````


