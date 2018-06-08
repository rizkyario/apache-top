package com.rizkyario.apache_top

import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.GivenWhenThen

class ApacheTopParserSpec extends FunSpec with BeforeAndAfter with GivenWhenThen
{
	val parser = new ApacheTopParser

	describe ("ApacheTopParser ParseLog: ...")
	{
		it ("should parse all value of valid log")
		{
			val log = this.parser.parseLog(ApacheTopSampleLog.data(0))
			assert(log("ip") == "124.30.9.161")
			assert(log("client") == "-")
			assert(log("user") == "-")
			assert(log("timestamp") == "[21/Jul/2009:02:48:11 -0700]")
			assert(log("request") == "GET /java/edu/pj/pj010004/pj010004.shtml HTTP/1.1")
			assert(log("status") == "200")
			assert(log("bytes") == "16731")
			assert(log("referrer") == "http://www.google.co.in/search?hl=en&client=firefox-a&rlz=1R1GGGL_en___IN337&hs=F0W&q=reading+data+from+file+in+java&btnG=Search&meta=&aq=0&oq=reading+data+")
			assert(log("agent") == "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 GTB5")
		}
		it ("should parse all value of log without referrer")
		{
			val log = this.parser.parseLog(ApacheTopSampleLog.data(1))
			assert(log("ip") == "89.166.165.223")
			assert(log("client") == "-")
			assert(log("user") == "-")
			assert(log("timestamp") == "[21/Jul/2009:02:48:12 -0700]")
			assert(log("request") == "GET /favicon.ico HTTP/1.1")
			assert(log("status") == "404")
			assert(log("bytes") == "970")
			assert(log("referrer") == "-")
			assert(log("agent") == "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11")
		}
		it ("should parse all value of log with no returned data [ bytes == - ]")
		{
			val log = this.parser.parseLog(ApacheTopSampleLog.data(2))
			assert(log("ip") == "66.249.70.10")
			assert(log("client") == "-")
			assert(log("user") == "-")
			assert(log("timestamp") == "[23/Feb/2014:03:21:59 -0700]")
			assert(log("request") == "GET /blog/post/java/how-load-multiple-spring-context-files-standalone/ HTTP/1.0")
			assert(log("status") == "301")
			assert(log("bytes") == "-")
			assert(log("referrer") == "-")
			assert(log("agent") == "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
		}
		it ("should parse all value of log containing IPv6 ")
		{
			val log = this.parser.parseLog(ApacheTopSampleLog.data(3))
			assert(log("ip") == "2001:0db8:85a3:0000:0000:8a2e:0370:7334")
			assert(log("client") == "-")
			assert(log("user") == "-")
			assert(log("timestamp") == "[23/Feb/2014:03:21:59 -0700]")
			assert(log("request") == "GET /blog/post/java/how-load-multiple-spring-context-files-standalone/ HTTP/1.0")
			assert(log("status") == "301")
			assert(log("bytes") == "-")
			assert(log("referrer") == "-")
			assert(log("agent") == "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
		}
		it ("should not parse incomplete log")
		{
			val log = this.parser.parseLog(ApacheTopSampleLog.data(4))
			assert(log.isEmpty)
		}
		it ("should not parse log file without timestamp")
		{
			val log = this.parser.parseLog(ApacheTopSampleLog.data(5))
			assert(log.isEmpty)
		}
		it ("should not parse invalid format log")
		{
			val log = this.parser.parseLog("Error")
			assert(log.isEmpty)
		}
		it ("should not parse empty log")
		{
			val log = this.parser.parseLog("")
			assert(log.isEmpty)
		}
	}

	describe ("ApacheTopParser ParseDate")
	{
		it ("should return 'yyyy/MM/dd' when parameter is valid")
		{
			val date = ApacheTopParser.parseDate("[21/Jul/2009:02:48:12 -0700]")
			assert(date == "2009/07/21")
		}
		it ("should return empty string when parameter is invalid")
		{
			val date = ApacheTopParser.parseDate("21/Jul/2009:02:48:12 -0700")
			assert(date.isEmpty)
		}
	}

	describe ("ApacheTopParser ParseRequestField")
	{
		it ("should return (requestType, uri, httpVersion) when parameter is valid")
		{
			val (requestType, uri, httpVersion) = ApacheTopParser.parseRequestField("GET /the-uri-here HTTP/1.1")
			assert(requestType == "GET")
			assert(uri == "/the-uri-here")
			assert(httpVersion == "HTTP/1.1")
		}
		it ("should return empty strings when parameter is empty")
		{
			val (requestType, uri, httpVersion) = ApacheTopParser.parseRequestField("")
			assert(requestType.isEmpty)
			assert(uri.isEmpty)
			assert(httpVersion.isEmpty)
		}
		it ("should return empty strings when parameter is incompleted")
		{
			val (requestType, uri, httpVersion) = ApacheTopParser.parseRequestField("GET /the-uri-here")
			assert(requestType.isEmpty)
			assert(uri.isEmpty)
			assert(httpVersion.isEmpty)
		}
		it ("should return empty strings when parameter is overloaded")
		{
			val (requestType, uri, httpVersion) = ApacheTopParser.parseRequestField("GET /the-uri-here HTTP/1.1 Extra")
			assert(requestType.isEmpty)
			assert(uri.isEmpty)
			assert(httpVersion.isEmpty)
		}
	}
}