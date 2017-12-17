# Snapshot repository
SONATYPE_NEXUS_REPOSITORY = "http://oss.sonatype.org/content/repositories/snapshots"

# Add rules_maven repo to get a few additional features here.
git_repository(
  name = "org_pubref_rules_maven",
  remote = "https://github.com/pubref/rules_maven",
  commit = "43d56ae", # replace with latest version
)
load("@org_pubref_rules_maven//maven:rules.bzl", "maven_repositories", "maven_repository")
maven_repositories()

maven_repository(
  name = 'guice',
  deps = [
    'com.google.inject:guice:4.1.0',
  ],
  transitive_deps = [
    '0235ba8b489512805ac13a8f9ea77a1ca5ebe3e8:aopalliance:aopalliance:1.0',
    '6ce200f6b23222af3d8abb6b6459e6c44f4bb0e9:com.google.guava:guava:19.0',
    'eeb69005da379a10071aa4948c48d89250febb07:com.google.inject:guice:4.1.0',
    '6975da39a7040257bd51d21a231b76c915872d38:javax.inject:javax.inject:1',
  ],
)
load("@guice//:rules.bzl", "guice_compile")
guice_compile()

maven_jar(
    name = "guava",
    artifact = "com.google.guava:guava:18.0",
    sha1 = "cce0823396aa693798f8882e64213b1772032b09",
)

maven_jar(
    name = "config",
    artifact = "com.typesafe:config:1.3.2",
    sha1 = "d6ac0ce079f114adce620f2360c92a70b2cb36dc",
)

maven_repository(
    name = 'email',
    deps = [
        'org.simplejavamail:simple-java-mail:4.4.5',
    ],
    transitive_deps = [
        'f7be08ec23c21485b9b5a1cf1654c2ec8c58168d:com.google.code.findbugs:jsr305:3.0.1',
        'ffcd34b5de820f35bcc9303649cf6ab2c65ad44e:com.sun.mail:javax.mail:1.5.5',
        'e6cb541461c2834bdea3eb920f1884d1eb508b50:javax.activation:activation:1.1',
        '2dae40a946e86c1543fd8a55538992518ad0d92e:org.simplejavamail:simple-java-mail:4.4.5',
        '139535a69a4239db087de9bab0bee568bf8e0b70:org.slf4j:slf4j-api:1.7.21',
    ],
)
load("@email//:rules.bzl", "email_runtime", "email_default")
email_default()

maven_repository(
    name = 'log4j',
    deps = [
        'org.slf4j:slf4j-simple:1.7.25',
    ],
)
load("@log4j//:rules.bzl", "log4j_runtime", "log4j_default")
log4j_default()

maven_repository(
    name = 'opencsv',
    deps = [
        'com.opencsv:opencsv:4.1',
    ],
    force = [
        'org.apache.commons:commons-lang3:3.6',
    ],
    transitive_deps = [
        '5d0a1c5f5e94c9fc00a7e6c886e2d2a1511ea95e:com.opencsv:opencsv:4.1',
        'c845703de334ddc6b4b3cd26835458cb1cba1f3d:commons-beanutils:commons-beanutils:1.9.3',
        '8ad72fe39fa8c91eaaf12aadb21e0c3661fe26d5:commons-collections:commons-collections:3.2.2',
        '4bfc12adfe4842bf07b657f0369c4cb522955686:commons-logging:commons-logging:1.2',
        '9d28a6b23650e8a7e9063c04588ace6cf7012c17:org.apache.commons:commons-lang3:3.6',
        'c336bf600f44b88af356c8a85eef4af822b06a4d:org.apache.commons:commons-text:1.1',
    ],
)
load("@opencsv//:rules.bzl", "opencsv_runtime", "opencsv_default")
opencsv_default()

maven_repository(
    name = 'ta4j',
    deps = [
        'org.ta4j:ta4j-core:0.10',
        'org.slf4j:slf4j-simple:1.7.25',
    ],
    transitive_deps = [
        'da76ca59f6a57ee3102f8f9bd9cee742973efa8a:org.slf4j:slf4j-api:1.7.25',
        '8dacf9514f0c707cbbcdd6fd699e8940d42fb54e:org.slf4j:slf4j-simple:1.7.25',
        '3152443539a191024e924e38eaa2588a0c1e9b95:org.ta4j:ta4j-core:0.10',
    ],
)
load("@ta4j//:rules.bzl", "ta4j_runtime", "ta4j_default")
ta4j_default()

maven_jar(
    name = "xchange_core",
    artifact = "org.knowm.xchange:xchange-core:4.3.1",
    sha1 = "b8f53d4f4c8b647913f0485f6792665a0e9db62e",
)

