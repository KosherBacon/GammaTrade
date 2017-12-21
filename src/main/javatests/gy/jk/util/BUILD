package(default_visibility = ["//visibility:public"])

java_test(
    name = "test",
    srcs = [
        "NumberConversionTest.java",
    ],
    test_class = "jk.gy.util.NumberConversionTest",
    deps = [
        "//src/main/java/gy/jk/util:util",
        "//proto:shared_java_proto",
        "@junit4//jar",
        "@mockito//:compile",
        "@google_truth//:compile",
#        "@junit//:compile",
    ],
    size = "small",
)
