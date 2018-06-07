package com.rizkyario.apache_top

import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable.LinkedHashMap
import scala.collection._
import java.io.File
import java.text.SimpleDateFormat
import util.control.Breaks._

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

	def displayProcentBar(value: Int, total: Int, length: Int): String =
	{
		"|" * (value.toFloat / total.toFloat * length).toInt
	}

	def displayVisitorLog(logs: mutable.MutableList[Map[String, String]], limit: Int) =
	{
		val gLogs = (for ((key, gLogs) <- logs.groupBy(_.get("date")))
			yield
			{
				(
					key.get, 
					(for (log <- gLogs) yield (log("ip"))).distinct.length,
					gLogs
				)
			}
		).toSeq.sortWith(_._1 > _._1)
		println(f"\n** 1. Unique Visitor per Day (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length}) **\n")

		val total = gLogs.foldLeft(0){(total, logs)=>{total + (for (log <- logs._3) yield (log("ip"))).distinct.length}}
		for ((log, i) <- gLogs.zipWithIndex)
		{
			val size = log._3.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
			if (i < limit)
				println(f"${log._2}%-4d  ${toByteText(size)}  ${log._1}  ${displayProcentBar(log._2, total, 30)}")
		}
	}

	def toByteText(bytes: Long): String =
	{
		if (bytes > 1000000000)
			f"${bytes.toFloat/1000000000}%-6.2f GiB"
		else if (bytes > 1000000)
			f"${bytes.toFloat/1000000   }%-6.2f MiB"
		else
			f"${bytes.toFloat/1000      }%-6.2f KiB"
	}

	def displayRequestLog(logs: mutable.MutableList[Map[String, String]], limit: Int) =
	{
		val gLogs = (for((key, gLogs) <- logs.groupBy(_.get("request"))) 
			yield 
			(
				key.get,
				gLogs.length,
				gLogs
			)
		).toSeq.sortWith(_._2 > _._2)
		println(f"\n** 2. Top 10 Requested Files (URLs) (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length}) **\n")
		for ((log, i) <- gLogs.zipWithIndex)
		{
			val size = log._3.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
			if (i < limit)
				println(f"${log._2}%-4d  ${toByteText(size)}  ${log._1.split(" ")(0)}%-6s ${log._1.split(" ")(2)}%-10s  ${log._1.split(" ")(1)}")
		}
	}

	def displaySummaryLog(filename: String, logs: mutable.MutableList[Map[String, String]]) =
	{
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
		println(f"Valid Request   ${validRequests }%-6d  Unique Files     ${urls.distinct.length    }%-6d        Unique 404  ${req404s.distinct.length  }%-6d  Log Size    ${toByteText(fileSize)}")
		println(f"Failed Request  ${failedRequests}%-6d  Bandwidth        ${toByteText(size)}")
	}

	def parseDate(timestamp: String): String = {
		val date = new SimpleDateFormat("[dd/MMM/yyyy:hh:mm:ss Z]").parse(timestamp)
		new SimpleDateFormat("yyyy/MM/dd").format(date) 
	}
	
   	def main(args: Array[String])
	{
		if (args.length == 0)
		{
			println("Usage ./apache_top [log_file]")
			sys.exit(0)
		}
	   	val filename = args(0)
		val delay = 1000
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

		var fileSize: Long = 0
		while (true)
		{
			val nfileSize = new File(filename).length
			if (nfileSize != fileSize)
			{
				fileSize = nfileSize
				print("\u001b[H\u001b[J")
				val logs = mutable.MutableList[Map[String, String]]()
				for (line <- Source.fromFile(filename).getLines if line.length != 0)
				{
					val log = parseLog(line, combineRules)
					logs += (log + ("date" -> parseDate(log("timestamp"))))
				}
				displaySummaryLog(filename, logs)
				displayVisitorLog(logs, 20)
				displayRequestLog(logs, 20)
			}
			Thread.sleep(delay)
		}
   }
}