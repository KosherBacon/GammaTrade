package(default_visibility = ["//visibility:public"])

java_library(
    name = "strategy_builder",
    srcs = [
        "StrategyBuilder.java",
    ],
    deps = [
        "@ta4j//:compile",
    ]
)

java_library(
    name = "strategy_module",
    srcs = [
        "StrategyModule.java",
    ],
    deps = [
        "strategy",
        "@guice//:compile",
    ],
)

java_library(
    name = "strategy",
    srcs = [
        "PrimaryStrategy.java",
        "RSIStrategy.java",
    ],
    deps = [
        "strategy_builder",
        "@guice//:compile",
        "@ta4j//:compile",
    ],
)