import scala.io.Source

val filename = "apache.log"
for (line <- Source.fromFile(filename).getLines) {
    println(line)
}