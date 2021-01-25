package com.study.spring.webserver.model.commons;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.*;

public class AttachedFile {

  private final String originalFileName;

  private final String contentType;

  private final byte[] bytes;

  public AttachedFile(String originalFileName, String contentType, byte[] bytes) {
    this.originalFileName = originalFileName;
    this.contentType = contentType;
    this.bytes = bytes;
  }

  private static boolean verify(MultipartFile multipartFile) {
    if (multipartFile != null && multipartFile.getSize() > 0 && multipartFile.getOriginalFilename() != null) {
      String contentType = multipartFile.getContentType();
      // 첨부파일 타입(contentType)을 확인하고 이미지인 경우 처리. 그런데 이건 최소한의 확인일뿐 보안상 안전한 방법은 아니다.
      // 좀더 보안에 안전하게 만들려면,  첫 N바이트 이용한다. Magic Byte라 하는 데 이걸 보고  JPEG인지 PNG 인지 알 수 있다. 그런데 이것도 조작 가능
      // ByteArray를 분석해주는 해킹 툴을 사용할 수 밖에 없음. 하지만 매번 ByteArray를 모두 스캔하면서 확인 하는건 성능에 악영향 => 성능과 보안 간의 트레이드 오프
      if (isNotEmpty(contentType) && contentType.toLowerCase().startsWith("image"))
        return true;
    }
    return false;
  }

  public static AttachedFile toAttachedFile(MultipartFile multipartFile) {
    try {
      return verify(multipartFile)
        ? new AttachedFile(multipartFile.getOriginalFilename(), multipartFile.getContentType(), multipartFile.getBytes())
        : null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String extension(String defaultExtension) {
    return defaultIfEmpty(getExtension(originalFileName), defaultExtension);
  }

  /**
   * @Params basePath: 이미지가 업로드되는 상위 디렉토리를 지정하는 역할
   **/
  public String randomName(String defaultExtension) {
    return randomName(null, defaultExtension);
  }

  public String randomName(String basePath, String defaultExtension) {
    String name = isEmpty(basePath) ? UUID.randomUUID().toString() : basePath + "/" + UUID.randomUUID().toString();
    return name + "." + extension(defaultExtension);
  }

  public InputStream inputStream() {
    return new ByteArrayInputStream(bytes);
  }

  public long length() {
    return bytes.length;
  }

  public String getContentType() {
    return contentType;
  }

}
