package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import javax.inject.Inject
import org.apache.avro.Schema
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.mvc.{Result, Results}
import scalaz.Scalaz._
import scalaz._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object TcompClient {

  def properties(filename: String): JsValue = Json.obj(
    "properties" -> Json.obj(
      "format"          -> "AVRO",
      "path"            -> s"/data/$filename",
      "@definitionName" -> "SimpleFileIoDataset"
    ),
    "dependencies" -> Json.arr(
      Json.obj(
        "@definitionName" -> "SimpleFileIoDatastore"
      ))
  )

}

class TcompClient @Inject()(ws: WSClient, configuration: Configuration)(implicit ec: ExecutionContext) {

  import Status._
  import Results._
  import TcompClient._

  val tcompBase    = configuration.get[String]("poc.tcomp.url")
  val tcompTimeout = configuration.get[Duration]("poc.tcomp.timeout")

  def schemaFor(filename: String): Future[Result \/ Schema] =
    ws.url(s"$tcompBase/runtimes/schema")
      .withRequestFilter(AhcCurlRequestLogger())
      .addHttpHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .withRequestTimeout(tcompTimeout)
      .post(properties(filename)) map { response =>
      if (isSuccessful(response.status)) new Schema.Parser().parse(response.body).right
      else ServiceUnavailable(s"cannot fetch schema for $filename from tcomp").left
    }

  def dataFor(filename: String, accept: Option[String]): Future[Result \/ Source[ByteString, _]] =
    ws.url(s"$tcompBase/runtimes/data")
      .withRequestFilter(AhcCurlRequestLogger())
      //.addQueryStringParameters("from" -> "0", "limit" -> "10")
      .addHttpHeaders(HeaderNames.ACCEPT -> accept.getOrElse("application/avro-json"))
      .withRequestTimeout(tcompTimeout)
      .withMethod("POST")
      .withBody(properties(filename))
      .stream() map { response =>
      if (isSuccessful(response.status)) response.bodyAsSource.right
      else ServiceUnavailable(s"cannot fetch data for $filename from tcomp").left
    }

}
