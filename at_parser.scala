import scala.util.matching.Regex
import scala.io.Source
import scala.util.control.Breaks._

object at_parser {
   def main(args: Array[String]) {
		val filename = args(0)
		val ip = "(\\S+)"
		val client = "(\\S+)"
		val user = "(\\S+)"
		val timestamp = "(\\[.+?\\])"
		val request = "\"(.*?)\""
		val status = "(\\d{3})"
		val bytes = "(\\S+)"
		val referer = "\"(.*?)\""
		val agent = "\"(.*?)\""
		val re = s"$ip $client $user $timestamp $request $status $bytes $referer $agent".r

		for (line <- Source.fromFile(filename).getLines) {
			for(m <- re.findAllIn(line).matchData; e <- m.subgroups) 
				print(e + "|")
			println()
		}
   }
}