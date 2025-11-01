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
 * Dịch vụ trích xuất metadata từ các file MP3 sử dụng thư viện {@code mp3agic}.
 *
 * <p>Dịch vụ này hỗ trợ:</p>
 * <ul>
 *   <li>Đọc thẻ ID3v2 (title, album, danh sách nghệ sĩ)</li>
 *   <li>Lấy thời lượng bài hát tính theo mili giây</li>
 *   <li>Phân tích và chuẩn hóa ảnh bìa nhúng (chỉ JPEG/PNG)</li>
 *   <li>Dự phòng nếu thẻ thiếu (ví dụ: sử dụng tên file làm tiêu đề)</li>
 * </ul>
 *
 * <p>Các file tạm thời sẽ được tạo để phân tích và tự động xóa sau đó.</p>
 *
 * <p>Cách sử dụng: được gọi từ controller khi người dùng tải lên một file MP3,
 * trả về một đối tượng {@link MetaResponseDto} có cấu trúc, có thể lưu vào cơ sở dữ liệu
 * hoặc chuyển cho các dịch vụ khác.</p>
 *
 * @author Huy1902
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataService {

  private final Validator validator;

  /**
   * Xây dựng metadata có cấu trúc từ một file MP3 được tải lên.
   *
   * <p>Phương thức sẽ:</p>
   * <ol>
   *   <li>Lưu file tạm thời lên đĩa (yêu cầu bởi {@code mp3agic}).</li>
   *   <li>Đọc thẻ ID3v2 nếu có (title, album, nghệ sĩ, ảnh bìa).</li>
   *   <li>Dự phòng dùng tên file làm title nếu thiếu.</li>
   *   <li>Chuẩn hóa / kiểm tra loại MIME của ảnh (chỉ JPEG/PNG).</li>
   *   <li>Trả về metadata dưới dạng {@link MetaResponseDto}.</li>
   * </ol>
   *
   * @param metaRequestDto đối tượng request chứa {@link MultipartFile}.
   * @return đối tượng response DTO chứa metadata đã phân tích (title, album, nghệ sĩ, thời lượng, ảnh bìa).
   * @throws MetaExtractException nếu có lỗi trong quá trình trích xuất.
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
   * Chuyển các chuỗi rỗng hoặc chỉ chứa khoảng trắng thành {@code null}.
   */
  private static String blankToNull(String s) {
    return StringUtils.hasText(s) ? s : null;
  }

  /**
   * Lấy tên file cơ bản từ đường dẫn (loại bỏ thư mục và phần mở rộng).
   *
   * @param filename tên file hoặc đường dẫn gốc.
   * @return tên file cơ bản, hoặc "unknown" nếu đầu vào rỗng.
   */
  private static String baseName(String filename) {
    if (!StringUtils.hasText(filename)) return "unknown";
    int slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
    String name = (slash >= 0) ? filename.substring(slash + 1) : filename;
    int dot = name.lastIndexOf('.');
    return (dot > 0) ? name.substring(0, dot) : name;
  }

  /**
   * Tách chuỗi nghệ sĩ thô thành danh sách các nghệ sĩ riêng lẻ.
   *
   * <p>Xử lý các dấu phân cách phổ biến: dấu phẩy, "&amp;", "feat.", "ft.", "x".</p>
   *
   * @param artistRaw chuỗi nghệ sĩ thô (ví dụ: "Artist feat. Guest").
   * @return danh sách tên nghệ sĩ đã xử lý, không bao giờ trả về {@code null}.
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
   * Chuẩn hóa các biến thể MIME phổ biến (ví dụ: {@code image/jpg → image/jpeg}).
   *
   * @param mime chuỗi MIME thô.
   * @return chuỗi MIME đã chuẩn hóa, hoặc {@code null} nếu đầu vào null.
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
   * Kiểm tra loại ảnh có được hỗ trợ (JPEG hoặc PNG).
   *
   * @param mime loại MIME cần kiểm tra.
   */
  private static void validateImageType(String mime) {
    if (!("image/jpeg".equalsIgnoreCase(mime) || "image/png".equalsIgnoreCase(mime))) {
      log.error("Unsupported image type: {}", mime);
    }
  }
}
