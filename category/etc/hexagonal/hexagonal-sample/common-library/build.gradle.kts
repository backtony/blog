task("clean") {
    subprojects.forEach {
        it.afterEvaluate {
            val cleanTask = it.tasks.findByName("clean")
            if (cleanTask != null) {
                dependsOn(cleanTask)
            }
        }
    }
}

task("test") {
    subprojects.forEach {
        it.afterEvaluate {
            val testTask = it.tasks.findByName("test")
            if (testTask != null) {
                dependsOn(testTask)
            }
        }
    }
}

task("lint") {
    subprojects.forEach {
        it.afterEvaluate {
            val lintTask = it.tasks.findByName("ktlintCheck")
            if (lintTask != null) {
                dependsOn(lintTask)
            }
        }
    }
}

task("ktlintFormat") {
    subprojects.forEach {
        it.afterEvaluate {
            val lintTask = it.tasks.findByName("ktlintFormat")
            if (lintTask != null) {
                dependsOn(lintTask)
            }
        }
    }
}

subprojects {
    group = "com.sample.hexagonal.common"
    version = "FIXED"
}
