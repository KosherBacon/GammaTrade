package(default_visibility = ["//visibility:public"])

java_library(
    name = "annotations",
    srcs = [
        "Annotations.java",
    ],
    deps = [
        "@guice//:compile"
    ],
)

java_library(
    name = "data_recorder_module",
    srcs = [
        "DataRecorderModule.java"
    ],
    deps = [
        "annotations",
        "data_store",
        "//proto:shared_java_proto",
        "@guice//:compile",
    ],
)

java_library(
    name = "data_store",
    srcs = [
        "DataStore.java",
        "LocalDataStore.java",
    ],
    deps = [
        "annotations",
        "//proto:shared_java_proto",
        "@guice//:compile",
        
    ],
)

java_library(
    name = "trade_receiver",
    srcs = [
        "TradeReceiver.java",
    ],
    deps = [
        "data_store",
        "//proto:shared_java_proto",
        "//src/main/java/gy/jk/tick",
        "@guava//jar",
        "@guice//:compile",
        "@xchange_gdax//:compile",
    ],
)
