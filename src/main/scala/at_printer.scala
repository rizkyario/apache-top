package com.rizkyario.apache_top

import java.text.SimpleDateFormat
import scala.collection.mutable.LinkedHashMap

class ApacheTopParser()
{
	/*
	** Set default rules to `combined` LogFormat
	*/
	var rules: LinkedHashMap[String, String] = LinkedHashMap(
		("ip", "(\\S+)"),
		("client", "(\\S+)"),
		("user", "(\\S+)"),
		("timestamp", "(\\[.+?\\])"),
		("request", "\"(.*?)\""),
		("status", "(\\d{3})"),
		("bytes", "(\\S+)"),
		("referrer", "\"(.*?)\""),
		("agent", "\"(.*?)\"")
	)

	/*
	** ParseLog based on $rules
	** Return all value in Map[String, String] if match with $rules
	** Else return empty Map
	*/
	def parseLog(line: String): Map[String, String] =
	{
		val regexRule = this.rules.values.reduce((a, b) => a + " " + b)
		val re = s"$regexRule".r
		val log =
			(for
			{
				m <- re.findAllIn(line).matchData
				(e, i) <- m.subgroups.zipWithIndex
			}
			yield (rules.keys.toSeq(i) -> e)).toMap

		if (log.exists(_._1 == "timestamp"))
			log + ("date" -> ApacheTopParser.parseDate(log("timestamp")))
		else
			log
	}
}

object ApacheTopParser
{
	/*
	** Parse date string from logs to sortable date format
	** Return 'yyyy/MM/dd' when parameter is valid
	** Else return empty string
	*/
	def parseDate(timestamp: String): String =
	{
		try
		{
			val date = new SimpleDateFormat("[dd/MMM/yyyy:hh:mm:ss Z]").parse(timestamp)
			new SimpleDateFormat("yyyy/MM/dd").format(date)
		}
		catch
		{
			case e: Exception => ""
		}
	}

	/*
	** Parse request string from logs to Tuple of requestType, uri, httpVersion
	** Return (requestType, uri, httpVersion) when parameter is valid
	** Else return empty string
	*/
	def parseRequestField(request: String): Tuple3[String, String, String] =
	{
        val arr = request.split(" ")
        if (arr.size == 3) ((arr(0), arr(1), arr(2))) else ("", "", "")
    }
}