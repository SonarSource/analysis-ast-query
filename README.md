Pipeline Builder
================

This project was created in the context of a [Master's thesis](./MasterThesis.pdf) for EPFL by [Gabriel Fleischer](https://github.com/GabrielFleischer).

The goal of this project is to provide a tool to help create data pipelines descriptively. Its main features are:
- Descriptive: The user describes the pipeline in a kotlin DSL.
- Type-safe: The pipeline is type-checked at compile-time.
- Optimized: The pipeline is optimized before execution.
- Reusability: Once optimized, the pipeline can be reused with different inputs.

This tool is useful in contexts where many different logics need to be applied on an object multiple times.

A demonstration of the tool can be found is available on the sonar-java project [here](https://github.com/SonarSource/analysis-ast-query/)