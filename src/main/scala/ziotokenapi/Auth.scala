package ziotokenapi

import upickle.default.{macroR, Reader}

case class Auth(access_token: String, instance_url: String)

object Auth {
  implicit val reader: Reader[Auth] = macroR
}
