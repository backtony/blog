rootProject.name = "common-library"

include("json")
include("utils")
include("exception")
include("actuator")
include("kafka-consumer")
include("kafka-producer")

includeBuild("../build-plugin")
