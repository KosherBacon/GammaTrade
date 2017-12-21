package(default_visibility = ["//visibility:public"])

java_library(
    name = "gamma_trade",
    srcs = [
        "GammaTrade.java",
    ],
    deps = [
        "backend_module",
        "//src/main/java/gy/jk/datarecorder:trade_receiver",
        "//src/main/java/gy/jk/datarecorder:data_recorder_module",
        "//src/main/java/gy/jk/email:email_module",
        "//src/main/java/gy/jk/exchange:exchange_connector",
        "//src/main/java/gy/jk/exchange:exchange_module",
        "//src/main/java/gy/jk/orderengine:orderengine_module",
        "//src/main/java/gy/jk/tick:tick_module",
        "//src/main/java/gy/jk/trade:trade_module",
        "@guava//jar",
        "@guice//:compile",
        "@log4j//:compile",
    ],
    resources = [
        "//src/main/resources:logger",
    ],
)

java_library(
    name = "backend_module",
    srcs = [
        "BackendModule.java",
    ],
    deps = [
        "annotations",
        "@guava//jar",
        "@guice//:compile",
    ],
)

java_library(
    name = "annotations",
    srcs = [
        "Annotations.java",
    ],
    deps = [
        "@guice//:compile",
    ],
    resources = [
        "//src/main/resources:logger",
    ],
)