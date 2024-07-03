rootProject.name = "sample-service"

include("domain")
include("application")

include("adapter:inbound:controller")
include("adapter:inbound:job")
include("adapter:inbound:listener")

include("adapter:outbound:repository")
include("adapter:outbound:producer")

include("infrastructure:mongo")
include("infrastructure:h2")

include("server:api")
include("server:batch")
include("server:consumer")

includeBuild("../build-plugin")
includeBuild("../common-library")
