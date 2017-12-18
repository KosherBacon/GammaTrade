package(default_visibility = ["//visibility:public"])

java_library(
    name = "tick_module",
    srcs = [
        "TickModule.java",
    ],
    deps = [
        "annotations",
        "@config//jar",
        "@guice//:compile"
    ],
    resources = [
        "//src/main/resources:application",
    ],
)

java_library(
    name = "tick",
    srcs = [
        "TickEngine.java",
    ],
    deps = [
        "annotations",
        "//proto:shared_java_proto",
        "//src/main/java/gy/jk:annotations",
        "//src/main/java/gy/jk/trade:trader",
        "//src/main/java/gy/jk/util",
        "@guice//:compile",
        "@log4j//:compile",
        "@ta4j//:compile",
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
)