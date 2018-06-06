import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.{LinkedHashMap => Map}

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

		var re = buildRules(combineRules)
		for (line <- Source.fromFile(filename).getLines) {
			for(m <- re.findAllIn(line).matchData; e <- m.subgroups) 
				print(e + "|")
			println()
		}
   }
}