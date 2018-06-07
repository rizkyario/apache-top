package com.rizkyario.apache_top

import java.io.File
import java.text.SimpleDateFormat

class ApacheTopPrinter(filename: String)
{

	def printSummaryLog(logs: List[Map[String, String]]) =
	{
		val fileSize = new File(this.filename).length
		val size = logs.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
		val failedRequests = logs.filter((log)=>(log("status").toInt >= 400))
		val validRequests = logs.filter((log)=>(log("status").toInt < 400))
		val visitors = (for (log <- logs) yield (log("ip")))
		val referrers = (for (log <- logs) yield (log("referrer")))
		val requests = (for (log <- logs) yield (log("request").split(" ")(1)))
		val req404s = for (log <- logs; if log("status").toInt == 404 ) yield {log("status") -> log("request")}

		println("\n** APACHE TOP Overall Analysed Requests **\n")
		println(f"Total Request   ${logs.length        }%-6d  Unique Visitors  ${visitors.distinct.length}%-6d        Referrers   ${referrers.distinct.length}%-6d  Log Source  $filename")
		println(f"Valid Request   ${validRequests.size }%-6d  Unique Files     ${requests.distinct.length}%-6d        Unique 404  ${req404s.distinct.length  }%-6d  Log Size    ${ApacheTopPrinter.toByteText(fileSize)}")
		println(f"Failed Request  ${failedRequests.size}%-6d  Bandwidth        ${ApacheTopPrinter.toByteText(size)}")
	}

	def printVisitorLog(logs: List[Map[String, String]], limit: Int) =
	{
		val gLogs = (
			for ((key, gLogs) <- logs.groupBy(_.get("date")))
			yield
			{(
				key.get,
				(for (log <- gLogs) yield (log("ip"))).distinct.length,
				gLogs
			)}).toSeq.sortWith(_._1 > _._1)
		val total = gLogs.foldLeft(0){(total, logs)=>{total + (for (log <- logs._3) yield (log("ip"))).distinct.length}}

		println(f"\n** 1. Unique Visitor per Day (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length}) **\n")
		for ((log, i) <- gLogs.zipWithIndex)
		{
			val size = log._3.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
			if (i < limit)
				println(f"${log._2}%-4d  ${ApacheTopPrinter.toByteText(size)}  ${log._1}  ${ApacheTopPrinter.printProcentBar(log._2, total, 30)}")
		}
	}

	def printRequestLog(logs: List[Map[String, String]], limit: Int) =
	{
		val gLogs = (
			for((key, gLogs) <- logs.groupBy(_.get("request")))
			yield
			(
				key.get,
				gLogs.length,
				gLogs
			)).toSeq.sortWith(_._2 > _._2)

		println(f"\n** 2. Top 10 Requested Files (URLs) (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length}) **\n")
		for ((log, i) <- gLogs.zipWithIndex)
		{
			val size = log._3.foldLeft(0){(total, log)=>{total + log("bytes").toInt}}
			if (i < limit)
				println(f"${log._2}%-4d  ${ApacheTopPrinter.toByteText(size)}  ${log._1.split(" ")(0)}%-6s ${log._1.split(" ")(2)}%-10s  ${log._1.split(" ")(1)}")
		}
	}
}

object ApacheTopPrinter
{
	def toByteText(bytes: Long): String =
	{
		if (bytes > 1000000000)
			f"${bytes.toFloat/1000000000}%-6.2f GiB"
		else if (bytes > 1000000)
			f"${bytes.toFloat/1000000   }%-6.2f MiB"
		else
			f"${bytes.toFloat/1000      }%-6.2f KiB"
	}

	def printProcentBar(value: Int, total: Int, length: Int): String =
	{
		"|" * (value.toFloat / total.toFloat * length).toInt
	}

	def clearScreen = print("\u001b[H\u001b[J")
}