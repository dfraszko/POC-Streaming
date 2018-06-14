package utils

import java.io._

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.apache.avro.Schema
import org.apache.avro.file.{CodecFactory, DataFileWriter}
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}

object AvroUtils {

  def convertFromBinaryToFileContainer(schema: Schema, is: InputStream, os: OutputStream, codec: String = "null"): Unit = {
    val writer = new DataFileWriter[GenericRecord](new GenericDatumWriter[GenericRecord])
    writer.setCodec(CodecFactory.fromString(codec))
    writer.create(schema, os)

    forEachRecordFrom(schema, is)(record => writer.append(record))

    writer.flush()
  }

  def convertFromBinaryToJsonFormat(schema: Schema, is: InputStream, os: OutputStream): Unit = {
    val writer  = new GenericDatumWriter[GenericRecord](schema)
    val encoder = EncoderFactory.get.jsonEncoder(schema, os)

    forEachRecordFrom(schema, is)(record => writer.write(record, encoder))

    encoder.flush()
  }

  private[this] def forEachRecordFrom(schema: Schema, is: InputStream)(consumer: GenericRecord => Unit): Unit = {
    val reader  = new GenericDatumReader[GenericRecord](schema)
    val decoder = DecoderFactory.get.binaryDecoder(is, null)

    try {
      var record: GenericRecord = null
      while (true) {
        record = reader.read(record, decoder)
        consumer(record)
      }
    } catch {
      case exception: EOFException => ()
    }
  }

  val toJSON: Flow[GenericRecord, ByteString, NotUsed] =
    Flow.fromFunction[GenericRecord, ByteString] { record =>
      val schema  = record.getSchema
      val writer  = new GenericDatumWriter[GenericRecord](schema)
      val baos    = new ByteArrayOutputStream()
      val encoder = EncoderFactory.get.jsonEncoder(schema, baos, true)
      writer.write(record, encoder)
      encoder.flush()
      ByteString(baos.toByteArray)
    }

  val toBinary: Flow[GenericRecord, ByteString, NotUsed] =
    Flow.fromFunction[GenericRecord, ByteString] { record =>
      val schema  = record.getSchema
      val writer  = new GenericDatumWriter[GenericRecord](schema)
      val baos    = new ByteArrayOutputStream()
      val encoder = EncoderFactory.get.binaryEncoder(baos, null)
      writer.write(record, encoder)
      encoder.flush()
      ByteString(baos.toByteArray)
    }

}
