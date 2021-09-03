package uk.gov.nationalarchives.prototype.cookiesigning

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.time.temporal.ChronoUnit
import java.util.{Base64, Date}

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.typesafe.config.{Config, ConfigFactory}

class CookieSigningLambda extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    println("input:")
    println(new String(input.readAllBytes, StandardCharsets.UTF_8))

    val config: Config = ConfigFactory.load

    val encodedCert = config.getString("privateKeyBase64Encoded")
    val decodedCert = Base64.getDecoder().decode(encodedCert)
    val keySpec = new PKCS8EncodedKeySpec(decodedCert)
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKey = keyFactory.generatePrivate(keySpec)

    val s3ObjectKey = s"*"

    val protocol = Protocol.https
    val distributionDomain = "test-signed-cookies.tdr-sandbox.nationalarchives.gov.uk"
    val keyPairId = "KEIAZDQSPWWSQ"
    val activeFrom = Date.from(new Date().toInstant.minus(3, ChronoUnit.HOURS))
    val expiresOn = Date.from(new Date().toInstant.plus(3, ChronoUnit.HOURS))
    val ipRange = "0.0.0.0/0"

    val cookies = CloudFrontCookieSigner.getCookiesForCustomPolicy(
      protocol,
      distributionDomain,
      privateKey,
      s3ObjectKey,
      keyPairId,
      expiresOn,
      activeFrom,
      ipRange
    )

    val response =
      s"""{
        |    "isBase64Encoded": false,
        |    "statusCode": 200,
        |    "headers": {
        |       "Set-Cookie": "${cookies.getPolicy.getKey}=${cookies.getPolicy.getValue}; Path=/; Secure; HttpOnly",
        |       "Set-Cookie": "${cookies.getKeyPairId.getKey}=${cookies.getKeyPairId.getValue}; Path=/; Secure; HttpOnly",
        |       "Set-Cookie": "${cookies.getSignature.getKey}=${cookies.getSignature.getValue}; Path=/; Secure; HttpOnly"
        |    },
        |    "body": "Hello world"
        |}
        |""".stripMargin
    output.write(response.getBytes)
  }
}
