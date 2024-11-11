package capstone.cycle.post.api;

import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.like.service.LikeService;
import capstone.cycle.post.dto.*;
import capstone.cycle.post.entity.PostCategory;
import capstone.cycle.post.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/p/v1")
@RequiredArgsConstructor
public class PostController {


    private final PostService postService;
    private final LikeService likeService;
    private static final int PAGE_SIZE = 20;

    // 커뮤니티 게시글 생성
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPost(
            @RequestPart("postCreateDTO") PostCreateDTO postCreateDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long createdPostId = postService.createPost(postCreateDTO, images, userDetails.getUser().getId());
        return ResponseEntity.ok(createdPostId);
    }

    // 커뮤니티 게시글 조회(게시글O, 댓글X)
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PostResponseDTO post = postService.getPost(id, userDetails.getUser().getId());
        return ResponseEntity.ok(post);
    }

    // 게시글 업데이트
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long id,
            @RequestPart("postUpdateDTO") PostUpdateDTO postUpdateDTO,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        PostResponseDTO updatedPost = postService.updatePost(id, postUpdateDTO, newImages, userDetails.getUser().getId());
        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        postService.deletePost(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // 게시글 좋아요 토글
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponseDTO> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PostResponseDTO updatedPost = likeService.toggleLike(id, userDetails.getUser().getId());
        return ResponseEntity.ok(updatedPost);
    }

    // 카테고리 별 게시글 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/category/{category}")
    public ResponseEntity<Slice<SimplePostResponseDTO>> getPostsByCategory(
            @PathVariable PostCategory category,
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.getPostsByCategory(
                category,
                lastPostId,
                userDetails.getUser().getId(),
                PAGE_SIZE
        );
        return ResponseEntity.ok(posts);
    }

    // 커뮤니티 게시글 조회(게시글O, 댓글O)
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/detail")
    public ResponseEntity<PostDetailResponse> getPostWithComments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        PostDetailResponse postDetail = postService.getPostWithComments(id, userDetails.getUser().getId());
        return ResponseEntity.ok(postDetail);
    }

    //=========== 클럽 내 커뮤니티 ===========

    //클럽 내 게시글 생성
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/clubs/{clubId}/posts")
    public ResponseEntity<Long> createClubPost(
            @PathVariable Long clubId,
            @RequestPart("postCreateDTO") ClubPostCreateRequest clubPostCreateRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long postId = postService.createClubPost(clubId, clubPostCreateRequest, images, userDetails.getUser().getId());
        return ResponseEntity.ok(postId);
    }

    // 클럽 내 게시글 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/clubs/{clubId}/posts")
    public ResponseEntity<Slice<ClubPostPreviewResponse>> getClubPosts(
            @PathVariable Long clubId,
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<ClubPostPreviewResponse> clubPosts = postService.getClubPosts(clubId, userDetails.getUser().getId(), lastPostId, PAGE_SIZE);
        return ResponseEntity.ok(clubPosts);
    }

    // ============ myPage 게시글 ============
    // 일반 커뮤니티 나의 게시글 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my/posts")
    public ResponseEntity<Slice<SimplePostResponseDTO>> getMyPosts(
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.getMyPosts(userDetails.getUser().getId(), lastPostId, PAGE_SIZE);
        return ResponseEntity.ok(posts);
    }

    // 특정 클럽에서 나의 게시글 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my/clubs/{clubId}/posts")
    public ResponseEntity<Slice<SimplePostResponseDTO>> getMyClubPosts(
            @PathVariable Long clubId,
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.getMyClubPosts(
                userDetails.getUser().getId(),
                clubId,
                lastPostId,
                PAGE_SIZE
        );

        return ResponseEntity.ok(posts);
    }

    // ============ 특정 사용자 myPage 게시글 ============
    // 특정 사용자가 쓴 일반 게시글 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<Slice<SimplePostResponseDTO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.getUserPosts(userId, userDetails.getUser().getId(), lastPostId, PAGE_SIZE);
        return ResponseEntity.ok(posts);
    }

    // 특정 사용자가 특정 클럽에서 쓴 게시글 조회
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/users/{userId}/clubs/{clubId}/posts")
    public ResponseEntity<Slice<SimplePostResponseDTO>> getUserClubPosts(
            @PathVariable Long userId,
            @PathVariable Long clubId,
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.getUserClubPosts(userId,
                clubId,
                userDetails.getUser().getId(),
                lastPostId,
                PAGE_SIZE
        );

        return ResponseEntity.ok(posts);
    }

    /*// 일반 커뮤니티 검색
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/posts/search")
    public ResponseEntity<Slice<SimplePostResponseDTO>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long lastPostId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.searchPosts(keyword, lastPostId, userDetails.getUser().getId(), PAGE_SIZE);
        return ResponseEntity.ok(posts);
    }*/
}
