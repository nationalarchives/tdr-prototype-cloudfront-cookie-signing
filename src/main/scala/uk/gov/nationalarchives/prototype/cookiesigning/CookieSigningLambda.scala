package uk.gov.nationalarchives.prototype.cookiesigning

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class CookieSigningLambda extends RequestHandler[String, String] {
  override def handleRequest(input: String, context: Context): String = {
    println("input:")
    println(input)

    "hello world"
  }
}