maven_repository(
    name = 'xchange_gdax',
    deps = [
        'org.knowm.xchange:xchange-gdax:4.3.1',
        'info.bitrich.xchange-stream:xchange-gdax:4.3.0',
        'org.slf4j:slf4j-simple:1.7.25',
    ],
    force = [
        'org.knowm.xchange:xchange-gdax:4.3.1',
        'org.knowm.xchange:xchange-core:4.3.1',
        'org.slf4j:slf4j-api:1.7.25',
        'com.fasterxml.jackson.core:jackson-databind:2.9.1',
    ],
)
load("@xchange_gdax//:rules.bzl", "xchange_gdax_runtime", "xchange_gdax_default")
xchange_gdax_default()

maven_repository(
    name = 'xchange_gemini',
    deps = [
        'info.bitrich.xchange-stream:xchange-gemini:4.3.0',
        'org.slf4j:slf4j-simple:1.7.25',
    ],
    force = [
        'org.slf4j:slf4j-api:1.7.25',
        'com.fasterxml.jackson.core:jackson-databind:2.9.1'
    ],
)
load("@xchange_gemini//:rules.bzl", "xchange_gemini_runtime", "xchange_gemini_default")
xchange_gemini_default()

maven_repository(
    name = 'xchange_stream_core',
    deps = [
        'info.bitrich.xchange-stream:xchange-stream-core:4.3.0',
        'org.slf4j:slf4j-simple:1.7.25',
    ],
    force = [
        'org.slf4j:slf4j-api:1.7.25',
    ],
)
load("@xchange_stream_core//:rules.bzl", "xchange_stream_core_runtime", "xchange_stream_core_default")
xchange_stream_core_default()

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    urls = ["https://github.com/google/protobuf/archive/98836a56e616f3bc387e3c66133b1ad320f36d80.zip"],
    strip_prefix = "protobuf-98836a56e616f3bc387e3c66133b1ad320f36d80",
    sha256 = "2fa7b21d90558ac4986518ff0547c598382dd3976b37b9a77a5998f3a2c2b9bc",
)

# cc_proto_library rules implicitly depend on @com_google_protobuf_cc//:cc_toolchain,
# which is the C++ proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_cc",
    urls = ["https://github.com/google/protobuf/archive/98836a56e616f3bc387e3c66133b1ad320f36d80.zip"],
    strip_prefix = "protobuf-98836a56e616f3bc387e3c66133b1ad320f36d80",
    sha256 = "2fa7b21d90558ac4986518ff0547c598382dd3976b37b9a77a5998f3a2c2b9bc",
)

# java_proto_library rules implicitly depend on @com_google_protobuf_java//:java_toolchain,
# which is the Java proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_java",
    urls = ["https://github.com/google/protobuf/archive/98836a56e616f3bc387e3c66133b1ad320f36d80.zip"],
    strip_prefix = "protobuf-98836a56e616f3bc387e3c66133b1ad320f36d80",
    sha256 = "2fa7b21d90558ac4986518ff0547c598382dd3976b37b9a77a5998f3a2c2b9bc",
)

maven_jar(
    name = "junit4",
    artifact = "junit:junit:4.12",
)

maven_jar(
    name = "mockito",
    artifact = "org.mockito:mockito-all:1.10.19",
)

#maven_repository(
#    name = 'junit',
#    deps = [
#        'junit:junit:4.12',
#    ],
#    transitive_deps = [
#        '2973d150c0dc1fefe998f834810d68f278ea58ec:junit:junit:4.12',
#        '42a25dc3219429f0e5d060061f71acb49bf010a0:org.hamcrest:hamcrest-core:1.3',
#    ],
#)
#load("@junit//:rules.bzl", "junit_runtime", "junit_default")
#junit_default()

maven_repository(
    name = 'google_truth',
    deps = [
        'com.google.truth:truth:0.36',
    ],
    force = [
        'com.google.errorprone:error_prone_annotations:2.0.19',
    ],
    transitive_deps = [
        '40719ea6961c0cb6afaeb6a921eaa1f6afd4cfdf:com.google.code.findbugs:jsr305:1.3.9',
        'c3754a0bdd545b00ddc26884f9e7624f8b6a14de:com.google.errorprone:error_prone_annotations:2.0.19',
        '7b2fc7ef242f35bc2c9a3b816ed846c9345e3f95:com.google.guava:guava:22.0-android',
        'ed28ded51a8b1c6b112568def5f4b455e6809019:com.google.j2objc:j2objc-annotations:1.1',
        '7485219d2c1d341097a19382c02bde07e69ff5d2:com.google.truth:truth:0.36',
        '2973d150c0dc1fefe998f834810d68f278ea58ec:junit:junit:4.12',
        '775b7e22fb10026eed3f86e8dc556dfafe35f2d5:org.codehaus.mojo:animal-sniffer-annotations:1.14',
        '42a25dc3219429f0e5d060061f71acb49bf010a0:org.hamcrest:hamcrest-core:1.3',
    ],
)
load("@google_truth//:rules.bzl", "google_truth_runtime", "google_truth_default")
google_truth_default()
