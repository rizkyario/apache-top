package com.rizkyario.apache_top

import java.io.File
import java.text.SimpleDateFormat

class ApacheTopPrinter(filename: String, var logs: List[Map[String, String]])
{
	type LogType = Map[String, String]

	def selectDistinct(key: String, logs: List[LogType] = logs): List[String] = (for (log <- logs) yield (log(key))).distinct

	/*
	** Return formatted values of $logs
	*/
	def printFilename 		(filename: String = filename): String = if (new File(this.filename).length > 0) f"${Console.YELLOW}$filename${Console.RESET}" else ""
	def printFileSize 		(filename: String = filename): String = ApacheTopPrinter.toByteText(new File(this.filename).length)
	def printReqSize		(logs: List[LogType] = logs): String = ApacheTopPrinter.toByteText(logs.foldLeft(0){(total, log)=>{total + log("bytes").toInt}})
	def printTotalRequests	(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(logs.length)
	def printFailedRequests (logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(logs.filter((log)=>(log("status").toInt >= 400)).size)
	def printValidRequests	(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(logs.filter((log)=>(log("status").toInt < 400)).size)
	def printVisitors		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(selectDistinct("ip").length)
	def printReferrers		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(selectDistinct("referrer").length)
	def printRequests		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric(selectDistinct("request").length)
	def printReq404s		(logs: List[LogType] = logs): String = ApacheTopPrinter.toMetric((for (log <- logs; if log("status").toInt == 404 ) yield {log("status") -> log("request")}).distinct.length)

	/*
	** Return List of unique visitor (based on IP Address) logs grouped by date
	*/
	def getVisitorLogs() =
		(for ((key, gLogs) <- logs.groupBy(_.get("date")))
		yield
		{(
			key.get,
			(for (log <- gLogs) yield (log("ip"))).distinct.length,
			gLogs
		)}).toSeq.sortWith(_._1 > _._1)

	/*
	** Return List of logs grouped by request string and sort based on sum item
	** Optional logs is provided to accomodate filtering, default value is $logs
	*/
	def getRequestLogs(logs: List[Map[String, String]] = logs) =
		(for((key, gLogs) <- logs.groupBy(_.get("request")))
		yield
		(
			key.get,
			gLogs.length,
			gLogs
		)).toSeq.sortWith(_._2 > _._2)
	
	/*
	** Print formatted values of $logs to standard output
	*/
	def printHeader (header: String) =
	{
		println(f"${Console.WHITE_B}${Console.BLACK}")
		println(f" ** ${header} ** ")
		println(f"${Console.RESET}")
	}

	def printSummaryLog() =
	{
		printHeader("APACHE TOP Overall Analysed Requests")
		println(f"Total Request   ${printTotalRequests() }  Unique Visitors  ${printVisitors()}        Referrers   ${printReferrers()}  Log Source  ${printFilename()}")
		println(f"Valid Request   ${printValidRequests() }  Unique Files     ${printRequests()}        Unique 404  ${printReq404s()  }  Log Size    ${printFileSize()}")
		println(f"Failed Request  ${printFailedRequests()}  Bandwidth        ${printReqSize() }")
	}

	def printVisitorLog(limit: Int) =
	{
		val gLogs = getVisitorLogs()
		val max = gLogs.foldLeft(0)
			{(total, logs) =>
				{
					val count = selectDistinct("ip", logs._3).length
					if (total < count)
						count
					else 
						total 
				}
			}

		printHeader(f"1. Unique Visitor per Day (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length})")
		for ((log, i) <- gLogs.zipWithIndex if i < limit)
			println(f"${log._2}%-4d  ${printReqSize(log._3)}  ${log._1}  ${ApacheTopPrinter.printProcentBar(log._2, max, 30)}")
	}

	def printRequestLog(limit: Int) =
	{
		val gLogs = getRequestLogs()

		printHeader(f"2. Requested Files (URLs) (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length})")
		for ((log, i) <- gLogs.zipWithIndex if i < limit)
		{
			val (requestType, uri, httpVersion) = ApacheTopParser.parseRequestField(log._1)
			println(f"${log._2}%-4d  ${printReqSize(log._3)}  ${requestType}%-6s ${httpVersion}%-10s  ${uri}")
		}
	}

	def print404RequestLog(limit: Int) =
	{
		val gLogs = getRequestLogs(logs.filter((log)=>(log("status").toInt == 404)))

		printHeader(f"3. 404 Requested Files (URLs) (${if (limit > gLogs.length) gLogs.length else limit}/${gLogs.length})")
		for ((log, i) <- gLogs.zipWithIndex if i < limit)
		{
			val (requestType, uri, httpVersion) = ApacheTopParser.parseRequestField(log._1)
			println(f"${log._2}%-4d  ${printReqSize(log._3)}  ${requestType}%-6s ${httpVersion}%-10s  ${uri}")
		}
	}
}

object ApacheTopPrinter
{
	/*
	** Return formatted value in B, KiB, MiB, and GiB
	*/
	def toByteText(bytes: Long): String =
	{
		if (bytes >= 1000000000)
			f"${bytes.toFloat/1000000000}%-6.2f GiB"
		else if (bytes >= 1000000)
			f"${bytes.toFloat/1000000   }%-6.2f MiB"
		else if (bytes > 1000)
			f"${bytes.toFloat/1000      }%-6.2f KiB"
		else
			f"${bytes			        }%-6d   B"
	}

	/*
	** Return formatted value in B, K, M, and G
	*/
	def toMetric(value: Long): String =
	{
		var result = ""
		if (value >= 1000000000)
			result = f"${value.toFloat/1000000000}%.2fG"
		else if (value >= 1000000)
			result = f"${value.toFloat/1000000   }%.2fM"
		else if (value >= 1000)
			result = f"${value.toFloat/1000      }%.2fK"
		else
			result = f"${value.toFloat/1         }%.0f"
		f"${result}%-8s"
	}

	/*
	** Return Bar Chart with the width of $value relative to $max with the size of $length
	*/
	def printProcentBar(value: Int, max: Int, length: Int): String =
	{
		"|" * (value.toFloat / max.toFloat * length).toInt
	}

	def clearScreen = print("\u001b[H\u001b[J")
}