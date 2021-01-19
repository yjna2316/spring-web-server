package com.study.spring.webserver.controller.post;

import com.study.spring.webserver.controller.ApiResult;
import com.study.spring.webserver.model.commons.Id;
import com.study.spring.webserver.model.post.Writer;
import com.study.spring.webserver.model.user.User;
import com.study.spring.webserver.security.JwtAuthentication;
import com.study.spring.webserver.service.post.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.study.spring.webserver.controller.ApiResult.OK;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api")
public class PostRestController {

  private final PostService postService;

  public PostRestController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping(path = "post")
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

  @GetMapping(path = "user/{userId}/post/list")
  public ApiResult<List<PostDto>> posts(@PathVariable Long userId) {
    return OK(
      postService.findAll(Id.of(User.class, userId)).stream()
        .map(PostDto::new)
        .collect(toList())
    );
  }

}