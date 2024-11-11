import 'package:flutter/material.dart';
import '../model/post_detail_response.dart';
import '../service/post_service.dart';

class PostDetailPage extends StatefulWidget {
  final int postId;

  PostDetailPage({required this.postId});

  @override
  _PostDetailPageState createState() => _PostDetailPageState();
}

class _PostDetailPageState extends State<PostDetailPage>
    with SingleTickerProviderStateMixin {
  final PostService _postService = PostService();
  final TextEditingController _commentController = TextEditingController();
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;

  bool _isLoading = true;
  bool _isSubmitting = false;
  PostDetailResponse? _postDetail;
  Comment? _replyingTo;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: Duration(milliseconds: 300),
      vsync: this,
    );
    _fadeAnimation =
        Tween<double>(begin: 0.0, end: 1.0).animate(_animationController);
    _loadPostDetail();
    _animationController.forward();
  }

  @override
  void dispose() {
    _commentController.dispose();
    _animationController.dispose();
    super.dispose();
  }

  // 현재 사용자 ID 가져오기 (임시)
  int getCurrentUserId() {
    return 1; // TODO: 실제 사용자 ID 구현
  }

  Future<void> _loadPostDetail() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final detail = await _postService.getPostDetail(widget.postId);
      setState(() {
        _postDetail = detail;
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading post detail: $e');
      setState(() {
        _isLoading = false;
      });
      _showErrorSnackBar('게시글을 불러오는데 실패했습니다.');
    }
  }

  void _showErrorSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red[400],
        behavior: SnackBarBehavior.floating,
        margin: EdgeInsets.all(16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }

  Future<void> _togglePostLike() async {
    try {
      final success = await _postService.togglePostLike(widget.postId);
      if (success) {
        await _loadPostDetail();
      } else {
        _showErrorSnackBar('좋아요 처리에 실패했습니다');
      }
    } catch (e) {
      _showErrorSnackBar('오류가 발생했습니다');
    }
  }

  Future<void> _toggleCommentLike(int commentId) async {
    try {
      final success =
          await _postService.toggleCommentLike(widget.postId, commentId);
      if (success) {
        await _loadPostDetail();
      } else {
        _showErrorSnackBar('좋아요 처리에 실패했습니다');
      }
    } catch (e) {
      _showErrorSnackBar('오류가 발생했습니다');
    }
  }

  Future<void> _showDeleteDialog({
    required String title,
    required String content,
    required Function() onDelete,
  }) async {
    return showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          title: Text(
            title,
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
          content: Text(content),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: Text(
                '취소',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                onDelete();
              },
              child: Text(
                '삭제',
                style: TextStyle(
                  color: Colors.red[400],
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Future<void> _deletePost() async {
    try {
      final success = await _postService.deletePost(widget.postId);
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('게시글이 삭제되었습니다'),
            backgroundColor: Colors.green,
            behavior: SnackBarBehavior.floating,
            margin: EdgeInsets.all(16),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
        );
        Navigator.of(context).pop(true);
      } else {
        _showErrorSnackBar('게시글 삭제에 실패했습니다');
      }
    } catch (e) {
      _showErrorSnackBar('오류가 발생했습니다');
    }
  }

  Future<void> _deleteComment(int commentId) async {
    try {
      final success =
          await _postService.deleteComment(widget.postId, commentId);
      if (success) {
        await _loadPostDetail();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('댓글이 삭제되었습니다'),
            backgroundColor: Colors.green,
            behavior: SnackBarBehavior.floating,
            margin: EdgeInsets.all(16),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
        );
      } else {
        _showErrorSnackBar('댓글 삭제에 실패했습니다');
      }
    } catch (e) {
      _showErrorSnackBar('오류가 발생했습니다');
    }
  }

  Future<void> _submitComment() async {
    if (_isSubmitting) return;

    final content = _commentController.text.trim();
    if (content.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('댓글 내용을 입력해주세요'),
          backgroundColor: Colors.red[400],
          behavior: SnackBarBehavior.floating,
          margin: EdgeInsets.all(16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
      );
      return;
    }

    setState(() {
      _isSubmitting = true;
    });

    try {
      final success = await _postService.createComment(
        postId: widget.postId,
        content: content,
        parentId: _replyingTo?.id,
      );

      if (success) {
        _commentController.clear();
        setState(() {
          _replyingTo = null;
        });

        // 댓글 작성 후 목록 새로고침
        await _loadPostDetail();

        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('댓글이 등록되었습니다'),
            backgroundColor: Colors.green,
            behavior: SnackBarBehavior.floating,
            margin: EdgeInsets.all(16),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('댓글 등록에 실패했습니다'),
            backgroundColor: Colors.red[400],
            behavior: SnackBarBehavior.floating,
            margin: EdgeInsets.all(16),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
          ),
        );
      }
    } catch (e) {
      print('Error submitting comment: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('댓글 등록 중 오류가 발생했습니다'),
          backgroundColor: Colors.red[400],
          behavior: SnackBarBehavior.floating,
          margin: EdgeInsets.all(16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
      );
    } finally {
      setState(() {
        _isSubmitting = false;
      });
    }
  }

  void _showImageViewer(
      BuildContext context, List<PostImage> images, int initialIndex) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ImageViewerPage(
          images: images,
          initialIndex: initialIndex,
        ),
      ),
    );
  }

  Widget _buildCommentInput() {
    return Container(
      color: Colors.white,
      padding: EdgeInsets.fromLTRB(
          16, 8, 16, MediaQuery.of(context).viewPadding.bottom + 8),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (_replyingTo != null)
            Container(
              padding: EdgeInsets.all(12),
              margin: EdgeInsets.only(bottom: 8),
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey[200]!),
              ),
              child: Row(
                children: [
                  Icon(Icons.reply,
                      size: 16, color: Theme.of(context).primaryColor),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      '${_replyingTo!.author.nickname}님에게 답글 작성 중',
                      style: TextStyle(
                        color: Theme.of(context).primaryColor,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  GestureDetector(
                    // IconButton 대신 GestureDetector 사용
                    onTap: () {
                      setState(() {
                        _replyingTo = null;
                      });
                    },
                    child: Padding(
                      padding: EdgeInsets.all(8),
                      child: Icon(
                        Icons.close,
                        size: 20,
                        color: Colors.grey[600],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              color: Colors.grey[50],
              border: Border.all(color: Colors.grey[200]!),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Expanded(
                  child: TextField(
                    controller: _commentController,
                    decoration: InputDecoration(
                      hintText: _replyingTo != null ? '답글을 입력하세요' : '댓글을 입력하세요',
                      border: InputBorder.none,
                      contentPadding: EdgeInsets.all(16),
                      hintStyle: TextStyle(color: Colors.grey[400]),
                    ),
                    keyboardType: TextInputType.multiline,
                    maxLines: 3,
                    minLines: 1,
                  ),
                ),
                Padding(
                  padding: EdgeInsets.only(right: 8, bottom: 8),
                  child: ElevatedButton(
                    onPressed: _isSubmitting ? null : _submitComment,
                    style: ElevatedButton.styleFrom(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      padding:
                          EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    child: _isSubmitting
                        ? SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              valueColor:
                                  AlwaysStoppedAnimation<Color>(Colors.white),
                            ),
                          )
                        : Text('등록'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCommentItem(Comment comment) {
    final bool isCommentAuthor = comment.author.id == getCurrentUserId();

    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        border: Border(bottom: BorderSide(color: Colors.grey[200]!)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              if (comment.author.profileImageUrl != null)
                CircleAvatar(
                  backgroundImage:
                      NetworkImage(comment.author.profileImageUrl!),
                  radius: 16,
                )
              else
                CircleAvatar(
                  child: Text(
                    comment.author.nickname[0],
                    style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  backgroundColor: Theme.of(context).primaryColor,
                  radius: 16,
                ),
              SizedBox(width: 8),
              Text(
                comment.author.nickname,
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              Spacer(),
              Text(
                comment.createdAt,
                style: TextStyle(color: Colors.grey[600], fontSize: 12),
              ),
              if (isCommentAuthor)
                IconButton(
                  icon: Icon(Icons.more_vert),
                  onPressed: () => _showDeleteDialog(
                    title: '댓글 삭제',
                    content: '이 댓글을 삭제하시겠습니까?',
                    onDelete: () => _deleteComment(comment.id),
                  ),
                ),
            ],
          ),
          SizedBox(height: 8),
          Text(
            comment.content,
            style: TextStyle(fontSize: 16, height: 1.5),
          ),
          SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              if (comment.updatedAt != comment.createdAt)
                Text(
                  '(수정됨)',
                  style: TextStyle(color: Colors.grey[600], fontSize: 12),
                ),
              Spacer(),
              InkWell(
                onTap: () => _toggleCommentLike(comment.id),
                borderRadius: BorderRadius.circular(16),
                child: Container(
                  padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  child: Row(
                    children: [
                      Icon(
                        Icons.favorite,
                        size: 16,
                        color: comment.likedByCurrentUser
                            ? Colors.red
                            : Colors.grey,
                      ),
                      SizedBox(width: 4),
                      Text(
                        '${comment.likeCount}',
                        style: TextStyle(
                          color: comment.likedByCurrentUser
                              ? Colors.red
                              : Colors.grey,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              SizedBox(width: 16),
              TextButton(
                onPressed: () {
                  setState(() {
                    _replyingTo = comment;
                    _commentController.clear();
                  });
                },
                child: Text('답글'),
                style: TextButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  padding: EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                ),
              ),
            ],
          ),
          if (comment.replies.isNotEmpty) ...[
            Container(
              margin: EdgeInsets.only(left: 32, top: 8),
              padding: EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey[200]!),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: comment.replies.map((reply) {
                  final bool isReplyAuthor =
                      reply.author.id == getCurrentUserId();

                  return Container(
                    padding: EdgeInsets.symmetric(vertical: 12),
                    decoration: BoxDecoration(
                      border: Border(
                        bottom: BorderSide(
                          color: Colors.grey[200]!,
                          width: 1,
                        ),
                      ),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            if (reply.author.profileImageUrl != null)
                              CircleAvatar(
                                backgroundImage:
                                    NetworkImage(reply.author.profileImageUrl!),
                                radius: 12,
                              )
                            else
                              CircleAvatar(
                                child: Text(
                                  reply.author.nickname[0],
                                  style: TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.white,
                                  ),
                                ),
                                backgroundColor: Theme.of(context).primaryColor,
                                radius: 12,
                              ),
                            SizedBox(width: 8),
                            Text(
                              reply.author.nickname,
                              style: TextStyle(fontWeight: FontWeight.w500),
                            ),
                            Spacer(),
                            Text(
                              reply.createdAt,
                              style: TextStyle(
                                  color: Colors.grey[600], fontSize: 12),
                            ),
                            if (isReplyAuthor)
                              IconButton(
                                icon: Icon(Icons.more_vert, size: 18),
                                onPressed: () => _showDeleteDialog(
                                  title: '답글 삭제',
                                  content: '이 답글을 삭제하시겠습니까?',
                                  onDelete: () => _deleteComment(reply.id),
                                ),
                                constraints: BoxConstraints(),
                                padding: EdgeInsets.all(8),
                              ),
                          ],
                        ),
                        SizedBox(height: 8),
                        Text(
                          reply.content,
                          style: TextStyle(fontSize: 14, height: 1.5),
                        ),
                        SizedBox(height: 8),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            if (reply.updatedAt != reply.createdAt)
                              Text(
                                '(수정됨)',
                                style: TextStyle(
                                    color: Colors.grey[600], fontSize: 12),
                              ),
                            Spacer(),
                            InkWell(
                              onTap: () => _toggleCommentLike(reply.id),
                              borderRadius: BorderRadius.circular(16),
                              child: Container(
                                padding: EdgeInsets.symmetric(
                                    horizontal: 8, vertical: 4),
                                child: Row(
                                  children: [
                                    Icon(
                                      Icons.favorite,
                                      size: 14,
                                      color: reply.likedByCurrentUser
                                          ? Colors.red
                                          : Colors.grey,
                                    ),
                                    SizedBox(width: 4),
                                    Text(
                                      '${reply.likeCount}',
                                      style: TextStyle(
                                        fontSize: 12,
                                        color: reply.likedByCurrentUser
                                            ? Colors.red
                                            : Colors.grey,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  );
                }).toList(),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildPostContent() {
    final post = _postDetail!.post;
    final bool isAuthor = post.author.id == getCurrentUserId();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: EdgeInsets.all(16),
          color: Colors.white,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                post.title,
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  height: 1.4,
                ),
              ),
              SizedBox(height: 16),
              Row(
                children: [
                  if (post.author.profileImageUrl != null)
                    CircleAvatar(
                      backgroundImage:
                          NetworkImage(post.author.profileImageUrl!),
                      radius: 20,
                    )
                  else
                    CircleAvatar(
                      child: Text(
                        post.author.nickname[0],
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      radius: 20,
                    ),
                  SizedBox(width: 12),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        post.author.nickname,
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          fontSize: 16,
                        ),
                      ),
                      Text(
                        post.createdAt,
                        style: TextStyle(
                          color: Colors.grey[600],
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                  if (isAuthor) Spacer(),
                  IconButton(
                    icon: Icon(Icons.more_vert),
                    onPressed: () => _showDeleteDialog(
                      title: '게시글 삭제',
                      content: '이 게시글을 삭제하시겠습니까?',
                      onDelete: _deletePost,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        if (post.imageUrls.isNotEmpty)
          Container(
            height: 250,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: post.imageUrls.length,
              padding: EdgeInsets.symmetric(horizontal: 16, vertical: 16),
              itemBuilder: (context, index) {
                return GestureDetector(
                  onTap: () => _showImageViewer(context, post.imageUrls, index),
                  child: Container(
                    margin: EdgeInsets.only(right: 12),
                    width: 250,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(12),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.1),
                          blurRadius: 8,
                          offset: Offset(0, 2),
                        ),
                      ],
                    ),
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Hero(
                        tag: 'image_${post.imageUrls[index].imageId}',
                        child: Image.network(
                          post.imageUrls[index].imageUrl,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) {
                            return Container(
                              color: Colors.grey[200],
                              child: Icon(
                                Icons.error_outline,
                                color: Colors.grey[400],
                                size: 32,
                              ),
                            );
                          },
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        Container(
          padding: EdgeInsets.all(16),
          color: Colors.white,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                post.content,
                style: TextStyle(
                  fontSize: 16,
                  height: 1.6,
                  color: Colors.black87,
                ),
              ),
              SizedBox(height: 24),
              Row(
                children: [
                  _buildStatItem(
                    icon: Icons.remove_red_eye_outlined,
                    count: post.viewCount,
                  ),
                  SizedBox(width: 24),
                  InkWell(
                    onTap: _togglePostLike,
                    borderRadius: BorderRadius.circular(20),
                    child: _buildStatItem(
                      icon: Icons.favorite,
                      count: post.likeCount,
                      isActive: post.likeStatus == 'LIKE',
                    ),
                  ),
                  SizedBox(width: 24),
                  _buildStatItem(
                    icon: Icons.chat_bubble_outline,
                    count: post.commentCount,
                  ),
                ],
              ),
            ],
          ),
        ),
        Container(
          width: double.infinity,
          color: Colors.grey[100],
          padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Text(
            '댓글 ${_postDetail!.comments.length}개',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildStatItem({
    required IconData icon,
    required int count,
    bool isActive = false,
  }) {
    final color = isActive ? Colors.red : Colors.grey[600];

    return Row(
      children: [
        Icon(
          icon,
          size: 20,
          color: color,
        ),
        SizedBox(width: 4),
        Text(
          count.toString(),
          style: TextStyle(
            color: color,
            fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(
          title: Text('게시글'),
          elevation: 0,
        ),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_postDetail == null) {
      return Scaffold(
        appBar: AppBar(
          title: Text('게시글'),
          elevation: 0,
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.error_outline,
                size: 48,
                color: Colors.grey[400],
              ),
              SizedBox(height: 16),
              Text(
                '게시글을 불러올 수 없습니다',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontSize: 16,
                ),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text('게시글'),
        elevation: 0,
      ),
      body: Column(
        children: [
          Expanded(
            child: RefreshIndicator(
              onRefresh: _loadPostDetail,
              child: SingleChildScrollView(
                physics: AlwaysScrollableScrollPhysics(),
                child: FadeTransition(
                  opacity: _fadeAnimation,
                  child: Column(
                    children: [
                      _buildPostContent(),
                      Column(
                        children: _postDetail!.comments
                            .map((comment) => _buildCommentItem(comment))
                            .toList(),
                      ),
                      SizedBox(height: 100),
                    ],
                  ),
                ),
              ),
            ),
          ),
          // 댓글 입력 영역을 Material과 InkWell로 감싸기
          Material(
            elevation: 8,
            color: Colors.white,
            child: InkWell(
              onTap: () {
                showModalBottomSheet(
                  context: context,
                  isScrollControlled: true,
                  backgroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius:
                        BorderRadius.vertical(top: Radius.circular(16)),
                  ),
                  builder: (context) => Padding(
                    padding: EdgeInsets.only(
                      bottom: MediaQuery.of(context).viewInsets.bottom,
                    ),
                    child: Container(
                      padding: EdgeInsets.all(16),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (_replyingTo != null)
                            Container(
                              padding: EdgeInsets.all(8),
                              margin: EdgeInsets.only(bottom: 8),
                              decoration: BoxDecoration(
                                color: Colors.grey[100],
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Row(
                                children: [
                                  Text(
                                    '${_replyingTo!.author.nickname}님에게 답글 작성 중',
                                    style: TextStyle(color: Colors.blue),
                                  ),
                                  Spacer(),
                                  IconButton(
                                    icon: Icon(Icons.close),
                                    onPressed: () {
                                      setState(() {
                                        _replyingTo = null;
                                      });
                                    },
                                  ),
                                ],
                              ),
                            ),
                          TextField(
                            controller: _commentController,
                            autofocus: true,
                            decoration: InputDecoration(
                              hintText: _replyingTo != null
                                  ? '답글을 입력하세요'
                                  : '댓글을 입력하세요',
                              border: OutlineInputBorder(),
                            ),
                            maxLines: 3,
                          ),
                          SizedBox(height: 16),
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              onPressed: () {
                                _submitComment();
                                Navigator.pop(context);
                              },
                              child: Padding(
                                padding: EdgeInsets.symmetric(vertical: 12),
                                child: Text('등록'),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                child: Row(
                  children: [
                    Expanded(
                      child: Container(
                        padding:
                            EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        decoration: BoxDecoration(
                          color: Colors.grey[100],
                          borderRadius: BorderRadius.circular(24),
                          border: Border.all(color: Colors.grey[300]!),
                        ),
                        child: Text(
                          '댓글을 입력하세요',
                          style: TextStyle(color: Colors.grey[600]),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class ImageViewerPage extends StatefulWidget {
  final List<PostImage> images;
  final int initialIndex;

  ImageViewerPage({
    required this.images,
    required this.initialIndex,
  });

  @override
  _ImageViewerPageState createState() => _ImageViewerPageState();
}

class _ImageViewerPageState extends State<ImageViewerPage> {
  late PageController _pageController;
  late int _currentIndex;
  bool _isZoomed = false;
  TransformationController _transformationController =
      TransformationController();

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
    _pageController = PageController(initialPage: widget.initialIndex);
  }

  @override
  void dispose() {
    _pageController.dispose();
    _transformationController.dispose();
    super.dispose();
  }

  void _resetZoom() {
    _transformationController.value = Matrix4.identity();
    setState(() {
      _isZoomed = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: IconThemeData(color: Colors.white),
        title: Text(
          '${_currentIndex + 1} / ${widget.images.length}',
          style: TextStyle(color: Colors.white),
        ),
      ),
      body: GestureDetector(
        onTap: () {
          if (_isZoomed) {
            _resetZoom();
          }
        },
        child: PageView.builder(
          controller: _pageController,
          onPageChanged: (index) {
            setState(() {
              _currentIndex = index;
              _resetZoom();
            });
          },
          itemCount: widget.images.length,
          itemBuilder: (context, index) {
            return Center(
              child: Hero(
                tag: 'image_${widget.images[index].imageId}',
                child: InteractiveViewer(
                  transformationController: _transformationController,
                  minScale: 1.0,
                  maxScale: 4.0,
                  onInteractionStart: (details) {
                    setState(() {
                      _isZoomed = true;
                    });
                  },
                  onInteractionEnd: (details) {
                    if (_transformationController.value.getMaxScaleOnAxis() ==
                        1.0) {
                      setState(() {
                        _isZoomed = false;
                      });
                    }
                  },
                  child: Image.network(
                    widget.images[index].imageUrl,
                    fit: BoxFit.contain,
                    loadingBuilder: (context, child, loadingProgress) {
                      if (loadingProgress == null) return child;
                      return Center(
                        child: CircularProgressIndicator(
                          value: loadingProgress.expectedTotalBytes != null
                              ? loadingProgress.cumulativeBytesLoaded /
                                  loadingProgress.expectedTotalBytes!
                              : null,
                          color: Colors.white,
                        ),
                      );
                    },
                    errorBuilder: (context, error, stackTrace) {
                      return Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.error_outline,
                              color: Colors.white,
                              size: 48,
                            ),
                            SizedBox(height: 16),
                            Text(
                              '이미지를 불러올 수 없습니다',
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 16,
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                ),
              ),
            );
          },
        ),
      ),
      bottomNavigationBar: Container(
        color: Colors.black.withOpacity(0.5),
        padding: EdgeInsets.symmetric(vertical: 16),
        child: SafeArea(
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(
              widget.images.length,
              (index) => Container(
                width: 8,
                height: 8,
                margin: EdgeInsets.symmetric(horizontal: 4),
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: _currentIndex == index
                      ? Colors.white
                      : Colors.white.withOpacity(0.5),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
