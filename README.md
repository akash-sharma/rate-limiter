# rate-limiter

Use cases of a Rate limiter : 
(1) You want to limit the usage of an api so that your downstream resources stay healthy and does not exhaust on high TPS. Every api has some maximum threshold limit of TPS that can bear without any system resource failure.

(2) Rate limiter will help in avoiding cascading effect on downstream microservices. A service might be calling other microservices for pulling data and heavy load on a single service will have high load on its downstream services also.

(3) You want your api to be used as paid api. Then you can have separate limits of free version and paid version. For example : 200 request in 1 minute for free and after that it will paid.

(4) There are some apis which provide coupons or promocodes or cashback offers to users. These kind of apis should have a Rate limiter so that even if you have a bug in your system, you could avoid the state of heavy cash burn.

(5) Disadvantage of not using Rate limiter : If one of your api exceed its threashold, more system resources like CPU, memory and threads will be consumed to serve extra requests. For example thread timeouts on ElasticSearch will increase thread usage count as a result CPU consumption will increase. So new requests will not be served and this may cause ELB 5xx. This will downgrade the performance of your own service. Also it will have a cascading effect on upstream services or clients.
