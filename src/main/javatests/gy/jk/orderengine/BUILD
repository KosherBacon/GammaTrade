package(default_visibility = ["//visibility:public"])

java_test(
    name = "test",
    srcs = [
        "LimitOrderAsyncEngineTest.java",
    ],
    test_class = "jk.gy.orderengine.LimitOrderAsyncEngineTest",
    deps = [
        "//src/main/java/gy/jk/exchange:trading_api",
        "//src/main/java/gy/jk/orderengine:orderengine_impl",
        "//src/main/javatests/gy/jk:test_helper",
        "@junit4//jar",
        "@mockito//:compile",
        "@google_truth//:compile",
        "@xchange_core//jar",
    ],
    size = "small",
)
