package uk.gov.nationalarchives.prototype.cookiesigning

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}

class CookieSigningLambda extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    println("input:")
    println(new String(input.readAllBytes, StandardCharsets.UTF_8))

    val response =
      """{
        |    "isBase64Encoded": false,
        |    "statusCode": 200,
        |    "headers": { "headerName": "headerValue" },
        |    "body": "Hello world"
        |}
        |""".stripMargin
    output.write(response.getBytes)
  }
}
