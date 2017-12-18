package(default_visibility = ["//visibility:public"])

java_library(
    name = "exchange_connector",
    srcs = [
        "ExchangeConnector.java",
    ],
    deps = [
        "annotations",
        "//proto:shared_java_proto",
        "//src/main/java/gy/jk/datarecorder:trade_receiver",
        "//src/main/java/gy/jk/email:email",
        "//src/main/java/gy/jk/util:util",
        "@guava//jar",
        "@guice//:compile",
        "@log4j//:compile",
        "@xchange_core//jar",
        "@xchange_gdax//:compile",
        "@xchange_gemini//:compile",
        "@xchange_stream_core//:compile",
    ],
)

java_library(
    name = "order_engine",
    srcs = [
        "OrderEngine.java",
    ],
    deps = [
        "annotations",
        "@guice//:compile",
        "@ta4j//:compile",
        "@xchange_core//jar",
    ],
)

java_library(
    name = "trading_api",
    srcs = [
        "TradingApi.java",
    ],
    deps = [
        "//proto:shared_java_proto",
        "@guava//jar",
        "@ta4j//:compile",
        "@xchange_core//jar",
    ],
)

java_library(
    name = "trading_impl",
    srcs = [
        "GdaxTradingEngine.java",
    ],
    deps = [
        "annotations",
        "trading_api",
        "//src/main/java/gy/jk/email:email",
        "@guava//jar",
        "@guice//:compile",
        "@log4j//:compile",
        "@ta4j//:compile",
        "@xchange_gdax//:compile"
    ],
)

java_library(
    name = "exchange_module",
    srcs = [
        "ExchangeModule.java",
    ],
    deps = [
        "annotations",
        "trading_api",
        "trading_impl",
        "@config//jar",
        "@guice//:compile",
        "@xchange_core//jar",
        "@xchange_gdax//:compile",
        "@xchange_gemini//:compile",
        "@xchange_stream_core//:compile",
    ],
    resources = [
        "//src/main/resources:exchanges",
    ],
)

java_library(
    name = "annotations",
    srcs = [
        "Annotations.java",
    ],
    deps = [
        "@guice//:compile",
    ]
)