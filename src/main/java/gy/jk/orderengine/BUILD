package(default_visibility = ["//visibility:public"])

java_library(
    name = "orderengine",
    srcs = [
        "OrderEngine.java",
    ],
    deps = [
        "@guava//jar",
        "@xchange_core//jar",
    ],
)

java_library(
    name = "orderengine_impl",
    srcs = [
        "LimitOrderAsyncEngine.java",
        "MarketOrderEngine.java",
    ],
    deps = [
        "lastorderengine",
        "orderengine",
        "//src/main/java/gy/jk/exchange:annotations",
        "//src/main/java/gy/jk/exchange:trading_api",
        "//src/main/java/gy/jk/trade:annotations",
        "@guava//jar",
        "@guice//:compile",
        "@log4j//:compile",
        "@xchange_core//jar",
    ],
)

java_library(
    name = "lastorderengine",
    srcs = [
        "LastOrderEngine.java",
    ],
    deps = [
        "//src/main/java/gy/jk/exchange:trading_api",
        "@guava//jar",
        "@guice//:compile",
        "@log4j//:compile",
        "@xchange_core//jar",
    ]
)

java_library(
    name = "orderengine_module",
    srcs = [
        "OrderEngineModule.java"
    ],
    deps = [
        "orderengine",
        "orderengine_impl",
        "@config//jar",
        "@guice//:compile",
    ]
)