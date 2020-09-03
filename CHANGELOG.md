# m3-tracing Release Notes

<!--
## Unreleased
-->

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
