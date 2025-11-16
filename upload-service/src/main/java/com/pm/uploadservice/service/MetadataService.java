package com.pm.uploadservice.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.pm.uploadservice.dto.MetaRequestDto;
import com.pm.uploadservice.dto.MetaResponseDto;
import com.pm.uploadservice.exception.MetaExtractException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Service for extracting metadata from MP3 files using the {@code mp3agic} library.
 *
 * <p>This service supports:</p>
 * <ul>
 *   <li>Reading ID3v2 tags (title, album, list of artists)</li>
 *   <li>Obtaining song duration in milliseconds</li>
 *   <li>Analyzing and validating embedded cover images (JPEG/PNG only)</li>
 *   <li>Fallback when tags are missing (e.g., use file name as title)</li>
 * </ul>
 *
 * <p>Temporary files are created for analysis and automatically deleted afterward.</p>
 *
 * <p>Usage: called from a controller when a user uploads an MP3 file,
 * returning a {@link MetaResponseDto} object that can be stored in the database
 * or passed to other services.</p>
 *
 * @author Huy1902
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataService {

  private final Validator validator;

  /**
   * Builds structured metadata from an uploaded MP3 file.
   *
   * <p>The method will:</p>
   * <ol>
   *   <li>Save the file temporarily on disk (required by {@code mp3agic}).</li>
   *   <li>Read the ID3v2 tag if present (title, album, artists, cover image).</li>
   *   <li>Fallback to using the file name as title if missing.</li>
   *   <li>Normalize/check image MIME type (JPEG/PNG only).</li>
   *   <li>Return metadata as a {@link MetaResponseDto} object.</li>
   * </ol>
   *
   * @param metaRequestDto request object containing a {@link MultipartFile}.
   * @return response DTO containing analyzed metadata (title, album, artists, duration, cover image).
   * @throws MetaExtractException if any error occurs during extraction.
   */
  public MetaResponseDto buildMetadata(MetaRequestDto metaRequestDto)
          throws MetaExtractException {

    MultipartFile file = metaRequestDto.getFile();
    if (file == null || file.isEmpty()) {
      log.error("No file provided");
      throw new MetaExtractException("No file provided");
    }
    try {

      Path tmp = Files.createTempFile("upload-", ".mp3");

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

      if (!StringUtils.hasText(title)) {
        title = baseName(file.getOriginalFilename());
      }

      if (!StringUtils.hasText(album)) {
        album = "";
      }
      MetaResponseDto metaResponseDto = MetaResponseDto.builder()
              .title(title.trim())
              .album(album.trim())
              .artists(artists)
              .durationMs(durationMs)
              .image(image)
              .imageType(imageType)
              .build();

      Files.deleteIfExists(tmp);

      Set<ConstraintViolation<MetaResponseDto>> violations = validator.validate(metaResponseDto);

      if (!violations.isEmpty()) {
        String message = violations.iterator().next().getMessage();
        log.error(message);
        throw new MetaExtractException(message);
      } else {
        return metaResponseDto;
      }


    } catch (InvalidDataException | UnsupportedTagException | IOException e) {
      throw new MetaExtractException(e.getMessage());
    }
  }

  /**
   * Converts empty strings or strings containing only whitespace to {@code null}.
   */
  private static String blankToNull(String s) {
    return StringUtils.hasText(s) ? s : null;
  }

  /**
   * Returns the base name of a file from a path (removes directories and extension).
   *
   * @param filename the original file name or path
   * @return the base file name, or "unknown" if input is empty.
   */
  private static String baseName(String filename) {
    if (!StringUtils.hasText(filename)) return "unknown";
    int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
    String name = (slash >= 0) ? filename.substring(slash + 1) : filename;
    int dot = name.lastIndexOf('.');
    return (dot > 0) ? name.substring(0, dot) : name;
  }

  /**
   * Splits a raw artist string into a list of individual artist names.
   *
   * <p>Handles common delimiters: comma, "&", "feat.", "ft.", "x".</p>
   *
   * @param artistRaw raw artist string (e.g., "Artist feat. Guest")
   * @return list of processed artist names, never {@code null}.
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
   * Normalizes common MIME variants (e.g., {@code image/jpg → image/jpeg}).
   *
   * @param mime raw MIME string
   * @return normalized MIME string, or {@code null} if input is null
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
   * @param mime the MIME type to check
   */
  private static void validateImageType(String mime) {
    if (!("image/jpeg".equalsIgnoreCase(mime) || "image/png".equalsIgnoreCase(mime))) {
      log.error("Unsupported image type: {}", mime);
    }
  }
}
