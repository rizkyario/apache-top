package com.rizkyario.apache_top

import java.io.File
import java.text.SimpleDateFormat

class ApacheTopPrinter(filename: String, var logs: List[Map[String, String]])
{
	type LogType = Map[String, String]

	def printFileSize 		(filename: String = filename): String = ApacheTopPrinter.toByteText(new File(this.filename).length)
	def printReqSize		(logs: List[LogType] = logs): String = ApacheTopPrinter.toByteText(logs.foldLeft(0){(total, log)=>{total + log("bytes").toInt}})
	def printTotalRequests	(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(logs.length)
	def printFailedRequests (logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(logs.filter((log)=>(log("status").toInt >= 400)).size)
	def printValidRequests	(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(logs.filter((log)=>(log("status").toInt < 400)).size)
	def printVisitors		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric((for (log <- logs) yield (log("ip"))).distinct.length)
	def printReferrers		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric((for (log <- logs) yield (log("referrer"))).distinct.length)
	def printRequests		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric((for (log <- logs) yield (log("request").split(" ")(1))).distinct.length)
	def printReq404s		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric((for (log <- logs; if log("status").toInt == 404 ) yield {log("status") -> log("request")}).distinct.length)

	def printSummaryLog() =
	{
		println("\n** APACHE TOP Overall Analysed Requests **\n")
		println(f"Total Request   ${printTotalRequests() }  Unique Visitors  ${printVisitors()}        Referrers   ${printReferrers()}  Log Source  $filename")
		println(f"Valid Request   ${printValidRequests() }  Unique Files     ${printRequests()}        Unique 404  ${printReq404s()  }  Log Size    ${printFileSize()}")
		println(f"Failed Request  ${printFailedRequests()}  Bandwidth        ${printReqSize() }")
	}

	def printVisitorLog(limit: Int) =
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
			if (i < limit)
				println(f"${log._2}%-4d  ${printReqSize(log._3)}  ${log._1}  ${ApacheTopPrinter.printProcentBar(log._2, total, 30)}")
		}
	}

	def printRequestLog(limit: Int) =
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
			val (requestType, uri, httpVersion) = ApacheTopPrinter.parseRequestField(log._1)
			if (i < limit)
				println(f"${log._2}%-4d  ${printReqSize(log._3)}  ${requestType}%-6s ${httpVersion}%-10s  ${uri}")
		}
	}
}

object ApacheTopPrinter
{
	def toByteText(bytes: Long): String =
	{
		if (bytes >= 1000000000)
			f"${bytes.toFloat/1000000000}%-6.2f GiB"
		else if (bytes >= 1000000)
			f"${bytes.toFloat/1000000   }%-6.2f MiB"
		else
			f"${bytes.toFloat/1000      }%-6.2f KiB"
	}

	def toMetric(value: Long): String =
	{
		if (value >= 1000000000)
			f"${value.toFloat/1000000000}%-6.2fG"
		else if (value >= 1000000)
			f"${value.toFloat/1000000   }%-6.2fM"
		else if (value >= 1000)
			f"${value.toFloat/1000      }%-6.2fK"
		else
			f"${value.toFloat/1         }%-6.0f"
	}

	def printProcentBar(value: Int, total: Int, length: Int): String =
	{
		"|" * (value.toFloat / total.toFloat * length).toInt
	}

	def parseRequestField(request: String): Tuple3[String, String, String] =
	{
        val arr = request.split(" ")
        if (arr.size == 3) ((arr(0), arr(1), arr(2))) else ("", "", "")
    }

	def clearScreen = print("\u001b[H\u001b[J")
}