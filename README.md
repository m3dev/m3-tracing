Distributed Tracing Wrapper Libraries.

# Aim of this libraries

- Provide unified/tested set of libraries
  - No need to load/maintain various `contrib` libraries
- Provide high-level wrapper
  - No need to know detail of each tracing SDK

# Non-goal of this libraries

- Do not aim to `agent` support, _you need to activate/call this library explicitly_
  - Do not prefer to use dynamic code rewriting / monkey patching to minimize risk
  - To use feature of this library, please explicitly call/enable them
- Do not aim to support all libraries (HTTP client library, logging library, ...) in the world
  - Focus on use case in our company.
  - If you want to add library support, fork this.

# Features

## Feature matrix

| Feature                                      | Java (java-common)                             | Spring                       | Play (Scala)            |
|---------------------------------------------:| -----------------------------------------------|------------------------------| ------------------------|
| Supported runtime                            | JRE >= 8                                       | ( Same as Java )             | ( Same as Java )        |
| Capture incoming HTTP request                | Servlet >= 4.0                                 | Spring Web                   | Play ?.?                |
| Capture & Propagate to outgoing HTTP request | Commons HTTP client >= ?.?, async-http-client  | RestTemplate                 | ?????? (Play)           |
| Capture database call                        | p6spy support for JDBC/RDBMS                   | ( Same as Java )             | ( Same as Java )        |
| Multi-thread                                 | ( Custom context machanism )                   | ( Same as Java )             | Future wrapper (Play)   |
| Log correlation                              | SLF4J, Log4j                                   | ( Same as Java )             | ( Same as Java )        |
| Capture method invocation                    |                                                | method annotation AOP        | Play: ????              |
| Dependency injection                         |                                                | AutoConfiguration (Boot)     | ????                    |

Summary of each features are described below, but don't forget to see document of each language to see how to use/enable it.

## Feature: Capture incoming HTTP request

With this feature, this library automatically create trace/span for each incoming requests.

## Feature: Capture & Propagate to outgoing HTTP request

With this feature:

- This library create span for each outgoing requests
  - So that you can know duration/summary of outgoing requests
- This library adds `Trace ID` to outgoing HTTP requests
  - If destination service uses this library, we can trace request over services (-> "distributed" tracing)

## Feature: Capture database call

With this feature, this library yield spans for each database call.

## Feature: Multi-thread

Whth this feature, this library wraps multi-thread (async) operations and propagate thread-bounded trace/span to threads.

## Feature: Log correlation

With this feature, you can output `Trace ID` in logs. So that you can find related logs from/to traces.

## Feature: Capture any method invocation

With this feature, you can capture method call of your own class.

## Feature: Dependency injection

With this feature, this library provides DI configuration so that you can get objects from DI context.

