package org.fastcampus.oruryapi.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fastcampus.oruryapi.base.converter.ApiResponse;
import org.fastcampus.oruryapi.domain.post.converter.dto.PostDto;
import org.fastcampus.oruryapi.domain.post.converter.request.PostCreateRequest;
import org.fastcampus.oruryapi.domain.post.converter.request.PostUpdateRequest;
import org.fastcampus.oruryapi.domain.post.converter.response.PostResponse;
import org.fastcampus.oruryapi.domain.post.converter.response.PostsResponse;
import org.fastcampus.oruryapi.domain.post.converter.response.PostsWithCursorResponse;
import org.fastcampus.oruryapi.domain.post.service.PostLikeService;
import org.fastcampus.oruryapi.domain.post.service.PostService;
import org.fastcampus.oruryapi.domain.post.util.PostMessage;
import org.fastcampus.oruryapi.domain.user.converter.dto.UserDto;
import org.fastcampus.oruryapi.domain.user.service.UserService;
import org.fastcampus.oruryapi.global.constants.NumberConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping
@RestController
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 생성", description = "게시글 정보를 받아, 게시글을 생성한다.")
    @PostMapping("/post")
    public ApiResponse<Object> createPost(@RequestBody PostCreateRequest request) {
        UserDto userDto = userService.getUserDtoById(request.userId());
        PostDto postDto = request.toDto(userDto);

        postService.createPost(postDto);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(PostMessage.POST_CREATED.getMessage())
                .build();
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 id를 받아, 게시글 상세 정보를 돌려준다.")
    @GetMapping("/post/{id}")
    public ApiResponse<PostResponse> getPostById(@PathVariable Long id) {
        PostDto postDto = postService.getPostDtoById(id);

        postService.addViewCount(postDto);

        boolean isLike = postLikeService.isLiked(NumberConstants.USER_ID, id);
        PostResponse response = PostResponse.of(postDto, isLike);

        return ApiResponse.<PostResponse>builder()
                .status(HttpStatus.OK.value())
                .message(PostMessage.POST_READ.getMessage())
                .data(response)
                .build();
    }

    @Operation(summary = "카테고리별 게시글 목록 조회", description = "카테고리(1: 자유게시판, 2: Q&A게시판)와 cursor값을 받아, '카테고리와 cursor값에 따른 다음 게시글 목록'과 'cursor값(목록의 마지막 게시글 id / 조회된 게시글 없다면 -1L)'을 돌려준다.")
    @GetMapping("/posts/{category}")
    public ApiResponse<PostsWithCursorResponse> getPostsByCategory(@PathVariable int category, @RequestParam Long cursor) {
        List<PostDto> postDtos = postService.getPostDtosByCategory(category, cursor, PageRequest.of(0, NumberConstants.POST_PAGINATION_SIZE));
        List<PostsResponse> postsResponses = postDtos.stream()
                .map(PostsResponse::of)
                .toList();

        PostsWithCursorResponse responses = PostsWithCursorResponse.of(postsResponses);

        return ApiResponse.<PostsWithCursorResponse>builder()
                .status(HttpStatus.OK.value())
                .message(PostMessage.POSTS_READ.getMessage())
                .data(responses)
                .build();
    }

    @Operation(summary = "검색어에 따른 게시글 목록 조회", description = "검색어와 cursor값을 받아, '검색어와 cursor값에 따른 다음 게시글 목록'과 'cursor값(목록의 마지막 게시글 id / 조회된 게시글 없다면 -1L)'을 돌려준다.")
    @GetMapping("/posts")
    public ApiResponse<PostsWithCursorResponse> getPostsBySearchWord(@RequestParam String searchWord, Long cursor) {
        List<PostDto> postDtos = postService.getPostDtosBySearchWord(searchWord, cursor, PageRequest.of(0, NumberConstants.POST_PAGINATION_SIZE));
        List<PostsResponse> postsResponses = postDtos.stream()
                .map(PostsResponse::of)
                .toList();

        PostsWithCursorResponse responses = PostsWithCursorResponse.of(postsResponses);

        return ApiResponse.<PostsWithCursorResponse>builder()
                .status(HttpStatus.OK.value())
                .message(PostMessage.POSTS_READ.getMessage())
                .data(responses)
                .build();
    }

    @Operation(summary = "게시글 수정", description = "게시글 정보를 받아, 게시글을 수정한다.")
    @PatchMapping("/post")
    public ApiResponse<Object> updatePost(@RequestBody PostUpdateRequest request) {
        UserDto userDto = userService.getUserDtoById(request.userId());
        PostDto postDto = postService.getPostDtoById(request.id());

        postDto = request.toDto(postDto, userDto);

        postService.updatePost(postDto);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(PostMessage.POST_UPDATED.getMessage())
                .build();
    }

    @Operation(summary = "게시글 삭제", description = "게시글 id를 받아, 게시글을 삭제한다.")
    @DeleteMapping("/post/{id}")
    public ApiResponse<Object> deletePost(@PathVariable Long id) {
        UserDto userDto = userService.getUserDtoById(NumberConstants.USER_ID);
        PostDto postDto = postService.getPostDtoById(id);
        postService.isValidate(postDto, userDto);

        postService.deletePost(postDto);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(PostMessage.POST_DELETED.getMessage())
                .build();
    }
}