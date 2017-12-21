package(default_visibility = ["//visibility:public"])

java_library(
    name = "trade_module",
    srcs = [
        "TradeModule.java",
    ],
    deps = [
        "annotations",
        "//src/main/java/gy/jk/strategy",
        "//src/main/java/gy/jk/strategy:strategy_builder",
        "@config//jar",
        "@guice//:compile",
        "@ta4j//:compile",
        "@xchange_core//jar"
    ],
    resources = [
        "//src/main/resources:application",
    ]
)

java_library(
    name = "trader",
    srcs = [
        "Trader.java",
    ],
    deps = [
        "annotations",
        "//src/main/java/gy/jk/orderengine:orderengine",
        "//src/main/java/gy/jk/strategy:strategy_builder",
        "@guice//:compile",
        "@log4j//:compile",
        "@ta4j//:compile",
        "@xchange_core//jar"
    ],
)

java_library(
    name = "annotations",
    srcs = [
        "Annotations.java",
    ],
    deps = [
        "@guice//:compile"
    ],
)