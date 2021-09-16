# TDR Prototype cookie signing Lambda

This is a prototype repo for the [Transfer Digital Records] project.

The code is designed to run in an AWS Lambda, responding to a request sent through API Gateway.

It takes a request which contains a token issued by the [TDR auth server], and returns a response with `Set-Cookie`
headers that set [CloudFront signed cookies].

This prototype was part of a spike in the Sandbox environment to see if we could upload files to CloudFront rather than
directly to S3, because S3 uploads were being blocked by some users' networks.

[Transfer Digital Records]: https://github.com/nationalarchives/tdr-dev-documentation 
[TDR auth server]: https://github.com/nationalarchives/tdr-auth-server
[CloudFront signed cookies]: https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-signed-cookies.html