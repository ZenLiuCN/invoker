<p>
<img src="https://img.shields.io/badge/license-GPLv2%20CE-green?style=plastic" alt="license"/>
<img src="https://img.shields.io/badge/java-8+-yellowgreen?style=plastic" alt="java version"/>
<a href="https://central.sonatype.com/search?smo=true&q=invoker&namespace=io.github.zenliucn">
<img src="https://img.shields.io/maven-central/v/io.github.zenliucn/invoker?style=plastic" alt="maven central"/>
</a>
</p>

# Invoker

Java method, function, field to interface util.

## Benchmark

1. Each execute 100 times.
2. `xxxObject` invokes method accept an Integer.And also have box time casts.

```text
Benchmark                                     Mode  Cnt     Score    Error  Units
InvokerBenchmarkTest.direct                   avgt   18   284.397 ±  0.535  ns/op
InvokerBenchmarkTest.directObject             avgt   18   707.437 ±  1.298  ns/op
InvokerBenchmarkTest.invoker                  avgt   18   636.802 ±  2.007  ns/op
InvokerBenchmarkTest.invokerObject            avgt   18   636.011 ±  1.241  ns/op
InvokerBenchmarkTest.invokerUnder             avgt   18   659.157 ±  2.122  ns/op
InvokerBenchmarkTest.invokerUnderObject       avgt   18   740.227 ±  3.459  ns/op
InvokerBenchmarkTest.reflect                  avgt   18   940.523 ± 45.790  ns/op
InvokerBenchmarkTest.reflectObject            avgt   18  1108.931 ±  5.922  ns/op

```

# Accessor

Wrap of MethodHandle for fields.

## Benchmark

Benchmark: run with oracle JDK 1.8u333

1. `xxx` are setter method which invoke 100 times, the sheet value are divide by 100 except direct, it's something
   strange.
2. `xxxObject` are operate on Integer, there are box casts.
3. `xxxGetxxx` are getter method

```text

Benchmark                                             Mode  Cnt    Score    Error  Units
AccessorBenchmarkTest.direct                          avgt   18    7.784 ±  0.176  ns/op ???
AccessorBenchmarkTest.handle                          avgt   18  3.83862 ±  0.833  ns/op
AccessorBenchmarkTest.handleExact                     avgt   18  3.76852 ±  0.480  ns/op
AccessorBenchmarkTest.accPrimitive                    avgt   18  4.89688 ±  5.971  ns/op
AccessorBenchmarkTest.reflect                         avgt   18  4.97960 ±  2.825  ns/op
AccessorBenchmarkTest.accGeneric                      avgt   18  6.87966 ± 58.286  ns/op

AccessorBenchmarkTest.directObject                    avgt   18  2.40978 ±  6.451  ns/op
AccessorBenchmarkTest.handleObject                    avgt   18  5.60798 ±  1.981  ns/op
AccessorBenchmarkTest.reflectObject                   avgt   18  6.77643 ±  2.700  ns/op
AccessorBenchmarkTest.accSetterGenericObject          avgt   18  6.95306 ± 26.905  ns/op
AccessorBenchmarkTest.accGenericObject                avgt   18  7.15315 ±  2.707  ns/op

AccessorBenchmarkTest.directGet                       avgt   18    1.821 ±  0.018  ns/op
AccessorBenchmarkTest.handleGet                       avgt   18    6.176 ±  0.044  ns/op
AccessorBenchmarkTest.reflectGet                      avgt   18    5.706 ±  0.154  ns/op
AccessorBenchmarkTest.accGenericGet                   avgt   18    7.886 ±  0.350  ns/op
AccessorBenchmarkTest.accPrimitiveGet                 avgt   18    7.121 ±  0.075  ns/op

AccessorBenchmarkTest.directGetObject                 avgt   18    2.327 ±  0.005  ns/op
AccessorBenchmarkTest.handleGetObject                 avgt   18    5.105 ±  0.015  ns/op
AccessorBenchmarkTest.reflectGetObject                avgt   18    4.734 ±  0.115  ns/op
AccessorBenchmarkTest.accGetterGenericGetObject       avgt   18    5.691 ±  0.023  ns/op
AccessorBenchmarkTest.accGenericGetObject             avgt   18    7.116 ±  0.092  ns/op
```

+ cast back to underlying interface type would improve the performance.