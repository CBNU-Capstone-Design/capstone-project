package capstone.cycle.like.service;

import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.like.entity.Like;
import capstone.cycle.like.repository.LikeRepository;
import capstone.cycle.post.dto.PostResponseDTO;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.error.PostErrorResult;
import capstone.cycle.post.error.PostException;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponseDTO toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        User user = userRepository.getReferenceById(userId);
        boolean hasLiked = likeRepository.existsByPostIdAndUserId(postId, userId);

        Post updatedPost;
        LikeStatus likeStatus;

        if (hasLiked) {
            likeRepository.deleteByPostIdAndUserId(postId, userId);
            updatedPost = post.decrementLikeCount();
            likeStatus = LikeStatus.UNLIKE;
        } else {
            Like newLike = Like.createLike(post, user);
            likeRepository.save(newLike);
            updatedPost = post.incrementLikeCount();
            likeStatus = LikeStatus.LIKE;
        }

        Post savedPost = postRepository.save(updatedPost);
        return new PostResponseDTO(savedPost, likeStatus);
    }

    public boolean hasUserLikedPost(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    public Long getLikeCount(Long postId) {
        return postRepository.findById(postId)
                .map(Post::getLikeCount)
                .orElse(0L);
    }

}
