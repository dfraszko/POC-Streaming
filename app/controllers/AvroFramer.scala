package controllers

import java.io.{EOFException, InputStream}

import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream._
import akka.util.ByteString
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.DecoderFactory
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object AvroFramer {

  def frame(schema: Schema, chunks: Source[ByteString, _])(implicit mat: Materializer): Source[GenericRecord, _] = {
    val is = chunks.runWith(StreamConverters.asInputStream())
    Source
      .fromGraph(new AvroFramer(schema, is))
      .withAttributes(ActorAttributes.dispatcher("akka.stream.blocking-io-dispatcher"))
  }

}

class AvroFramer(schema: Schema, is: InputStream) extends GraphStage[SourceShape[GenericRecord]] {

  val LOGGER = LoggerFactory.getLogger("poc.AvroFramer")

  val out: Outlet[GenericRecord] = Outlet("output")

  override val shape: SourceShape[GenericRecord] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    val datumReader = new GenericDatumReader[GenericRecord](schema)
    val decoder     = DecoderFactory.get.binaryDecoder(is, null)
    var counter     = 0 // It is safe to use var inside the GraphStageLogic

    setHandler(
      out,
      new OutHandler {
        override def onPull(): Unit = {
          Try(datumReader.read(null, decoder)) match {
            case Success(record)           => counter += 1; push(out, record)
            case Failure(ex: EOFException) => LOGGER.debug(s"End of stream; ${counter} records have been processed"); completeStage()
            case Failure(ex: Throwable)    => failStage(ex)
          }
        }
      }
    )

    override def postStop(): Unit = {
      LOGGER.debug("Close the underlying input stream")
      is.close()
    }
  }

}
