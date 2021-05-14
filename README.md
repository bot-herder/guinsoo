# Guinsoo

![logo](public/guinsoo-app.svg)

`Guinsoo, not only a database.`

Powered by [Guinsoo Lab](https://guinsoolab.github.io/glab/).

<br/>

## Feature

* Super-fast, open source, JDBC API
* In-memory, non-blocking store, designed for low-latency applications
* Embedded and server modes; disk-based or in-memory databases
* Transaction support, multi-version concurrency
* Encrypted databases

More information: https://ciusji.github.io/guinsoo/

<br>

## Overview

Working from the top down, the layers look like this:

* [GuinsooPad](https://guinsoolab.github.io/guinsoopad/).
* JDBC driver.
* Connection/session management.
* SQL Parser.
* Command execution and planning.
* Table/Index/Constraints.
* Transactions layer.
* B-tree/ART.
* Filesystem abstraction.

<br>

## Documentation

[Guinsoo Documentation](https://ciusji.github.io/guinsoo/).

<br>

## Support

* [Issue tracker](https://github.com/ciusji/guinsoo/issues) for bug reports and feature requests