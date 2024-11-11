import 'package:flutter/material.dart';
import '../service/post_service.dart';
import '../model/simple_post.dart';
import 'write_post_page.dart';
import '../model/post_category.dart';
import 'post_detail_page.dart';

class BoardPage extends StatefulWidget {
  @override
  _BoardPageState createState() => _BoardPageState();
}

class _BoardPageState extends State<BoardPage>
    with SingleTickerProviderStateMixin {
  final PostService _postService = PostService();
  final ScrollController _scrollController = ScrollController();
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;

  PostCategory _selectedCategory = PostCategory.ALL;
  List<SimplePost> _posts = [];
  bool _isLoading = false;
  bool _hasMorePosts = true;
  int? _lastPostId;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: Duration(milliseconds: 300),
      vsync: this,
    );
    _fadeAnimation =
        Tween<double>(begin: 0.0, end: 1.0).animate(_animationController);
    _loadPosts();
    _scrollController.addListener(_scrollListener);
    _animationController.forward();
  }

  void _scrollListener() {
    if (_scrollController.position.pixels ==
        _scrollController.position.maxScrollExtent) {
      if (!_isLoading && _hasMorePosts) {
        _loadMorePosts();
      }
    }
  }

  Future<void> _loadPosts() async {
    if (_isLoading) return;

    setState(() {
      _isLoading = true;
      _posts = [];
      _lastPostId = null;
    });

    try {
      final result = await _postService.getPostsByCategory(_selectedCategory);
      final List<dynamic> postsData = result['content'] ?? [];

      setState(() {
        _posts = postsData.map((data) => SimplePost.fromJson(data)).toList();
        _hasMorePosts = !(result['last'] ?? true);
        _lastPostId = _posts.isNotEmpty ? _posts.last.id : null;
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading posts: $e');
      setState(() {
        _isLoading = false;
      });
      _showErrorSnackBar('게시글을 불러오는데 실패했습니다.');
    }
  }

  Future<void> _loadMorePosts() async {
    if (_isLoading || !_hasMorePosts) return;

    setState(() {
      _isLoading = true;
    });

    try {
      final result = await _postService.getPostsByCategory(
        _selectedCategory,
        lastPostId: _lastPostId,
      );

      final List<dynamic> postsData = result['content'] ?? [];
      final List<SimplePost> newPosts =
          postsData.map((data) => SimplePost.fromJson(data)).toList();

      setState(() {
        _posts.addAll(newPosts);
        _hasMorePosts = !(result['last'] ?? true);
        _lastPostId = newPosts.isNotEmpty ? newPosts.last.id : _lastPostId;
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading more posts: $e');
      setState(() {
        _isLoading = false;
      });
      _showErrorSnackBar('추가 게시글을 불러오는데 실패했습니다.');
    }
  }

  void _showErrorSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
        margin: EdgeInsets.all(16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        backgroundColor: Colors.red[400],
      ),
    );
  }

  bool _shouldShowWriteButton() {
    return _selectedCategory == PostCategory.FREE_BOARD ||
        _selectedCategory == PostCategory.QUESTION_BOARD;
  }

  Widget _buildPostCard(SimplePost post) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 2,
      shadowColor: Colors.black.withOpacity(0.1),
      child: InkWell(
        onTap: () async {
          final result = await Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => PostDetailPage(postId: post.id),
            ),
          );
          if (result == true) {
            _loadPosts();
          }
        },
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                post.title,
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: Colors.black87,
                  height: 1.4,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              if (post.firstImageUrl != null) ...[
                SizedBox(height: 12),
                ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: Image.network(
                    post.firstImageUrl!,
                    height: 200,
                    width: double.infinity,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) {
                      return Container(
                        height: 200,
                        decoration: BoxDecoration(
                          color: Colors.grey[100],
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Center(
                          child: Icon(Icons.error_outline,
                              color: Colors.grey[400]),
                        ),
                      );
                    },
                  ),
                ),
              ],
              SizedBox(height: 12),
              Row(
                children: [
                  CircleAvatar(
                    radius: 14,
                    backgroundColor:
                        Theme.of(context).primaryColor.withOpacity(0.1),
                    child: Text(
                      post.authorName[0],
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).primaryColor,
                      ),
                    ),
                  ),
                  SizedBox(width: 8),
                  Text(
                    post.authorName,
                    style: TextStyle(
                      color: Colors.black87,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Spacer(),
                  _buildStatItem(Icons.remove_red_eye_outlined, post.viewCount),
                  SizedBox(width: 16),
                  _buildStatItem(
                    Icons.favorite,
                    post.likeCount,
                    color: post.likeStatus == 'LIKE' ? Colors.red : null,
                  ),
                  SizedBox(width: 16),
                  _buildStatItem(Icons.chat_bubble_outline, post.commentCount),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatItem(IconData icon, int count, {Color? color}) {
    return Row(
      children: [
        Icon(
          icon,
          size: 16,
          color: color ?? Colors.grey[600],
        ),
        SizedBox(width: 4),
        Text(
          count.toString(),
          style: TextStyle(
            color: color ?? Colors.grey[600],
            fontSize: 14,
          ),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('게시판'),
        elevation: 0,
      ),
      body: Column(
        children: [
          Container(
            color: Colors.white,
            padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Container(
              decoration: BoxDecoration(
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey[300]!),
              ),
              padding: EdgeInsets.symmetric(horizontal: 16),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<PostCategory>(
                  isExpanded: true,
                  value: _selectedCategory,
                  icon:
                      Icon(Icons.keyboard_arrow_down, color: Colors.grey[600]),
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.black87,
                    fontWeight: FontWeight.w500,
                  ),
                  onChanged: (PostCategory? newValue) {
                    if (newValue != null) {
                      setState(() {
                        _selectedCategory = newValue;
                      });
                      _loadPosts();
                    }
                  },
                  items: PostCategory.values.map((category) {
                    return DropdownMenuItem<PostCategory>(
                      value: category,
                      child: Text(
                        category.displayName,
                        style: TextStyle(
                          fontSize: 16,
                          color: Colors.black87,
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ),
            ),
          ),
          Divider(height: 1),
          Expanded(
            child: RefreshIndicator(
              onRefresh: _loadPosts,
              child: AnimatedSwitcher(
                duration: Duration(milliseconds: 300),
                child: _posts.isEmpty && !_isLoading
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.article_outlined,
                              size: 48,
                              color: Colors.grey[400],
                            ),
                            SizedBox(height: 16),
                            Text(
                              '게시글이 없습니다',
                              style: TextStyle(
                                fontSize: 16,
                                color: Colors.grey[600],
                              ),
                            ),
                          ],
                        ),
                      )
                    : ListView.builder(
                        controller: _scrollController,
                        padding: EdgeInsets.only(top: 8, bottom: 100),
                        itemCount: _posts.length + (_hasMorePosts ? 1 : 0),
                        itemBuilder: (context, index) {
                          if (index == _posts.length) {
                            return Center(
                              child: Padding(
                                padding: EdgeInsets.all(16.0),
                                child: CircularProgressIndicator(),
                              ),
                            );
                          }
                          return FadeTransition(
                            opacity: _fadeAnimation,
                            child: _buildPostCard(_posts[index]),
                          );
                        },
                      ),
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: _shouldShowWriteButton()
          ? FloatingActionButton.extended(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => WritePostPage()),
                ).then((_) => _loadPosts());
              },
              icon: Icon(Icons.edit),
              label: Text('글쓰기'),
              elevation: 2,
              backgroundColor: Theme.of(context).primaryColor,
            )
          : null,
    );
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _animationController.dispose();
    super.dispose();
  }
}
