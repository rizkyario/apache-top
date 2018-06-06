import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.LinkedHashMap
import scala.util.control.Breaks._
import scala.collection._

object apache_top {
	def parseLog(line: String, rules: LinkedHashMap[String, String]): Map[String, String] = 
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

	def displayLog(logs: mutable.MutableList[Map[String, String]]) =
	{
		print("\033[H\033[J")
		val size = logs.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
		val count = logs.length
		println(s"Total Hits: $count \t Total Bandwidth: $size bytes")
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
		while (true)
		{
			val logs = mutable.MutableList[Map[String, String]]()
			for (line <- Source.fromFile(filename).getLines) {
				val log = parseLog(line, combineRules)
				logs += log
			}
			displayLog(logs)
			Thread.sleep(1000)
		}
   }
}