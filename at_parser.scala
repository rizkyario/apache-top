import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.LinkedHashMap
import scala.util.control.Breaks._

object at_parser {
	def parseValue(line: String, rules: LinkedHashMap[String, String]): Map[String, String] = 
	{
		val re = s"${rules.values.reduce((a, b) => a + " " + b)}".r
		val values = for {
			m <- re.findAllIn(line).matchData
			(e, i) <- m.subgroups.zipWithIndex
		} yield {
			rules.keys.toSeq(i) -> e
		}
		values.map(c => c._1 -> c._2).toMap
	}
	
   	def main(args: Array[String])
	{
	   	val filename = args(0)
		val combineRules = LinkedHashMap(
			("ip", "(\\S+)"),
			("client", "(\\S+)"),
			("user", "(\\S+)"),
			("timestamp", "(\\[.+?\\])"),
			("request", "\"(.*?)\""),
			("status", "(\\d{3})"),
			("bytes", "(\\S+)"),
			("referer", "\"(.*?)\""),
			("agent", "\"(.*?)\""),
		)
		for (line <- Source.fromFile(filename).getLines) {
			println(parseValue(line, combineRules))
		}
   }
}