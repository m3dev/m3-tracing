This repository provides distributed tracing wrapper libraries which we use in our company. 

# Aim of these libraries

- Provide unified/tested sets of libraries
  - No need to load/maintain various `contrib` libraries
- Provide high-level & simple interfaces over tracing standard APIs (e.g. OpenCensus)
  - No need to know details of APIs
  - Provide simple interfaces
  - Hide complicated object lifecycles of APIs (hide error prone things from you)

# Non-goal of these libraries

- Do not aim to provide `agent`, _you need to activate/call these libraries explicitly_
  - We do not prefer to use dynamic code rewriting / monkey patching
- Do not aim to support all kinds of libraries in the world (HTTP client library, logging library, ...)
  - We focus on usecases or scenarios appearing in our work.
  - If you want to add any kinds of library support which is not supported by us, please feel free to fork this repository.

# How to start

## Java

See [jvm/README](jvm/README.md).

# Features

## Feature matrix

| Feature                                      | Java (core)                                   | Spring                       | Play (Scala)            |
|---------------------------------------------:|-----------------------------------------------|------------------------------| ------------------------|
| Supported runtime                            | JRE >= 8 (since 2.x,  JRE >= 17 supported)    | ( Same as Java )             | ( Same as Java )        |
| Capture incoming HTTP request                | Servlet >= 3.0.1                              | Spring Web                   | Play ?.?                |
| Capture & Propagate to outgoing HTTP request | Commons HTTP client >= ?.?, async-http-client | RestTemplate                 | ?????? (Play)           |
| Capture database call                        | p6spy support for JDBC/RDBMS                  | ( Same as Java )             | ( Same as Java )        |
| Multi-thread                                 | ( Custom context machanism )                  | ( Same as Java )             | Future wrapper (Play)   |
| Log correlation                              | SLF4J, Log4j                                  | ( Same as Java )             | ( Same as Java )        |
| Capture method invocation                    |                                               | method annotation AOP        | Play: ????              |
| Dependency injection                         |                                               | AutoConfiguration (Boot)     | ????                    |

Summaries of each features are described below. Supplemental documents for each languages are provided to see how to use/enable them.

## Feature: Capture incoming HTTP requests

A trace/span is automatically created for each incoming request.

## Feature: Capture & Propagate to outgoing HTTP requests

- The library creates one span for each outgoing request, which helps to know durations/summaries of your outgoing requests
- This library adds `Trace ID` to outgoing HTTP requests
  - If destination service uses this library, we can trace requests over services (-> "distributed" tracing)

## Feature: Capture database call

The library yields spans for each database call.

## Feature: Multi-thread

The library wraps multi-thread (async) operations and propagate thread-bounded trace/span to threads.

## Feature: Log correlation

You can output `Trace ID` in logs to distinguish which trace/span logs are related.

## Feature: Capture any method invocation

You can capture method calls of your own classes.

## Feature: Dependency injection

The library provides DI configuration so that you can get objects from DI context.

## Note: Jakarta namespace support

Since 2.x, m3-tracing supports Jakarta namespace(Jakarta EE).
