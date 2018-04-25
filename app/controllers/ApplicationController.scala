package controllers

import java.nio.file.{Files, Paths}

import akka.stream._
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import javax.inject.Inject
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.streams.Accumulator
import play.api.mvc._
import scalaz._

import scala.concurrent.ExecutionContext

class ApplicationController @Inject()(
    tcomp: TcompClient,
    configuration: Configuration,
    cc: ControllerComponents
)(implicit ec: ExecutionContext, mat: Materializer)
    extends AbstractController(cc) {

  val directory = Paths.get("storage")
  if (!Files.exists(directory)) Files.createDirectory(directory)

  def relay(filename: String) = Action(parse.empty).async { request =>
    tcomp.dataFor(filename, request.headers.get(HeaderNames.ACCEPT)) map {
      case -\/(error)  => error
      case \/-(source) => Ok.chunked(source)
    }
  }

  val sourceBodyParser: BodyParser[Source[ByteString, _]] = BodyParser { request =>
    Accumulator.source[ByteString].map(Right.apply)
  }

  def save(id: String) = Action(sourceBodyParser).async { request =>
    val source = request.body
    val sink   = FileIO.toPath(directory.resolve(id))
    source.runWith(sink).map(ioResult => Ok(ioResult.toString))
  }

  def load(id: String) = Action(parse.empty) {
    Ok.sendPath(directory.resolve(id))
  }

}
