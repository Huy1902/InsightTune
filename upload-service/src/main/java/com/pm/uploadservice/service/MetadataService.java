package com.pm.uploadservice.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.pm.uploadservice.dto.MetaRequestDto;
import com.pm.uploadservice.dto.MetaResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Service for extracting metadata from MP3 files using the {@code mp3agic} library.
 *
 * <p>This service supports:</p>
 * <ul>
 *   <li>Reading ID3v2 tags (title, album, artist list)</li>
 *   <li>Extracting track duration in milliseconds</li>
 *   <li>Parsing and normalizing embedded album art (JPEG/PNG only)</li>
 *   <li>Fallbacks if tags are missing (e.g., using filename for title)</li>
 * </ul>
 *
 * <p>Temporary files are created for parsing, and cleaned up automatically.</p>
 *
 * <p>Intended usage: invoked by controllers when a user uploads an MP3 file,
 * producing a structured {@link MetaResponseDto} that can be persisted or passed to other services.</p>
 *
 * @author Huy1902
 */
@Service
@Slf4j
public class MetadataService {

  /**
   * Builds structured metadata from an uploaded MP3 file.
   *
   * <p>The method will:</p>
   * <ol>
   *   <li>Save the uploaded file temporarily to disk (required by {@code mp3agic}).</li>
   *   <li>Read ID3v2 tags if available (title, album, artist(s), album art).</li>
   *   <li>Fallback to filename for title if missing.</li>
   *   <li>Normalize/validate image MIME type (only JPEG/PNG allowed).</li>
   *   <li>Return metadata as a {@link MetaResponseDto} object.</li>
   * </ol>
   *
   * @param metaRequestDto request wrapper containing the uploaded {@link MultipartFile}.
   * @return a response DTO containing parsed metadata (title, album, artists, duration, image bytes).
   * @throws IOException             if file transfer or deletion fails.
   * @throws InvalidDataException    if the MP3 file is invalid.
   * @throws UnsupportedTagException if tags are unsupported.
   */
  public MetaResponseDto buildMetadata(MetaRequestDto metaRequestDto)
          throws IOException, InvalidDataException, UnsupportedTagException {

    MultipartFile file = metaRequestDto.getFile();
    if (file == null || file.isEmpty()) {
      log.error("No file provided");
    }

    Path tmp = Files.createTempFile("upload-", ".mp3");

    try (InputStream in = Objects.requireNonNull(file).getInputStream()) {
      // Save MultipartFile to temp file (mp3agic requires a real file handle)
      file.transferTo(tmp);

      Mp3File mp3 = new Mp3File(tmp.toFile());
      Integer durationMs = (int) (mp3.getLengthInSeconds() * 1000);

      String title = null;
      String album = null;
      List<String> artists = List.of();
      byte[] image = null;
      String imageType = null;

      if (mp3.hasId3v2Tag()) {
        ID3v2 tag = mp3.getId3v2Tag();

        title = blankToNull(tag.getTitle());
        album = blankToNull(tag.getAlbum());
        artists = splitArtists(blankToNull(tag.getArtist()));

        image = tag.getAlbumImage();
        imageType = sanitizeMime(blankToNull(tag.getAlbumImageMimeType()));

        log.info("title={}, album={}, artists={}, imageType={}", title, album, artists, imageType);

        if (image == null || image.length == 0) {
          log.error("No embedded album art in ID3v2 tag");
        }
        if (imageType == null) {
          imageType = "image/jpeg"; // default if not set
        }
        validateImageType(imageType);
      } else {
        log.error("No ID3v2 tag found (album art requires ID3v2)");
      }

      // Fallback: derive title from filename if missing
      if (!StringUtils.hasText(title)) {
        title = baseName(file.getOriginalFilename());
      }

      if(!StringUtils.hasText(album)) {
        album = "";
      }

      return MetaResponseDto.builder()
              .title(title.trim())
              .album(album.trim())
              .artists(artists)
              .durationMs(durationMs)
              .image(image)
              .imageType(imageType)
              .build();
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  // === Helper methods ===

  /**
   * Converts blank or empty strings to {@code null}.
   */
  private static String blankToNull(String s) {
    return StringUtils.hasText(s) ? s : null;
  }

  /**
   * Extracts the base name from a file path (without directories or extension).
   *
   * @param filename the original file name or path.
   * @return the base name, or "unknown" if input is empty.
   */
  private static String baseName(String filename) {
    if (!StringUtils.hasText(filename)) return "unknown";
    int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
    String name = (slash >= 0) ? filename.substring(slash + 1) : filename;
    int dot = name.lastIndexOf('.');
    return (dot > 0) ? name.substring(0, dot) : name;
  }

  /**
   * Splits a raw artist string into a list of individual artists.
   *
   * <p>Handles common separators: comma, "&amp;", "feat.", "ft.", "x".</p>
   *
   * @param artistRaw the raw artist string (e.g., "Artist feat. Guest").
   * @return list of cleaned artist names, never {@code null}.
   */

  private static List<String> splitArtists(String artistRaw) {
    if (!StringUtils.hasText(artistRaw)) return List.of();

    // Delimiters:
    //  - comma
    //  - & (ampersand)
    //  - feat. / ft. (case-insensitive, as whole words)
    //  - 'x' ONLY when surrounded by spaces (A x B), not in "DJ X," or "X." etc.
    String[] parts = artistRaw.split(
            "(?i)\\s*(?:,|&|\\bfeat\\.?\\b|\\bft\\.?\\b|\\s+x\\s+)\\s*"
    );

    return Arrays.stream(parts)
            .map(s -> s == null ? "" : s.trim())
            // strip leading/trailing punctuation and extra spaces
            .map(s -> s.replaceAll("^[\\p{Punct}\\s]+", "")
                    .replaceAll("[\\p{Punct}\\s]+$", ""))
            .filter(StringUtils::hasText)
            .toList();
  }


  /**
   * Normalizes common MIME type variants (e.g., {@code image/jpg → image/jpeg}).
   *
   * @param mime raw MIME type string.
   * @return cleaned MIME type, or {@code null} if input was null.
   */
  private static String sanitizeMime(String mime) {
    if (mime == null) return null;
    String m = mime.trim().toLowerCase();
    if (m.equals("image/jpg")) {
      return "image/jpeg";
    }
    return m;
  }

  /**
   * Validates that the image type is supported (JPEG or PNG).
   *
   * @param mime the MIME type to validate.
   */
  private static void validateImageType(String mime) {
    if (!("image/jpeg".equalsIgnoreCase(mime) || "image/png".equalsIgnoreCase(mime))) {
      log.error("Unsupported image type: {}", mime);
    }
  }
}
