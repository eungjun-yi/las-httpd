package com.npcode

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.util.ByteString
import java.io.File
import java.io.PrintWriter

@RunWith(classOf[JUnitRunner])
class HttpdSuite extends FunSuite {

  test("Document root") {
    new Object with com.npcode.HTTP {
      assert(docroot == ".");
    }
  }

  test("Mimetype - HTML") {
    new Object with com.npcode.HTTP {
      val tempFile = File.createTempFile("scala-httpd", ".html")
      val writer = new java.io.PrintWriter(tempFile)
      writer.print("<html></html>");
      writer.close();
      assert(mimeType(tempFile) == "text/html")
    }
  }

  test("Mimetype - txt") {
    new Object with com.npcode.HTTP {
      val tempFile = File.createTempFile("scala-httpd", ".txt")
      val writer = new java.io.PrintWriter(tempFile)
      writer.print("hello");
      writer.close();
      assert(mimeType(tempFile) == "text/plain")
    }
  }

  test("Request line") {
    new Object with com.npcode.HTTP {
      assert(requestLine(ByteString("GET /index.html HTTP/1.1\r\n\r\n")) == "GET /index.html HTTP/1.1")
    }
  }

  test("400 Bad Request") {
    new Object with com.npcode.HTTP {
      assert(badRequest == ByteString("HTTP/1.1 400 Bad Request\r\n\r\n"))
    }
  }

  test("405 Method Not Allowed") {
    new Object with com.npcode.HTTP {
      assert(methodNotAllowed == ByteString("HTTP/1.1 405 Method Not Allowed\r\n\r\n"))
    }
  }

  test("404 Not Found") {
    new Object with com.npcode.HTTP {
      assert(notFound == ByteString("HTTP/1.1 404 Not Found\r\n\r\n"))
    }
  }

  test("request - 200 OK") {
    val tempFile = File.createTempFile("scala-httpd", ".txt")
    val writer = new java.io.PrintWriter(tempFile)
    writer.print("hello");
    writer.close();

    new Object with com.npcode.HTTP {
      override val docroot = tempFile.getParent
      val res = response(ByteString("GET /" + tempFile.getName() + " HTTP/1.1\r\n\r\n")) 
      val expected = ByteString("HTTP/1.1 200 OK\r\nContent-Length: 5\r\nContent-Type: text/plain\r\n\r\nhello")
      assert(res == expected)
    }
  }

  val tempFile = File.createTempFile("scala-httpd", ".txt")
  val writer = new java.io.PrintWriter(tempFile)
  writer.print("hello");
  writer.close();

  test("request - 400 Bad Request") {
    new Object with com.npcode.HTTP {
      override val docroot = tempFile.getParent
      val res = response(ByteString("blahblah"))
      val expected = ByteString("HTTP/1.1 400 Bad Request\r\n\r\n")
      assert(res == expected)
    }
  }

  test("request - 404 Not Found") {
    new Object with com.npcode.HTTP {
      override val docroot = tempFile.getParent
      val res = response(ByteString("GET /index.html HTP/1.1\r\n\r\n"))
      val expected = ByteString("HTTP/1.1 404 Not Found\r\n\r\n")
      assert(res == expected)
    }
  }

  test("request - 405 Method Not Allowed") {
    new Object with com.npcode.HTTP {
      override val docroot = tempFile.getParent
      val res = response(ByteString("FAKE /" + tempFile.getName() + " HTP/1.1\r\n\r\n"))
      val expected = ByteString("HTTP/1.1 405 Method Not Allowed\r\n\r\n")
      assert(res == expected)
    }
  }

}

// vim: set ts=2 sw=2 et:
