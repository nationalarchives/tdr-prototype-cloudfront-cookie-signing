package uk.gov.nationalarchives.prototype.cookiesigning

import java.io.{InputStream, OutputStream}
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.time.temporal.ChronoUnit
import java.util.{Base64, Date}

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.auth0.jwt.JWT
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.parser.parse

import scala.io.Source

class CookieSigningLambda extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val rawInput = Source.fromInputStream(input).mkString

    println("input:")
    println(rawInput)

    val json = parse(rawInput).getOrElse(throw new RuntimeException(s"Could not parse $rawInput as JSON"))

    // Pull the auth header out of the input JSON. The real implementation should be less fragile!
    val authHeader = json.hcursor.downField("headers").get[String]("Authorization").toOption.get
    val rawToken = authHeader.stripPrefix("Bearer ")

    // In the real implementation, validate the token like the export authoriser does
    val decoded = JWT.decode(rawToken)
    val userId = decoded.getSubject

    println(s"User ID: $userId")

    val config: Config = ConfigFactory.load

    val encodedCert = config.getString("privateKeyBase64Encoded")
    val decodedCert = Base64.getDecoder.decode(encodedCert)
    val keySpec = new PKCS8EncodedKeySpec(decodedCert)
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKey = keyFactory.generatePrivate(keySpec)

    // Authorisation rule: only allow users to upload files to a folder corresponding to their user ID
    val s3ObjectKey = s"$userId/*"

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

    // CORS header is just for localhost to get spike working. Real header would have to be environment-specific. On
    // integration, it should return the intg domain or localhost, depending on the Host header in the request.
    val response =
      s"""{
        |    "isBase64Encoded": false,
        |    "statusCode": 200,
        |    "headers": {
        |       "Access-Control-Allow-Origin": "http://localhost:9000",
        |       "Access-Control-Allow-Credentials": "true"
        |    },
        |    "multiValueHeaders": {
        |       "Set-Cookie": [
        |         "${cookies.getPolicy.getKey}=${cookies.getPolicy.getValue}; Path=/; Secure; HttpOnly; SameSite=None",
        |         "${cookies.getKeyPairId.getKey}=${cookies.getKeyPairId.getValue}; Path=/; Secure; HttpOnly; SameSite=None",
        |         "${cookies.getSignature.getKey}=${cookies.getSignature.getValue}; Path=/; Secure; HttpOnly; SameSite=None"
        |       ]
        |    },
        |    "body": "{}"
        |}
        |""".stripMargin
    output.write(response.getBytes)
  }
}
