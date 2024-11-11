class SimplePost {
  final int id;
  final String title;
  final String categoryName;
  final int viewCount;
  final int likeCount;
  final String likeStatus;
  final String? firstImageUrl;
  final String authorName;
  final int commentCount;

  SimplePost({
    required this.id,
    required this.title,
    required this.categoryName,
    required this.viewCount,
    required this.likeCount,
    required this.likeStatus,
    this.firstImageUrl,
    required this.authorName,
    required this.commentCount,
  });

  factory SimplePost.fromJson(Map<String, dynamic> json) {
    return SimplePost(
      id: json['id'] as int,
      title: json['title'] as String,
      categoryName: json['categoryName'] as String,
      viewCount: json['viewCount'] as int,
      likeCount: json['likeCount'] as int,
      likeStatus: json['likeStatus'] as String,
      firstImageUrl: json['firstImageUrl'] as String?,
      authorName: json['authorName'] as String,
      commentCount: json['commentCount'] as int,
    );
  }
}