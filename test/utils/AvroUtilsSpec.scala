package utils

import java.io.{File, FileInputStream, FileOutputStream}

import org.apache.avro.Schema
import org.specs2.mutable.Specification

class AvroUtilsSpec extends Specification {

  "AvroUtils" should {
    "convert from binary to file container format" in skipped {
      val schema = new Schema.Parser().parse(new File("avro-schema-from-tcomp.avsc"))
      val is     = new FileInputStream("avro-binary-from-tcomp.avro")
      val os     = new FileOutputStream("result.avro")
      AvroUtils.convertFromBinaryToFileContainer(schema, is, os)
      os.close()
      is.close()
      success
    }

    "convert from binary to file container format" in skipped {
      val schema = new Schema.Parser().parse(new File("avro-schema-from-tcomp.avsc"))
      val is     = new FileInputStream("avro-binary-from-tcomp.avro")
      val os     = new FileOutputStream("result.avro")
      AvroUtils.convertFromBinaryToJsonFormat(schema, is, os)
      os.close()
      is.close()
      success
    }
  }

}
