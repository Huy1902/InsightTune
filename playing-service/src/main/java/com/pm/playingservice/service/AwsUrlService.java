package com.pm.playingservice.service;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

@Component
public class AwsUrlService {

  @Value("${spotube.aws.cloudfront.distributionDomainName}")
  private String distributionDomainName; // e.g. dxxxx.cloudfront.net

  @Value("${spotube.aws.cloudfront.keypairId}")
  private String cloudFrontKeyPairId;    // Public key ID from CloudFront (Key Group)

  @Value("${spotube.aws.cloudfront.urlTtlSeconds:120}")
  private long urlTtlSeconds;            // default 120s

  @Value("${spotube.aws.pvtkeyPath}")
  private Resource privateKeyResource;   // DER (PKCS#8) private key

  public String getUrl(String objectKey) throws Exception {

    // Load the DER private key to a temp file (the signer API needs a File)
    File tempKeyFile = File.createTempFile("cf-key-", ".der");
    try (InputStream in = privateKeyResource.getInputStream();
         FileOutputStream out = new FileOutputStream(tempKeyFile)) {
      in.transferTo(out);
    }

    try {
      Date expires = Date.from(Instant.now().plusSeconds(urlTtlSeconds));

      return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
              SignerUtils.Protocol.https,
              distributionDomainName,
              tempKeyFile,
              objectKey,
              cloudFrontKeyPairId,
              expires
      );
    } finally {
      // best-effort cleanup
      //noinspection ResultOfMethodCallIgnored
      tempKeyFile.delete();
    }
  }
}

