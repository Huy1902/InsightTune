package com.pm.playingservice.service;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsUrlServiceTest {

  @Test
  void givenValidInputs_whenGetUrl_thenReturnsSignedUrlAndCleansTempFile() throws Exception {
    // given
    AwsUrlService service = new AwsUrlService();
    String distribution = "d111111abcdef8.cloudfront.net";
    String keyPairId = "K123ABCEXAMPLE";
    long ttlSeconds = 90L;
    Resource privateKeyResource = mock(Resource.class);

    ReflectionTestUtils.setField(service, "distributionDomainName", distribution);
    ReflectionTestUtils.setField(service, "cloudFrontKeyPairId", keyPairId);
    ReflectionTestUtils.setField(service, "urlTtlSeconds", ttlSeconds);
    ReflectionTestUtils.setField(service, "privateKeyResource", privateKeyResource);

    byte[] derBytes = new byte[] { 0x30, (byte)0x82, 0x01, 0x0a }; // arbitrary bytes; not parsed by test
    InputStream is = new ByteArrayInputStream(derBytes);
    when(privateKeyResource.getInputStream()).thenReturn(is);

    String objectKey = "tracks/12345.mp3";
    String expectedSigned = "https://" + distribution + "/" + objectKey + "?Policy=...&Signature=...&Key-Pair-Id=" + keyPairId;

    AtomicReference<File> capturedTempKeyFile = new AtomicReference<>();
    AtomicReference<Date> capturedExpiry = new AtomicReference<>();

    // when
    Instant before = Instant.now();
    try (MockedStatic<CloudFrontUrlSigner> mocked = mockStatic(CloudFrontUrlSigner.class)) {
      mocked.when(() -> CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
              eq(SignerUtils.Protocol.https),
              eq(distribution),
              argThat(f -> { capturedTempKeyFile.set(f); return f != null && f.exists(); }),
              eq(objectKey),
              eq(keyPairId),
              argThat(d -> { capturedExpiry.set(d); return d != null; })
      )).thenReturn(expectedSigned);

      String actual = service.getUrl(objectKey);

      // then
      assertThat(actual).isEqualTo(expectedSigned);

      // verify expiry is roughly now + ttlSeconds (±2s window)
      Instant after = Instant.now();
      assertThat(capturedExpiry.get()).isNotNull();
      Instant exp = capturedExpiry.get().toInstant();
      Instant lower = before.plusSeconds(ttlSeconds - 2);
      Instant upper = after.plusSeconds(ttlSeconds + 2);
      assertThat(exp).isBetween(lower, upper);

      // verify protocol/domain/objectKey/keyPair already matched by eq(...)
      // verify temp file got deleted after signing
      File tmpFile = capturedTempKeyFile.get();
      assertThat(tmpFile).isNotNull();
      // The service deletes the file in finally; at this point call has returned.
      assertThat(tmpFile.exists()).isFalse();

      // one static call as expected
      mocked.verify(() -> CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
              SignerUtils.Protocol.https, distribution, tmpFile, objectKey, keyPairId, capturedExpiry.get()
      ));
      mocked.verifyNoMoreInteractions();
    }
  }

  @Test
  void givenPrivateKeyIoError_whenGetUrl_thenThrowsAndCleansTempFileIfCreated() throws Exception {
    // given
    AwsUrlService service = new AwsUrlService();
    ReflectionTestUtils.setField(service, "distributionDomainName", "dzzz.cloudfront.net");
    ReflectionTestUtils.setField(service, "cloudFrontKeyPairId", "KPAIR");
    ReflectionTestUtils.setField(service, "urlTtlSeconds", 60L);

    Resource privateKeyResource = mock(Resource.class);
    ReflectionTestUtils.setField(service, "privateKeyResource", privateKeyResource);

    // Simulate I/O failure when opening the key stream
    when(privateKeyResource.getInputStream()).thenThrow(new RuntimeException("error"));

    // We cannot capture a temp file because failure happens before writing;
    // just ensure exception surfaces and no signer call happens.
    try (MockedStatic<CloudFrontUrlSigner> mocked = mockStatic(CloudFrontUrlSigner.class)) {
      assertThatThrownBy(() -> service.getUrl("foo/bar"))
              .isInstanceOf(Exception.class)
              .hasMessageContaining("error");

      mocked.verifyNoInteractions();
    }
  }
}
