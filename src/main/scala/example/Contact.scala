package example

import upickle.default.{Reader, macroR}

case class Contact(
    Id: String,
    Email: Option[String],
    Salutation: Option[String],
    FirstName: Option[String],
    LastName: Option[String]
)

object Contact {
  implicit val reader: Reader[Contact] = macroR
}
