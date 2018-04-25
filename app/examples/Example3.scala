package examples

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, ClosedShape}
import com.sksamuel.avro4s.RecordFormat
import org.apache.avro.generic.GenericRecord
import utils.{AvroUtils, Person, PersonGenerator}

import scala.concurrent.Future

object Example3 extends App {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  val directory = Paths.get("storage")
  if (!Files.exists(directory)) Files.createDirectory(directory)

  val sink1 = FileIO.toPath(directory.resolve("persons.json.avro"))
  val sink2 = FileIO.toPath(directory.resolve("persons.binary.avro"))
  val sink3 = Sink.foreach(println).withAttributes(ActorAttributes.dispatcher("akka.stream.default-blocking-io-dispatcher"))

  val graph = RunnableGraph.fromGraph(GraphDSL.create(sink1, sink2, sink3)((_, _, _)) { implicit builder => (sink1, sink2, sink3) =>
    import GraphDSL.Implicits._

    val source          = Source.repeat(new PersonGenerator(0)).map(_.generate()).take(10)
    val toGenericRecord = Flow.fromFunction[Person, GenericRecord](RecordFormat[Person].to)
    val bcast           = builder.add(Broadcast[GenericRecord](3))

    source ~> toGenericRecord ~> bcast ~> AvroUtils.toJSON ~> sink1
    bcast ~> AvroUtils.toBinary ~> sink2
    bcast ~> sink3

    ClosedShape
  })

  val result = graph.run(mat)

  Future.sequence(List(result._1, result._2, result._3)).onComplete { futures =>
    futures.map(println)
    system.terminate()
  }

}
