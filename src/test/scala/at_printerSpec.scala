package com.rizkyario.apache_top

import org.scalatest.FunSpec

class ApacheTopPrinterSpec extends FunSpec
{
	describe ("ApacheTopPrinter Print_")
	{
		it ("should print correct summary of logs")
		{
			val printer = new ApacheTopPrinter(ApacheTopSampleLog.filename, ApacheTopSampleLog.logs())
			assert(printer.printFilename() 		== ApacheTopSampleLog.filenameColor)
			assert(printer.printFileSize() 		== "8.23   KiB")
			assert(printer.printReqSize() 		== "113.86 KiB")
			assert(printer.printTotalRequests() == "37      ")
			assert(printer.printFailedRequests()== "6       ")
			assert(printer.printValidRequests() == "31      ")
			assert(printer.printVisitors() 		== "6       ")
			assert(printer.printReferrers() 	== "5       ")
			assert(printer.printRequests() 		== "13      ")
			assert(printer.printReq404s() 		== "2       ")
			assert(printer.getVisitorLogs().length 	== 6)
			assert(printer.getRequestLogs().length 	== 13)
			assert(printer.getRequestLogs(ApacheTopSampleLog.logs().filter((log)=>(log("status").toInt == 404))).length == 2)
		}
		it ("should print correct summary of one log")
		{
			val logs = ApacheTopSampleLog.logs().take(1)
			val printer = new ApacheTopPrinter(ApacheTopSampleLog.filename, logs)
			assert(printer.printFilename() 		== ApacheTopSampleLog.filenameColor)
			assert(printer.printFileSize() 		== "8.23   KiB")
			assert(printer.printReqSize() 		== "1.61   KiB")
			assert(printer.printTotalRequests() == "1       ")
			assert(printer.printFailedRequests()== "0       ")
			assert(printer.printValidRequests() == "1       ")
			assert(printer.printVisitors() 		== "1       ")
			assert(printer.printReferrers() 	== "1       ")
			assert(printer.printRequests() 		== "1       ")
			assert(printer.printReq404s() 		== "0       ")
			assert(printer.getVisitorLogs().length 	== 1)
			assert(printer.getRequestLogs().length 	== 1)
			assert(printer.getRequestLogs(logs.filter((log)=>(log("status").toInt == 404))).length == 0)
		}
		it ("should print correct summary of two logs")
		{
			val logs = ApacheTopSampleLog.logs().take(2)
			val printer = new ApacheTopPrinter(ApacheTopSampleLog.filename, logs)
			assert(printer.printFilename() 		== ApacheTopSampleLog.filenameColor)
			assert(printer.printFileSize() 		== "8.23   KiB")
			assert(printer.printReqSize() 		== "2.58   KiB")
			assert(printer.printTotalRequests() == "2       ")
			assert(printer.printFailedRequests()== "1       ")
			assert(printer.printValidRequests() == "1       ")
			assert(printer.printVisitors() 		== "2       ")
			assert(printer.printReferrers() 	== "2       ")
			assert(printer.printRequests() 		== "2       ")
			assert(printer.printReq404s() 		== "1       ")
			assert(printer.getVisitorLogs().length 	== 1)
			assert(printer.getRequestLogs().length 	== 2)
			assert(printer.getRequestLogs(logs.filter((log)=>(log("status").toInt == 404))).length == 1)
		}
		it ("should print correct summary of zero logs")
		{
			val logs = ApacheTopSampleLog.logs().take(0)
			val printer = new ApacheTopPrinter(ApacheTopSampleLog.filename, logs)
			assert(printer.printFilename() 		== ApacheTopSampleLog.filenameColor)
			assert(printer.printFileSize() 		== "8.23   KiB")
			assert(printer.printReqSize() 		== "0        B")
			assert(printer.printTotalRequests() == "0       ")
			assert(printer.printFailedRequests()== "0       ")
			assert(printer.printValidRequests() == "0       ")
			assert(printer.printVisitors() 		== "0       ")
			assert(printer.printReferrers() 	== "0       ")
			assert(printer.printRequests() 		== "0       ")
			assert(printer.printReq404s() 		== "0       ")
			assert(printer.getVisitorLogs().length 	== 0)
			assert(printer.getRequestLogs().length 	== 0)
			assert(printer.getRequestLogs(logs.filter((log)=>(log("status").toInt == 404))).length == 0)
		}
		it ("should handle invalid file")
		{
			val printer = new ApacheTopPrinter("build.sbt", ApacheTopSampleLog.logs("build.sbt").take(0))
			printer.printSummaryLog()
			printer.printVisitorLog(20)
			printer.printRequestLog(20)
			printer.print404RequestLog(20)
			assert(printer.printFileSize() 		== "171      B")
		}
		it ("should handle non-existent file")
		{
			val printer = new ApacheTopPrinter("Error", ApacheTopSampleLog.logs("Error").take(0))
			assert(printer.printFilename() 		== "")
			assert(printer.printFileSize() 		== "0        B")
		}
	}
	describe ("ApacheTopPrinter ToByteText")
	{
		it ("should print correct value")
		{
			assert(ApacheTopPrinter.toByteText(1340000000) 	== "1.34   GiB")
			assert(ApacheTopPrinter.toByteText(1340000) 	== "1.34   MiB")
			assert(ApacheTopPrinter.toByteText(1340) 		== "1.34   KiB")
			assert(ApacheTopPrinter.toByteText(2)			== "2        B")
			assert(ApacheTopPrinter.toByteText(1)			== "1        B")
			assert(ApacheTopPrinter.toByteText(0)			== "0        B")
		}
	}
	describe ("ApacheTopPrinter ToMetric")
	{
		it ("should print correct value")
		{
			assert(ApacheTopPrinter.toMetric(1340000000) 	== "1.34G   ")
			assert(ApacheTopPrinter.toMetric(1340000) 		== "1.34M   ")
			assert(ApacheTopPrinter.toMetric(1340) 			== "1.34K   ")
			assert(ApacheTopPrinter.toMetric(2)				== "2       ")
			assert(ApacheTopPrinter.toMetric(1)				== "1       ")
			assert(ApacheTopPrinter.toMetric(0)				== "0       ")
		}
	}
}