package utils

import java.util.Locale

import io.codearte.jfairy.Fairy

import scala.util.Random

class PersonGenerator(seed: Int = 0) {

  private[this] val random = new Random(seed)
  private[this] val fairy  = Fairy.builder().withLocale(Locale.FRANCE).withRandomSeed(seed).build()

  def generate(): Person = {
    val person  = fairy.person()
    val address = person.getAddress
    Person(
      person.getFirstName,
      if (random.nextBoolean()) Some(person.getMiddleName) else None,
      person.getLastName,
      address.getAddressLine1,
      address.getPostalCode,
      address.getCity
    )
  }

}
