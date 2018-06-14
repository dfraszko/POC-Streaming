package controllers

import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{Keep, Sink, Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.Await
import scala.concurrent.duration._

object Investigation extends App {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  val sink = Sink
    .foreach[ByteString](x => println(s"${Thread.currentThread.getName} $x"))
    .withAttributes(ActorAttributes.dispatcher("akka.stream.default-blocking-io-dispatcher"))

  val (os, publisher) = StreamConverters
    .asOutputStream()
    .toMat(Sink.asPublisher(false))(Keep.both)
    .run()

  println("start")
  (1 to 20).foreach(_ => os.write("hello".getBytes))
  os.flush()
  os.close() // required to complete the stream (and the future)
  println("stop")

  val future = Source.fromPublisher(publisher).runWith(sink)

  future.onComplete { x =>
    println(x)
    system.terminate()
  }

}
