package com.rizkyario.apache_top

import scala.util.matching.Regex
import scala.io.Source
import scala.collection.mutable
import java.io.File
import util.control.Breaks._

object apache_top {
	
   	def main(args: Array[String])
	{
		if (args.length == 0)
		{
			println("Usage ./apache_top [log_file]")
			sys.exit(0)
		}
	   	val filename = args(0)
		val delay = 1000
		val parser = new ApacheTopParser
		val printer = new ApacheTopPrinter(filename)
		var fileSize: Long = 0
		while (true)
		{
			val nfileSize = new File(filename).length
			if (nfileSize != fileSize)
			{
				fileSize = nfileSize
				ApacheTopPrinter clearScreen
				val logs = (for (line <- Source.fromFile(filename).getLines if line.length != 0)
				yield
				{
					val log = parser.parseLog(line)
					log
				}).toList
				printer.printSummaryLog(logs)
				printer.printVisitorLog(logs, 20)
				printer.printRequestLog(logs, 20)
			}
			Thread.sleep(delay)
		}
   }
}