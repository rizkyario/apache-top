package com.rizkyario.apache_top

import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.GivenWhenThen

class ApacheTopParserSpec extends FunSpec with BeforeAndAfter with GivenWhenThen
{
	val parser = new ApacheTopParser

	describe("Testing access log 0...")
	{
		
		val log = this.parser.parseLog(ApacheTopSampleLog.data(0))
		it("the result should not be None")
		{
			assert(log != None)
		}
		it("the individual fields should be right")
		{
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
	}

	describe("Testing access log 1...")
	{
		val log = this.parser.parseLog(ApacheTopSampleLog.data(1))
		it("the result should not be None")
		{
			assert(log != None)
		}
		it("the individual fields should be right")
		{
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
	}

	describe("Testing bad access log ...")
	{
		val log = this.parser.parseLog(ApacheTopSampleLog.badData(0))
		it("the result should not be None")
		{
			assert(log != None)
		}
		it("the individual fields should be right")
		{
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
	}
}