import java.io.*

tasks.register("generateChangelog") {
    doFirst {
        val changelogLines = File("${projectDir}/src/main/res/raw/changelog.json")
            .readLines().drop(3).dropLast(1)
            .map { it.trim().replace("\"|,|<br?\\/>|]".toRegex(), "").replace("•","-") }
        val changeLogText = changelogLines.joinToString(separator = "\n")
        val writer = FileWriter("${projectDir}/build/changelog.md")
        writer.write(changeLogText)
        writer.flush()
        writer.close()
    }
}