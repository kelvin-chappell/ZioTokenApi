package ziotokenapi.salesforce

import upickle.default.{macroR, Reader}

case class Contact(
    Id: String,
    Email: String,
    Salutation: String,
    FirstName: String,
    LastName: String
)

object Contact {
  implicit val reader: Reader[Contact] = macroR
}
