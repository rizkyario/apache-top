import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.{LinkedHashMap => Map}
import scala.util.control.Breaks._

object at_parser {
	def parseValue(line: String, rules: Map[String, String]): Map[String, String] = 
	{
		var values = Map[String, String]()
		var i = 0
		val re = s"${rules.values.reduce((a, b) => a + " " + b)}".r

		for (m <- re.findAllIn(line).matchData; e <- m.subgroups) 
		{
			values += ((rules.keys.toSeq(i) -> e))
			i += 1
		}
		println(values)
		values
	}
	
   	def main(args: Array[String])
	{
	   	val filename = args(0)
		val combineRules = Map(
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