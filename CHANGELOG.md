# m3-tracing Release Notes

<!--
## Unreleased
-->

## Release notes - 2.0.3 (2024-02-05)

### Changed

- Support for Spring Boot 3.2 [#12](https://github.com/m3dev/m3-tracing/pull/12)

## Release notes - 2.0.2 (2023-08-14)

### Changed

- build.gradle.kts: update plugin according to Kotlin version [#11](https://github.com/m3dev/m3-tracing/pull/11)

## Release notes - 2.0.1 (2023-06)

### Changed

- jakartaEE transition [#10](https://github.com/m3dev/m3-tracing/pull/10)

## Release notes - 1.0.6 (2020-09-18)

### Added

- servlet: update servlet for old servlet api [#9](https://github.com/m3dev/m3-tracing/pull/9)


## Release notes - 1.0.5 (2020-09-03)

### Added

- apache-httpclient: update apache-httpclinet module to handle error [#8](https://github.com/m3dev/m3-tracing/pull/8)


## Release notes - 1.0.4 (2020-08-07)

### Added

- OkHttp integration


## Release notes - 1.0.3 (2020-08-04)

### Breaking Change

- apache-httpclient: to refer an instance of `M3TracingHttpInterceptor`, use the `INSTANCE` static field instaed of `getInstance` static method

### Added

- opencensus: enable to control `publicEndpoint` settings for tracing context propagation. [#3](https://github.com/m3dev/m3-tracing/pull/3)
- spring-web: enable to add `traceparent` header taking over trace id to other services with HTTP [#4](https://github.com/m3dev/m3-tracing/pull/4)
- apache-httpclient: enable to add `traceparent` header when using apache-httpclient [#6](https://github.com/m3dev/m3-tracing/pull/6)

### Changed

- include sources in package for convenient code reading



## Release notes - 1.0.2 (2020-02-09)

## Fixed

- Commit uncomitted files


## Release notes - 1.0.1 (2019-09-19)

### Added

- apache-httpclient: enable to add `traceparent` header when using apache-httpclient [#1](https://github.com/m3dev/m3-tracing/pull/1)



## Release notes - 1.0.0 (2019-09-11)

First release

<!-- template https://keepachangelog.com/en/1.0.0/
## Release notes - x.x.x (yyyy-mm-dd)

### Added

### Changed

## Deprecated

## Removed

## Fixed

## Security
-->
