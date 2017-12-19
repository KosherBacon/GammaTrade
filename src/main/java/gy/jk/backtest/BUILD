package(default_visibility = ["//visibility:public"])

java_library(
    name = "backtester",
    srcs = [
        "Backtester.java",
    ],
    deps = [
        "annotations",
        "backtest_data_loader",
        "backtest_module",
        "//proto:shared_java_proto",
        "//src/main/java/gy/jk:backend_module",
        "//src/main/java/gy/jk/strategy:strategy_builder",
        "@guice//:compile",
        "@log4j//:compile",
        "@ta4j//:compile",
    ],
)

java_library(
    name = "backtest_data_loader",
    srcs = [
        "BacktestDataLoader.java",
    ],
    deps = [
        "//src/main/java/gy/jk/strategy:strategy_builder",
        "//src/main/java/gy/jk/tick:annotations",
        "@guice//:compile",
        "@log4j//:compile",
        "@opencsv//:compile",
        "@ta4j//:compile"
    ],
)

java_library(
    name = "backtest_module",
    srcs = [
        "BacktestModule.java",
    ],
    deps =[
        "annotations",
        "//src/main/java/gy/jk/strategy",
        "//src/main/java/gy/jk/strategy:strategy_builder",
        "//src/main/java/gy/jk/tick:annotations",
        "@config//jar",
        "@guice//:compile",
        "@ta4j//:compile",
    ],
    resources = [
        "//data:trade_data",
        "//src/main/resources:backtest"
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
