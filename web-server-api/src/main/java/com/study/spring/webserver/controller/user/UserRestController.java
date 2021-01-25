package com.study.spring.webserver.controller.user;

import com.amazonaws.AmazonServiceException;
import com.study.spring.webserver.aws.S3Client;
import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.commons.AttachedFile;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.user.Email;
import com.study.spring.webserver.model.user.Role;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.security.Jwt;
import com.study.spring.webserver.security.JwtAuthentication;
import com.study.spring.webserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.study.spring.webserver.controller.ApiResult.OK;
import static com.study.spring.webserver.model.commons.AttachedFile.toAttachedFile;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
@Api(tags = "사용자 APIs")
public class UserRestController {

  private static final Logger log = LoggerFactory.getLogger(S3Client.class);

  private final Jwt jwt;

  private final S3Client s3Client;

  private final UserService userService;

  public UserRestController(Jwt jwt, S3Client s3Client, UserService userService) {
    this.jwt = jwt;
    this.s3Client = s3Client;
    this.userService = userService;
  }

  @PostMapping(path = "user/exists")
  @ApiOperation(value = "이메일 중복확인 (API 토큰 필요없음)")
  public ApiResult<Boolean> checkEmail(
    @RequestBody @ApiParam(value = "example: {\"address\": \"test00@gmail.com\"}") Map<String, String> request
  ) {
    Email email = new Email(request.get("address"));
    return OK(
      userService.findByEmail(email).isPresent()
    );
  }

  /**
   *  S3Client를 사용해 이미지를 S3에 업로드한다.
   */
  public Optional<String> uploadProfileImage(AttachedFile profileFile) {
    String profileImageUrl = null;
    if (profileFile != null) {
      String key = profileFile.randomName("profiles", "jpeg");
      try {
        // metadata 내용
        // 정해져 있는 건 없으며 서비스에 맞게 분석시 도움되는 데이터를 넣으면 된다.
        // ex. 프로필 사진이라면 유저 PK, 성별
        profileImageUrl = s3Client.upload(profileFile.inputStream(), profileFile.length(), key, profileFile.getContentType(), null);
      } catch (AmazonServiceException e) {
        log.warn("Amazon S3 error (key : {}) : {}", key, e.getMessage(), e);
      }
    }

    return ofNullable(profileImageUrl);
  }

  /**
   * 다른 API들은 'consumes='가 JSON 타입으로 생략되어 있음.
   * MediaType.MULTIPART_FORM_DATA_VALUE 타입은 받은 파일을 MultipartFile형태로 변환해서 준다.
   */
  @PostMapping(path = "user/join", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiOperation(value = "회원가입 (API 토큰 필요없음)")
  public ApiResult<JoinResult> join(
    @ModelAttribute JoinRequest joinRequest,
    @RequestPart(required = false) MultipartFile file
    /**
     * MultipartFile: bytearray로 표현되어 안의 내용을 알 수가 없기 때문에 Content Type은 이미지로 조작해놓고 내용은 악의적인 실행 파일일 수도 있다. 즉, bytearray만 가지고는 이미지 파일인지 알 수 없음
     * 특히 클라이언트에서 보낸 파일을 별다른 검증없이 업로드한다면 지금 이 코드는 보안적으로 위험한 코드
     */
  ) {
    User user = userService.join(
      joinRequest.getName(),
      new Email(joinRequest.getPrincipal()),
      joinRequest.getCredentials()
    );

    // CompletableFuture를 활용해 User 이미지 업로드
    supplyAsync(() ->
      uploadProfileImage(toAttachedFile(file))
    ).thenAccept(url ->
        url.ifPresent(profileImageUrl ->
          // 이미지가 정상적으로 업로드가 완료된 경우 (profileImageUrl != null)
          // profileImageUrl ex)"https://s3.ap-northeast-2.amazonaws.com/prgrms-web-bjs/profiles/41245071-d631-4517-9864-8b484b8f90f5.png"
          userService.updateProfileImage(Id.of(User.class, user.getSeq()), profileImageUrl)
        )
    );

    // supplyAsync 실행 완료 여부와 관계 없이 리턴한다.
    String apiToken = user.newApiToken(jwt, new String[]{Role.USER.value()});
    return OK(
      new JoinResult(apiToken, user)
    );
  }



  /**
   * @AuthenticationPrincipal
   *  - SecurityContextHolder.getContext().getAuthentication().getPrinciple() 과 동일
   *  - Object 타입 => id=Id[reference=User,value=4], name="yoonji", email=Email[address=yjna.dev@gmail.com]
   */
  @GetMapping(path = "user/me")
  @ApiOperation(value = "내 정보")
  public ApiResult<UserDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findById(authentication.id)
        .map(UserDto::new)
        .orElseThrow(() -> new NotFoundException(User.class, authentication.id))
    );
  }


  @GetMapping(path = "user/connections")
  @ApiOperation(value = "내 친구 목록")
  public ApiResult<List<ConnectedUserDto>> connections(@AuthenticationPrincipal JwtAuthentication authentication) {
    return OK(
      userService.findAllConnectedUser(authentication.id).stream()
        .map(ConnectedUserDto::new)
        .collect(toList())
    );
  }
}

