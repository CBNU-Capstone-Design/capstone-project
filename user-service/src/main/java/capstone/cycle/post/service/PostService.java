package capstone.cycle.post.service;

import capstone.cycle.club.entity.Club;
import capstone.cycle.club.repository.ClubMemberRepository;
import capstone.cycle.club.service.ClubService;
import capstone.cycle.comment.dto.CommentResponseDTO;
import capstone.cycle.comment.entity.Comment;
import capstone.cycle.comment.repository.CommentRepository;
import capstone.cycle.comment.service.CommentService;
import capstone.cycle.common.security.role.UserRole;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.service.FileService;
import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.like.service.LikeService;
import capstone.cycle.post.dto.*;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.entity.PostCategory;
import capstone.cycle.post.error.PostErrorResult;
import capstone.cycle.post.error.PostException;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final ClubService clubService;
    private final ClubMemberRepository clubMemberRepository;
    private final CommentRepository commentRepository;
    private static final long POPULAR_POST_LIKE_THRESHOLD = 10;


    @Transactional
    public Long createPost(PostCreateDTO postCreateDTO, List<MultipartFile> images, Long userId) {
        validatePostCreation(postCreateDTO, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        Post post = Post.createPost(
                postCreateDTO.getTitle(),
                postCreateDTO.getContent(),
                user,
                postCreateDTO.getCategory()
        );
        postRepository.save(post);
        if (images != null && !images.isEmpty()) {
            List<File> files = fileService.uploadFiles(post,images);
            post = post.withContentImageGroup(files);
        }

        return post.getId();
    }

    @Transactional
    public PostResponseDTO getPost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        incrementViewCountAsync(post);

        LikeStatus likeStatus = likeService.hasUserLikedPost(id, userId)
                ? LikeStatus.LIKE
                : LikeStatus.UNLIKE;

        return new PostResponseDTO(post, likeStatus);
    }

    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> getPostsByCategory(
            PostCategory category,
            Long lastPostId,
            Long userId,
            int size
    ) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> posts = fetchPosts(category, lastPostId, pageable);

        return posts.map(post -> {
            LikeStatus likeStatus = likeService.hasUserLikedPost(post.getId(), userId)
                    ? LikeStatus.LIKE
                    : LikeStatus.UNLIKE;

            return SimplePostResponseDTO.fromPost(post, likeStatus);
        });
    }

    @Transactional
    public PostDetailResponse getPostWithComments(Long id, Long userId) {
        // 게시글 조회
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        // 조회수 증가
        incrementViewCountAsync(post);

        // 좋아요 상태 확인
        LikeStatus likeStatus = likeService.hasUserLikedPost(id, userId)
                ? LikeStatus.LIKE
                : LikeStatus.UNLIKE;

        // 댓글 목록 조회
        List<CommentResponseDTO> comments = commentService.getCommentsByPostId(id, userId);

        return PostDetailResponse.of(post, comments, likeStatus);
    }

    @Transactional
    public PostResponseDTO updatePost(Long id, PostUpdateDTO postUpdateDTO, List<MultipartFile> newImages, Long userId) {
        Post post = findById(id);
        User user = findUserById(userId);

        // 작성자 검증 - 공통
        validateAuthor(post, user);

        Post updatedPost;

        // 클럽 게시글인 경우
        if (post.getClub() != null) {
            validateClubMemberForWrite(post.getClub().getId(), userId);

            updatedPost = post.updateClubContent(
                    postUpdateDTO.getTitle(),
                    postUpdateDTO.getContent()
            );
        } else {
            // 일반 게시글인 경우
            updatedPost = post.updateContent(
                    postUpdateDTO.getTitle(),
                    postUpdateDTO.getContent(),
                    postUpdateDTO.getCategory()
            );
        }

        // 이미지 처리
        if (postUpdateDTO.getDeletedImageIds() != null && !postUpdateDTO.getDeletedImageIds().isEmpty()) {
            fileService.deleteFilesByFileIds(postUpdateDTO.getDeletedImageIds());
        }

        if (newImages != null && !newImages.isEmpty()) {
            List<File> files = fileService.uploadFiles(updatedPost, newImages);
            updatedPost = updatedPost.withContentImageGroup(files);
        }

        Post savedPost = postRepository.save(updatedPost);
        boolean isLiked = likeService.hasUserLikedPost(id, userId);

        return new PostResponseDTO(savedPost, isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE);
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = findById(id);
        User user = findUserById(userId);

        // 관리자나 작성자 검증
        validateDeletePermission(post, user);

        // 연관된 이미지들 삭제
        if (!post.getFiles().isEmpty()) {
            fileService.deleteFiles(post.getFiles());
        }

        postRepository.delete(post);
    }

    private Slice<Post> fetchPosts(PostCategory category, Long lastPostId, Pageable pageable) {
        if (lastPostId == null) {
            return switch (category) {
                case ALL -> postRepository.findFirstPage(pageable);
                case POPULAR -> postRepository.findFirstPagePopularPosts(POPULAR_POST_LIKE_THRESHOLD, pageable);
                default -> postRepository.findFirstPageByCategory(category, pageable);
            };
        } else {
            return switch (category) {
                case ALL -> postRepository.findAllForInfiniteScroll(lastPostId, pageable);
                case POPULAR -> postRepository.findPopularPostsForInfiniteScroll(
                        POPULAR_POST_LIKE_THRESHOLD,
                        lastPostId,
                        pageable
                );
                default -> postRepository.findByCategoryForInfiniteScroll(category, lastPostId, pageable);
            };
        }
    }

    private void validatePostCreation(PostCreateDTO postCreateDTO, Long userId) {
        if (!PostCategory.isValidForCreation(postCreateDTO.getCategory())) {
            throw new PostException(PostErrorResult.INVALID_CATEGORY);
        }

        if (postCreateDTO.getCategory() == PostCategory.NOTICE) {
            User user = userRepository.getReferenceById(userId);
            if (!isAdmin(user)) {
                throw new PostException(PostErrorResult.UNAUTHORIZED_ACTION);
            }
        }
    }

    private void handleImageUpload(Post post,List<MultipartFile> images) {
        try {
            fileService.uploadFiles(post,images);
        } catch (Exception e) {
            log.error("Failed to upload images", e);
            throw new PostException(PostErrorResult.FILE_UPLOAD_ERROR);
        }
    }

    @Async
    @Transactional
    protected void incrementViewCountAsync(Post post) {
        postRepository.incrementViewCount(post.getId());
    }


    private Post updatePostContent(Post post, PostUpdateDTO postUpdateDTO) {
        return post.updateContent(
                postUpdateDTO.getTitle(),
                postUpdateDTO.getContent(),
                postUpdateDTO.getCategory()
        );
    }

    private Post updatePostImages(Post post, List<Long> deletedImageIds, List<MultipartFile> newImages) {
        if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
            handleDeletedImages(deletedImageIds);
        }

        if (newImages != null && !newImages.isEmpty()) {
            handleNewImages(post, newImages);
        }

        return post;
    }


    private void handleDeletedImages(List<Long> deletedImageIds) {
        fileService.deleteFilesByFileIds(deletedImageIds);
    }

    private void handleNewImages(Post post, List<MultipartFile> newImages) {
        fileService.uploadFiles(post,newImages);
    }

