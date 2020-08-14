package example

import upickle.default.{Reader, macroR}

case class Auth(accessToken: String, instanceUrl: String)

object Auth {
  implicit val reader: Reader[Auth] = macroR
}
