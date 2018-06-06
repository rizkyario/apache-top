import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{LinkedHashMap => Map}
import scala.util.control.Breaks._

object at_parser {
	def buildRules(rules: Map[String, String]): Regex = 
	{
		var rule = ""
		var i = 0
	
		for ((k, v) <- rules)
		{
			if (i > 0) rule += " "
			rule += v
			i = i + 1
		}
		(s"$rule".r)
	}

	def parseValue(line: String, rules: Map[String, String]): Map[String, String] = 
	{
		var keys = Seq[String]()
		var values = Map[String, String]()
		var i = 0
		var re = buildRules(rules)

		for (key <- rules.keys) keys = keys:+ key
		for (m <- re.findAllIn(line).matchData; e <- m.subgroups) 
		{
			values += ((keys(i) -> e))
			i += 1
		}
		println(values)
		values
	}
	
   	def main(args: Array[String]) {
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