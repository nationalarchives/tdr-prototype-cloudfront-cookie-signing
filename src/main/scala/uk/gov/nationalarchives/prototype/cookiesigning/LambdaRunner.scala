package uk.gov.nationalarchives.prototype.cookiesigning

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets

object LambdaRunner extends App {
  val lambda = new CookieSigningLambda

  val input = "{}"
  val inputStream = new ByteArrayInputStream(input.getBytes)

  val outputStream = new ByteArrayOutputStream
  // Context is not used, so safe to set to null
  val context = null

  lambda.handleRequest(inputStream, outputStream, context)

  val output = new String(outputStream.toByteArray, StandardCharsets.UTF_8)

  println("Output:")
  println(output)
}
