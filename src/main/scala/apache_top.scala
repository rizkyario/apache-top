package com.rizkyario.apache_top

import scala.io.Source
import java.io.File

object apache_top
{	
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
		var fileSize: Long = 0
		while (true)
		{
			val nfileSize = new File(filename).length
			if (nfileSize != fileSize)
			{
				fileSize = nfileSize
				ApacheTopPrinter clearScreen
				val logs = (for (line <- Source.fromFile(filename).getLines if line.length != 0)
							yield(parser.parseLog(line))).toList.filter(log => !log.isEmpty)
				val printer = new ApacheTopPrinter(filename, logs)
				printer.printSummaryLog()
				printer.printVisitorLog(20)
				printer.printRequestLog(20)
				printer.print404RequestLog(20)
			}
			Thread.sleep(delay)
		}
	}
}