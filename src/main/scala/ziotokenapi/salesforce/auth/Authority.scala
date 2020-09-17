package ziotokenapi.salesforce.auth

import upickle.default.{macroR, Reader}

case class Authority(access_token: String, instance_url: String)

object Authority {
  implicit val reader: Reader[Authority] = macroR
}