//    private List<File> uploadNewFiles(List<MultipartFile> newImages) {
//        return newImages.stream()
//                .map(image -> {
//                    try {
//                        FileDTO fileDTO = fileService.uploadFile(image).toDTO();
//                        return fileService.getFile(fileDTO.getId());
//                    } catch (Exception e) {
//                        throw new PostException(PostErrorResult.FILE_UPLOAD_ERROR);
//                    }
//                })
//                .collect(Collectors.toList());
//    }

    private void validateAuthorization(Post post, User user, PostErrorResult errorResult) {
        if (post.getCategory() == PostCategory.NOTICE && !isAdmin(user)) {
            throw new PostException(PostErrorResult.UNAUTHORIZED_ACTION);
        }

        if (!isAuthorizedToModify(post, user)) {
            throw new PostException(errorResult);
        }
    }

    private boolean isAuthorizedToModify(Post post, User user) {
        return post.getAuthor().getId().equals(user.getId()) ||
                UserRole.ADMIN.getRole().equals(user.getRole());
    }

    private boolean isAdmin(User user) {
        return UserRole.ADMIN.getRole().equals(user.getRole());
    }

    public Long createClubPost(Long clubId, ClubPostCreateRequest clubPostCreateRequest, List<MultipartFile> images, Long userId) {
        // 클럽 존재 여부 확인
        Club club = clubService.findClubById(clubId);

        // 클럽 회원인지 확인
        validateClubMemberForWrite(clubId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        Post clubPost = Post.createClubPost(
                clubPostCreateRequest.getTitle(),
                clubPostCreateRequest.getContent(),
                author,
                club
        );

        Post savedClubPost = postRepository.save(clubPost);

        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<File> files = fileService.uploadFiles(savedClubPost, images);
            savedClubPost = savedClubPost.withContentImageGroup(files);
        }

        return savedClubPost.getId();
    }



    public Slice<ClubPostPreviewResponse> getClubPosts(Long clubId, Long userId, Long lastPostId, int pageSize) {
        // 클럽 존재 여부 확인
        clubService.findClubById(clubId);

        // 클럽 회원인지 확인
        validateClubMemberForRead(clubId, userId);

        Pageable pageable = PageRequest.of(0, pageSize);
        Slice<Post> posts;

        if (lastPostId == null) {
            posts = postRepository.findClubPostsFirstPage(clubId, pageable);
        } else {
            posts = postRepository.findClubPostsNextPage(clubId, lastPostId, pageable);
        }

        return posts.map(post -> {
            Comment latestComment = commentRepository
                    .findLatestCommentByPostId(post.getId(), PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElse(null);

            //좋아요 상태 체크
            boolean isLiked = likeService.hasUserLikedPost(post.getId(), userId);
            LikeStatus likeStatus = isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE;

            return ClubPostPreviewResponse.from(post, latestComment, userId, likeStatus);
        });
    }

    // 조회용 권한 체크
    private void validateClubMemberForRead(Long clubId, Long userId) {
        if (!clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
            throw new PostException(PostErrorResult.UNAUTHORIZED_ACCESS_READ);
        }
    }

    // 작성용 권한 체크
    private void validateClubMemberForWrite(Long clubId, Long userId) {
        if (!clubMemberRepository.existsByClubIdAndUserId(clubId, userId)) {
            throw new PostException(PostErrorResult.UNAUTHORIZED_ACCESS_WRITE);
        }
    }

    // 검증 메서드들
    private Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
    }

    private void validateDeletePermission(Post post, User user) {
        boolean isAdmin = user.getRole().equals(UserRole.ADMIN.getRole());
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());

        if (!isAdmin && !isAuthor) {
            throw new PostException(PostErrorResult.UNAUTHORIZED_DELETE);
        }
    }

    private void validateAuthor(Post post, User user) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new PostException(PostErrorResult.NOT_POST_AUTHOR);
        }
    }

    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> getMyPosts(Long userId, Long lastPostId, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize);
        Slice<Post> posts = lastPostId == null ?
                postRepository.findMyPostsFirstPage(userId, pageable) :
                postRepository.findMyPostsNextPage(userId, lastPostId, pageable);

        return posts.map(post -> {
            boolean isLiked = likeService.hasUserLikedPost(post.getId(), userId);
            return SimplePostResponseDTO.fromPost(post, isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE);
        });
    }

    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> getMyClubPosts(Long userId, Long clubId, Long lastPostId, int pageSize) {
//        validateClubMemberForRead(clubId, userId);
// 클럽 회원이 아니더라고 간단하게 Post를 볼 수 있고 삭제 가능
        Pageable pageable = PageRequest.of(0, pageSize);
        Slice<Post> posts = lastPostId == null ?
                postRepository.findMyClubPostsFirstPage(userId, clubId, pageable) :
                postRepository.findMyClubPostsNextPage(userId,clubId, lastPostId, pageable);

        return posts.map(post -> {
            boolean isLiked = likeService.hasUserLikedPost(post.getId(), userId);
            return SimplePostResponseDTO.fromPost(post, isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE);
        });
    }


    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> getUserPosts(Long targetUserId, Long currentUserId, Long lastPostId, int pageSize) {
        // 존재하는 사용자인지 확인
        findUserById(targetUserId);

        Pageable pageable = PageRequest.of(0, pageSize);
        Slice<Post> posts = lastPostId == null ?
                postRepository.findUserPostsFirstPage(targetUserId, pageable) :
                postRepository.findUserPostsNextPage(targetUserId, lastPostId, pageable);

        return posts.map(post -> {
            boolean isLiked = likeService.hasUserLikedPost(post.getId(), currentUserId);
            return SimplePostResponseDTO.fromPost(post, isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE);
        });
    }

    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> getUserClubPosts(Long targetUserId, Long clubId, Long currentUserId, Long lastPostId, int pageSize) {
        // 존재하는 사용자인지 확인
        findUserById(targetUserId);

        // 현재 사용자가 해당 클럽 회원인지 확인
//        validateClubMemberForRead(clubId, currentUserId);

        Pageable pageable = PageRequest.of(0, pageSize);
        Slice<Post> posts = lastPostId == null ?
                postRepository.findUserClubPostsFirstPage(targetUserId, clubId, pageable) :
                postRepository.findUserClubPostsNextPage(targetUserId, clubId, lastPostId, pageable);

        return posts.map(post -> {
            boolean isLiked = likeService.hasUserLikedPost(post.getId(), currentUserId);
            return SimplePostResponseDTO.fromPost(post, isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE);
        });
    }

    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> searchPosts(String keyword, Long lastPostId, Long userId, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new PostException(PostErrorResult.INVALID_SEARCH_KEYWORD);
        }

        Pageable pageable = PageRequest.of(0, pageSize);
        Slice<Post> posts = lastPostId == null ?
                postRepository.searchPostsFirstPage(keyword.trim(), pageable) :
                postRepository.searchPostsNextPage(keyword.trim(), lastPostId, pageable);

        return posts.map(post -> {
            boolean isLiked = likeService.hasUserLikedPost(post.getId(), userId);
            return SimplePostResponseDTO.fromPost(post, isLiked ? LikeStatus.LIKE : LikeStatus.UNLIKE);
        });
    }
}
