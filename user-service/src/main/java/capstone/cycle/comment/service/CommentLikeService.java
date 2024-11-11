package capstone.cycle.comment.service;

import capstone.cycle.club.repository.ClubMemberRepository;
import capstone.cycle.comment.dto.CommentResponseDTO;
import capstone.cycle.comment.entity.Comment;
import capstone.cycle.comment.entity.CommentLike;
import capstone.cycle.comment.error.CommentErrorResult;
import capstone.cycle.comment.error.CommentException;
import capstone.cycle.comment.repository.CommentLikeRepository;
import capstone.cycle.comment.repository.CommentRepository;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

//사용X
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ClubMemberRepository clubMemberRepository;


    @Transactional
    public CommentResponseDTO toggleCommentLike(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndPostIdWithDetails(commentId, postId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.COMMENT_NOT_FOUND));

        // 클럽 게시글의 댓글인 경우 클럽 회원 권한 체크
        if (comment.getPost().getClub() != null) {
            validateClubMemberForLike(comment.getPost().getClub().getId(), userId);
        }

        boolean hasLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        boolean newLikeStatus;

        if (hasLiked) {
            // 좋아요 삭제
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
            newLikeStatus = false;  // 좋아요가 삭제된 상태
        } else {
            // 좋아요 추가
            User user = userRepository.getReferenceById(userId);
            CommentLike newLike = CommentLike.createCommentLike(comment, user);
            commentLikeRepository.save(newLike);
            newLikeStatus = true;  // 좋아요가 추가된 상태
        }

        // 현재 좋아요 수 조회
        int currentLikeCount = commentLikeRepository.countByCommentId(commentId);

        // 상태가 변경된 후의 댓글 정보로 DTO 생성
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(SimpleUserInfoDTO.from(comment.getAuthor()))
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(comment.getReplies().stream()
                        .map(reply -> CommentResponseDTO.from(reply, userId))
                        .collect(Collectors.toList()))
                .likeCount(currentLikeCount)  // 실제 DB의 현재 좋아요 수
                .likedByCurrentUser(newLikeStatus)  // 방금 변경된 좋아요 상태
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    // 클럽 회원 권한 체크
    private void validateClubMemberForLike(Long clubId, Long userId) {
        if (!clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
            throw new CommentException(CommentErrorResult.UNAUTHORIZED_CLUB_LIKE);
        }
    }
}