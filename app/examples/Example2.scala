package examples

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object Example2 extends App {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  val source: Source[Int, NotUsed]  = Source.fromIterator(() => Iterator.from(1))
  val sink: Sink[Any, Future[Done]] = Sink.foreach(println) // warning: blocking call

  val result: Future[Done] = source
    .throttle(1, 1 second, 1, ThrottleMode.shaping)
    .take(10)
    .scan(1)(_ * _)
    .zipWithIndex
    .map { case (f, i) => s"$i! = $f" }
    .runWith(sink)

  result.onComplete(_ => system.terminate())

}
