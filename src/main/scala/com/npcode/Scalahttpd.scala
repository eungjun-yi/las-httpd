package com.npcode

import akka.actor._
import java.net.InetSocketAddress
import akka.util.ByteString
import java.io.File
import org.apache.tika.Tika
import java.io.FileInputStream
import scala.util.matching.Regex
import akka.actor.IO.ReadHandle

trait HTTP {
  val docroot = "."

  def mimeType(file: File) = new Tika().detect(file)

  def requestLine(bytes: ByteString): String = bytes.takeWhile(_ != '\r').utf8String

  def notFound: ByteString = ByteString("HTTP/1.1 404 Not Found\r\n\r\n")

  def methodNotAllowed: ByteString = ByteString("HTTP/1.1 405 Method Not Allowed\r\nAllow: GET\r\n\r\n")

  def badRequest: ByteString = ByteString("HTTP/1.1 400 Bad Request\r\n\r\n")

  def ok(file: File): ByteString = ByteString(
    "HTTP/1.1 200 OK\r\n"
    + "Content-Length: " + file.length() + "\r\n"
    + "Content-Type: " + mimeType(file) + "\r\n\r\n") ++ readFile(file)

  def serve(rHandle: ReadHandle, request: ByteString) = {
    rHandle.asSocket.write(response(request))
    rHandle.close()
  }

  def readFile(file: File) = {
    val resource = new Array[Byte](file.length.toInt)
    val in = new FileInputStream(file)
    in.read(resource)
    in.close()
    ByteString(resource)
  }

  def response(request: ByteString): ByteString = {
    new Regex("\\s+").split(requestLine(request)) match {
      case Array("GET", path, _) =>
        new File(docroot, path) match {
          case file if file.isFile() => ok(file)
          case _ => notFound
        }
      case Array(_, _, _) => methodNotAllowed
      case _ => badRequest
    }
  }
}

class TCPServer(port: Int) extends Actor with HTTP {

  override def preStart {
    IOManager(context.system).listen(new InetSocketAddress(port))
  }

  def receive = {
    case IO.NewClient(server) => server.accept()
    case IO.Read(rHandle, bytes) => serve(rHandle, bytes)
  }
}

object Application {
  def main(args: Array[String]) {
    val port = 8000
    ActorSystem().actorOf(Props(new TCPServer(port)))
  }
}
