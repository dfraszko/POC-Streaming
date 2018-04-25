package utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

class PersonGeneratorSpec(implicit ee: ExecutionEnv) extends Specification with AfterAll {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  override def afterAll(): Unit = {
    system.terminate()
  }

  "PersonGenerator" should {
    "generate data" in {
      val generator = new PersonGenerator(0)
      val result = Source
        .fromIterator(() => Iterator.continually(generator.generate()))
        .take(10)
        .runWith(Sink.seq)

      result.map(_.size) must be_===(10).await
      result.map(_.apply(0)) must be_===(Person("Perceval", Some("Jérémie"), "Le guen", "Grande Rue, 66", "22355", "Gap")).await
    }
  }

}
