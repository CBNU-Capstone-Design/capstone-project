package capstone.cycle.comment.service;

import capstone.cycle.club.repository.ClubMemberRepository;
import capstone.cycle.comment.dto.CommentCreateDTO;
import capstone.cycle.comment.dto.CommentResponseDTO;
import capstone.cycle.comment.dto.CommentUpdateDTO;
import capstone.cycle.comment.entity.Comment;
import capstone.cycle.comment.entity.CommentLike;
import capstone.cycle.comment.error.CommentErrorResult;
import capstone.cycle.comment.error.CommentException;
import capstone.cycle.comment.repository.CommentRepository;
import capstone.cycle.common.security.role.UserRole;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ClubMemberRepository clubMemberRepository;

    @Transactional
    public CommentResponseDTO createComment(Long postId, Long userId, CommentCreateDTO commentCreateDTO) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.POST_NOT_FOUND));

        // 클럽 게시글인 경우 클럽 회원 권한 체크
        if (post.getClub() != null) {
            validateClubMemberForComment(post.getClub().getId(), userId);
        }

        // 사용자 존재 여부 확인
        User user = userRepository.getReferenceById(userId);

        // 댓글 내용 검증
        validateCommentContent(commentCreateDTO.getContent());

        Comment comment;
        if (commentCreateDTO.getParentId() != null) {
            // 부모 댓글이 존재하고, 같은 게시물의 댓글인지 확인
            if (!commentRepository.existsByIdAndPostIdAndParentIsNull(commentCreateDTO.getParentId(), postId)) {
                throw new CommentException(CommentErrorResult.INVALID_PARENT_COMMENT);
            }

            Comment parentComment = commentRepository.findByIdWithParent(commentCreateDTO.getParentId())
                    .orElseThrow(() -> new CommentException(CommentErrorResult.INVALID_PARENT_COMMENT));

            if (parentComment.isReply()) {
                throw new CommentException(CommentErrorResult.NESTED_REPLY_NOT_ALLOWED);
            }

            comment = Comment.createReply(commentCreateDTO.getContent(), post, user, parentComment);
        } else {
            comment = Comment.createComment(commentCreateDTO.getContent(), post, user);
        }

        // 댓글 수 증가
        Post updatedPost = post.incrementCommentCount();
        postRepository.save(updatedPost);

        Comment savedComment = commentRepository.save(comment);
        return CommentResponseDTO.from(savedComment, userId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByPostId(Long postId, Long currentUserId) {
        if (!postRepository.existsById(postId)) {
            throw new CommentException(CommentErrorResult.POST_NOT_FOUND);
        }

        // 1. 루트 댓글만 조회
        List<Comment> parentComments = commentRepository.findParentCommentsByPostId(postId);

        // 2. 대댓글 조회
        final Map<Long, List<CommentResponseDTO>> replyDTOMap;
        if (!parentComments.isEmpty()) {
            List<Long> parentIds = parentComments.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());

            List<Comment> replies = commentRepository.findRepliesByParentIds(parentIds);

            // 대댓글들을 DTO로 변환하여 맵에 저장
            replyDTOMap = Map.copyOf(replies.stream()
                    .map(reply -> CommentResponseDTO.from(reply, currentUserId))
                    .collect(Collectors.groupingBy(CommentResponseDTO::getParentId)));
        } else {
            replyDTOMap = Map.of();
        }

        // 3. 부모 댓글 DTO 생성할 때 해당하는 대댓글 목록 포함
        return parentComments.stream()
                .map(parent -> {
                    CommentResponseDTO dto = CommentResponseDTO.from(parent, currentUserId);
                    return CommentResponseDTO.builder()
                            .id(dto.getId())
                            .content(dto.getContent())
                            .author(dto.getAuthor())
                            .parentId(null)  // 부모 댓글이므로 null
                            .replies(replyDTOMap.getOrDefault(parent.getId(), List.of()))
                            .likeCount(dto.getLikeCount())
                            .likedByCurrentUser(dto.isLikedByCurrentUser())
                            .createdAt(dto.getCreatedAt())
                            .updatedAt(dto.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDTO updateComment(Long postId, Long commentId, CommentUpdateDTO commentUpdateDTO, Long userId) {
        Comment comment = findCommentWithValidation(postId, commentId);

        // 본인 댓글인지 확인
        validateCommentAuthor(comment, userId);

        // 클럽 게시글인 경우 클럽 회원 권한 체크
        if (comment.getPost().getClub() != null) {
            validateClubMemberForComment(comment.getPost().getClub().getId(), userId);
        }

        validateCommentContent(commentUpdateDTO.getContent());

        Comment updatedComment = comment.updateContent(commentUpdateDTO.getContent());
        Comment savedComment = commentRepository.save(updatedComment);
        return CommentResponseDTO.from(savedComment, userId);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        Comment comment = findCommentWithValidation(postId, commentId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        // ADMIN은 모든 댓글 삭제 가능
        if (user.getRole().equals(UserRole.ADMIN.getRole())) {
            deleteCommentAndUpdateCount(comment);
            return;
        }

        // 본인 댓글인지 확인
        validateCommentAuthor(comment, userId);
        deleteCommentAndUpdateCount(comment);
    }

    @Transactional
    public CommentResponseDTO toggleLike(Long postId, Long commentId, Long userId) {
        Comment comment = findCommentWithValidation(postId, commentId);
        User user = userRepository.getReferenceById(userId);

        boolean hasLiked = comment.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId));

        Comment updatedComment;
        if (hasLiked) {
            CommentLike existingLike = comment.getLikes().stream()
                    .filter(like -> like.getUser().getId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new CommentException(CommentErrorResult.COMMENT_NOT_FOUND));
            updatedComment = comment.removeLike(existingLike);
        } else {
            CommentLike newLike = CommentLike.createCommentLike(comment, user);
            updatedComment = comment.addLike(newLike);
        }

        Comment savedComment = commentRepository.save(updatedComment);
        return CommentResponseDTO.from(savedComment, userId);
    }

    private Comment findCommentWithValidation(Long postId, Long commentId) {
        return commentRepository.findByIdAndPostIdWithDetails(commentId, postId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.COMMENT_NOT_FOUND));
    }

    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new CommentException(CommentErrorResult.INVALID_COMMENT_CONTENT);
        }
    }

    // 댓글 작성자 본인 확인
    private void validateCommentAuthor(Comment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new CommentException(CommentErrorResult.UNAUTHORIZED_DELETION);
        }
    }

    // 댓글 삭제 및 게시글 댓글 수 갱신을 위한 메서드
    private void deleteCommentAndUpdateCount(Comment comment) {
        Post post = comment.getPost();
        Post updatedPost = post.decrementCommentCount();
        postRepository.save(updatedPost);
        commentRepository.delete(comment);
    }

    // 클럽 회원 권한 체크
    private void validateClubMemberForComment(Long clubId, Long userId) {
        if (!clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
            throw new CommentException(CommentErrorResult.UNAUTHORIZED_CLUB_COMMENT);
        }
    }
}
