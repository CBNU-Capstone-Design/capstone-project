class PostDetailResponse {
  final PostDetail post;
  final List<Comment> comments;

  PostDetailResponse({
    required this.post,
    required this.comments,
  });

  factory PostDetailResponse.fromJson(Map<String, dynamic> json) {
    return PostDetailResponse(
      post: PostDetail.fromJson(json['post']),
      comments: (json['comments'] as List)
          .map((comment) => Comment.fromJson(comment))
          .toList(),
    );
  }
}

class PostDetail {
  final int id;
  final String title;
  final String content;
  final Author author;
  final List<PostImage> imageUrls;
  final String createdAt;  // DateTime에서 String으로 변경
  final String updatedAt;  // DateTime에서 String으로 변경
  final int viewCount;
  final int likeCount;
  final int commentCount;
  final String likeStatus;
  final String categoryName;

  PostDetail({
    required this.id,
    required this.title,
    required this.content,
    required this.author,
    required this.imageUrls,
    required this.createdAt,
    required this.updatedAt,
    required this.viewCount,
    required this.likeCount,
    required this.commentCount,
    required this.likeStatus,
    required this.categoryName,
  });

  factory PostDetail.fromJson(Map<String, dynamic> json) {
    return PostDetail(
      id: json['id'],
      title: json['title'],
      content: json['content'],
      author: Author.fromJson(json['author']),
      imageUrls: (json['imageUrls'] as List)
          .map((image) => PostImage.fromJson(image))
          .toList(),
      createdAt: json['createdAt'],  // 직접 String으로 받기
      updatedAt: json['updatedAt'],  // 직접 String으로 받기
      viewCount: json['viewCount'],
      likeCount: json['likeCount'],
      commentCount: json['commentCount'],
      likeStatus: json['likeStatus'],
      categoryName: json['categoryName'],
    );
  }
}


class Author {
  final int id;
  final String nickname;
  final String? profileImageUrl;

  Author({
    required this.id,
    required this.nickname,
    this.profileImageUrl,
  });

  factory Author.fromJson(Map<String, dynamic> json) {
    return Author(
      id: json['id'],
      nickname: json['nickname'],
      profileImageUrl: json['profileImageUrl'],
    );
  }
}

class PostImage {
  final int imageId;
  final String imageUrl;

  PostImage({
    required this.imageId,
    required this.imageUrl,
  });

  factory PostImage.fromJson(Map<String, dynamic> json) {
    return PostImage(
      imageId: json['imageId'],
      imageUrl: json['imageUrl'],
    );
  }
}

class Comment {
  final int id;
  final String content;
  final Author author;
  final int? parentId;
  final List<Comment> replies;  // String에서 Comment로 변경
  final int likeCount;
  final bool likedByCurrentUser;
  final String createdAt;
  final String updatedAt;

  Comment({
    required this.id,
    required this.content,
    required this.author,
    this.parentId,
    required this.replies,
    required this.likeCount,
    required this.likedByCurrentUser,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Comment.fromJson(Map<String, dynamic> json) {
    return Comment(
      id: json['id'],
      content: json['content'],
      author: Author.fromJson(json['author']),
      parentId: json['parentId'],
      replies: (json['replies'] as List?)
          ?.map((reply) => Comment.fromJson(reply))
          .toList() ?? [],  // replies를 Comment 객체로 변환
      likeCount: json['likeCount'],
      likedByCurrentUser: json['likedByCurrentUser'],
      createdAt: json['createdAt'],
      updatedAt: json['updatedAt'],
    );
  }
}