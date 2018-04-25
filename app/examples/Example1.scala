package examples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import utils.{Person, PersonGenerator}

import scala.concurrent.Future

object Example1 extends App {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  val source: Source[Person, NotUsed] = Source.repeat(new PersonGenerator(0)).map(_.generate())
  val sink: Sink[Any, Future[Done]]   = Sink.foreach(println) // warning: blocking call!

  val result: Future[Done] = source.take(10).runWith(sink)

  result.onComplete(_ => system.terminate())

}
