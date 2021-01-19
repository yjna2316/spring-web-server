package com.study.spring.webserver.controller.post;

import com.study.spring.webserver.configure.support.PageRequest;
import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.error.NotFoundException;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Post;
import com.study.spring.webserver.model.post.Writer;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.security.JwtAuthentication;
import com.study.spring.webserver.service.post.PostService;
import io.swagger.annotations.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.study.spring.webserver.controller.ApiResult.OK;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
@Api(tags = "포스팅 APIs")
public class PostRestController {

  private final PostService postService;

  public PostRestController(PostService postService) {
    this.postService = postService;
  }

  /**
   * 게시물 작성 - '공유하기' 버튼 눌렀을때 trigger
   *
   * @param authentication: id=Id[reference=User,value=4], name="yoonji", email=Email[address=yjna.dev@gmail.com]
   * @param request: PostingRequest[contents=게시물 작성 내용입니다]
   * @return
   **/
  @PostMapping(path = "post")
  @ApiOperation(value = "포스트 작성")
  public ApiResult<PostDto> posting(
    @AuthenticationPrincipal JwtAuthentication authentication,
    @RequestBody PostingRequest request
  ) {
    return OK(
      new PostDto(
        postService.write(
          request.newPost(authentication.id, new Writer(authentication.email))
        )
      )
    );
  }

  // 본인 또는 친구의 포스트를 조회한다.
  @GetMapping(path = "user/{userId}/post/list")
  @ApiOperation(value = "포스트 목록 조회")
  @ApiImplicitParams({
    @ApiImplicitParam(
      name = "offset",
      dataType = "long",
      paramType = "query",
      defaultValue = "0",
      value = "페이징 offset"
    ),
    @ApiImplicitParam(
      name = "limit",
      dataType = "integer",
      paramType = "query",
      defaultValue = "5",
      value = "최대 조회 갯수"
    )
  })
  public ApiResult<List<PostDto>> posts(
    @AuthenticationPrincipal JwtAuthentication authentication,
    @PathVariable
    @ApiParam(value = "조회대상자 PK (본인 또는 친구)", example = "1")
      Long userId,
    PageRequest pageable
  ) {
    /**
     * query parameter에 offset, limit 파라미터를 추가하고 페이징 처리한다.
     * offset: 페이징 offset, 기본값 0
     * limit: 최대 조회 갯수, 기본값 5
     * */
    return OK(
      postService.findAll(authentication.id, Id.of(User.class, userId), pageable.offset(), pageable.limit()).stream()
        .map(PostDto::new)
        .collect(toList())
    );
  }

  /**
   * PUT vs PATCH
   * PUT: 자원의 전체 교체, 자원 교체시 모든 필드 필요 (일부만 전달할 경우, 전달한 필드외 모두 null Or 초기값 처리되니 주의)
   * PATCH: 자원의 부분 교체, 자원교체시 일부 필드 필요
   */
  @PatchMapping(path = "user/{userId}/post/{postId}/like")
  @ApiOperation(value = "포스트 좋아요")
  public ApiResult<PostDto> like(
    @AuthenticationPrincipal JwtAuthentication authentication, // 좋아요 누른 자
    @PathVariable @ApiParam(value = "포스트 작성자 (본인 또는 친구)", example = "1") Long userId,
    @PathVariable @ApiParam(value = "대상 포스트", example = "1") Long postId
  ) {
    // 유저가 포스트 좋아요를 눌렀다. 포스트가 본인 것일 수도 있고, 친구의 것일 수도 있다.
    return OK(
      postService.like(authentication.id, Id.of(Post.class, postId), Id.of(User.class, userId))
        .map(PostDto::new)
        .orElseThrow(() -> new NotFoundException(Post.class, Id.of(Post.class, postId), Id.of(User.class, userId)))
    );
  }
}