import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.LinkedHashMap
import scala.util.control.Breaks._
import scala.collection._
import java.io.File

object apache_top {
	def parseLog(line: String, rules: LinkedHashMap[String, String]): Map[String, String] = 
	{
		val regexRule = rules.values.reduce((a, b) => a + " " + b)
		val re = s"$regexRule".r
		(for {
			m <- re.findAllIn(line).matchData
			(e, i) <- m.subgroups.zipWithIndex
		} yield {
			rules.keys.toSeq(i) -> e
		}) toMap
	}

	def displayVisitorLog(logs: mutable.MutableList[Map[String, String]]) =
	{
		val days = (for
		{
			(key, xs) <- logs.groupBy(_.get("timestamp"))
		} yield (key, xs.length)).toSeq.sortWith(_._2 > _._2)

		for ((url, i) <- days.zipWithIndex)
		{
			if (i < 10)
				println(f"${url._2}: ${url._1}")
		}
	}

	def displayRequestLog(logs: mutable.MutableList[Map[String, String]]) =
	{
		val uUrls = (for
		{
			(key, xs) <- logs.groupBy(_.get("request"))
		} yield (key, xs.length)).toSeq.sortWith(_._2 > _._2)

		for ((url, i) <- uUrls.zipWithIndex)
		{
			if (i < 10)
				println(f"${url._2}: ${url._1}")
		}
	}

	def displayLog(filename: String, logs: mutable.MutableList[Map[String, String]]) =
	{
		print("\033[H\033[J")
		val fileSize = new File(filename).length
		val size = logs.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
		val failedRequests = logs.filter((log)=>(log("status").toInt >= 400)).size
		val validRequests = logs.filter((log)=>(log("status").toInt < 400)).size
		val visitors = (for (log <- logs) yield (log("ip")))
		val referrers = (for (log <- logs) yield (log("referrer")))
		val urls = (for (log <- logs) yield (log("request").split(" ")(1)))
		val req404s = for (log <- logs; if log("status").toInt == 404 ) yield {log("status") -> log("request")}

		println("\n** APACHE TOP Overall Analysed Requests **\n")

		println(f"Total Request   ${logs.length   }%-6d  Unique Visitors  ${visitors.distinct.length}%-6d        Referrers   ${referrers.distinct.length}%-6d  Log Source  $filename")
		println(f"Valid Request   ${validRequests }%-6d  Unique Files     ${urls.distinct.length    }%-6d        Unique 404  ${req404s.distinct.length  }%-6d  Log Size    ${fileSize.toFloat/1000    }%-4.2f KiB")
		println(f"Failed Request  ${failedRequests}%-6d  Bandwidth        ${size.toFloat/1000       }%-4.2f KiB")

		println("\n** 1. Unique Visitor per Day **\n")
		
		displayVisitorLog(logs)

		println("\n** 2. Top 10 Requested Files (URLs) **\n")

		displayRequestLog(logs)
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
			("referrer", "\"(.*?)\""),
			("agent", "\"(.*?)\""),
		)
		// while (true)
		// {
			val logs = mutable.MutableList[Map[String, String]]()
			for (line <- Source.fromFile(filename).getLines) {
				val log = parseLog(line, combineRules)
				logs += log
				displayLog(filename, logs)
				Thread.sleep(1000)
			}
		// 	displayLog(logs)
		// 	Thread.sleep(1000)
		// }
   }
}