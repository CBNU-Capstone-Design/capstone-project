package capstone.cycle.comment.api;

import capstone.cycle.comment.dto.CommentCreateDTO;
import capstone.cycle.comment.dto.CommentResponseDTO;
import capstone.cycle.comment.dto.CommentUpdateDTO;
import capstone.cycle.comment.service.CommentLikeService;
import capstone.cycle.comment.service.CommentService;
import capstone.cycle.common.security.dto.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/p/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateDTO commentCreateDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CommentResponseDTO createdComment = commentService.createComment(postId, userDetails.getUser().getId(), commentCreateDTO);
        return ResponseEntity.ok(createdComment);

    }

   /* @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId, userDetails.getUser().getId());
        return ResponseEntity.ok(comments);
    }*/

    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateDTO commentUpdateDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CommentResponseDTO updatedComment = commentService.updateComment(postId, commentId, commentUpdateDTO, userDetails.getUser().getId());
        return ResponseEntity.ok(updatedComment);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        commentService.deleteComment(postId, commentId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    //CommentLikeService에서 Like 처리 버전
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentResponseDTO> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        CommentResponseDTO updatedComment = commentLikeService.toggleCommentLike(
                postId,
                commentId,
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(updatedComment);
    }
}
