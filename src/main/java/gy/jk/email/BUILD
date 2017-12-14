package(default_visibility = ["//visibility:public"])

java_library(
    name = "email",
    srcs = [
        "Emailer.java",
        "EmailAdapter.java",
        "FakeEmailAdapter.java",
    ],
    deps = [
        "annotations",
        "@email//:compile",
        "@guice//:compile",
    ],
)

java_library(
    name = "email_module",
    srcs = [
        "EmailModule.java",
    ],
    deps = [
        "annotations",
        "email",
        "@config//jar",
        "@email//:compile",
        "@guice//:compile",
    ],
    resources = [
        "//src/main/resources:email",
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
