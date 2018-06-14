package examples

import java.nio.file.Paths

import com.sksamuel.avro4s.AvroOutputStream
import org.apache.avro.file.CodecFactory
import utils.{Person, PersonGenerator}

object Generator extends App {

  val factor = 1

  val path = Paths.get(s"example-${factor}k.avro")

  val os = AvroOutputStream.data[Person](path, CodecFactory.fromString("deflate"))
  try {
    val generator = new PersonGenerator(0)
    Iterator
      .continually(generator.generate())
      .take(1000 * factor)
      .foreach(os.write)
  } finally {
    os.close()
  }

}
