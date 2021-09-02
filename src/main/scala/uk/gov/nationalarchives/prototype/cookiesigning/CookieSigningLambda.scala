package uk.gov.nationalarchives.prototype.cookiesigning

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class CookieSigningLambda extends RequestHandler[Map[String, String], String] {
  override def handleRequest(input: Map[String, String], context: Context): String = {
    "hello world"
  }
}
