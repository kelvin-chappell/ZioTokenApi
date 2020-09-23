package ziotokenapi.salesforce.authority

import upickle.default.{macroR, Reader}

case class Access(access_token: String, instance_url: String)

object Access {
  implicit val reader: Reader[Access] = macroR
}
