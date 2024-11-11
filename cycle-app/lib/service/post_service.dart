import 'dart:io';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http_parser/http_parser.dart';
import '../config/env_config.dart';
import '../model/post_category.dart';
import '../model/post_detail_response.dart';

class PostService {
  static final PostService _instance = PostService._internal();
  final String baseUrl = '${EnvConfig.postServiceUrl}/api/p/v1';

  factory PostService() {
    return _instance;
  }

  PostService._internal();

  Future<String?> _getAccessToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('accessToken');
  }

  Future<PostDetailResponse?> getPostDetail(int postId) async {
    try {
      print('Fetching post detail for postId: $postId'); // 요청 시작 로그

      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return null;
      }

      print(
          'Using access token: ${accessToken.substring(0, 10)}...'); // 토큰 확인 로그

      final url = '$baseUrl/$postId/detail';
      print('Request URL: $url'); // URL 확인 로그

      final response = await http.get(
        Uri.parse(url),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Accept': 'application/json',
          'Content-Type': 'application/json; charset=UTF-8',
        },
      );

      print('Response status code: ${response.statusCode}'); // 응답 상태 코드
      print('Response headers: ${response.headers}'); // 응답 헤더

      if (response.statusCode == 200) {
        final decodedBody = utf8.decode(response.bodyBytes);
        print('Response body: $decodedBody'); // 응답 데이터

        final Map<String, dynamic> data = json.decode(decodedBody);
        return PostDetailResponse.fromJson(data);
      } else {
        print(
            'Failed to fetch post detail. Status code: ${response.statusCode}');
        print('Error response: ${utf8.decode(response.bodyBytes)}');
        return null;
      }
    } catch (e, stackTrace) {
      print('Error fetching post detail: $e');
      print('Stack trace: $stackTrace');
      return null;
    }
  }

  Future<bool> togglePostLike(int postId) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return false;
      }

      final response = await http.post(
        Uri.parse('$baseUrl/$postId/like'),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Content-Type': 'application/json; charset=UTF-8',
        },
      );

      print('Like toggle response status: ${response.statusCode}');
      print('Like toggle response body: ${utf8.decode(response.bodyBytes)}');

      return response.statusCode == 200;
    } catch (e) {
      print('Error toggling like: $e');
      return false;
    }
  }

  Future<bool> toggleCommentLike(int postId, int commentId) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return false;
      }

      final response = await http.post(
        Uri.parse('$baseUrl/posts/$postId/comments/$commentId/like'),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Content-Type': 'application/json; charset=UTF-8',
        },
      );

      print('Comment like toggle response status: ${response.statusCode}');
      print(
          'Comment like toggle response body: ${utf8.decode(response.bodyBytes)}');

      return response.statusCode == 200;
    } catch (e) {
      print('Error toggling comment like: $e');
      return false;
    }
  }

  Future<bool> deletePost(int postId) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return false;
      }

      final response = await http.delete(
        Uri.parse('$baseUrl/$postId'),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Content-Type': 'application/json; charset=UTF-8',
        },
      );

      print('Delete post response status: ${response.statusCode}');
      print('Delete post response body: ${utf8.decode(response.bodyBytes)}');

      return response.statusCode == 204;
    } catch (e) {
      print('Error deleting post: $e');
      return false;
    }
  }

  // 댓글 삭제
  Future<bool> deleteComment(int postId, int commentId) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return false;
      }

      final response = await http.delete(
        Uri.parse('$baseUrl/posts/$postId/comments/$commentId'),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Content-Type': 'application/json; charset=UTF-8',
        },
      );

      print('Delete comment response status: ${response.statusCode}');
      print('Delete comment response body: ${utf8.decode(response.bodyBytes)}');

      return response.statusCode == 204;
    } catch (e) {
      print('Error deleting comment: $e');
      return false;
    }
  }

  // 카테고리별 게시글 조회 메서드
  Future<Map<String, dynamic>> getPostsByCategory(
    PostCategory category, {
    int? lastPostId,
  }) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return {'content': [], 'last': true};
      }

      final queryParams = lastPostId != null ? '?lastPostId=$lastPostId' : '';
      final url = '$baseUrl/category/${category.value}$queryParams';

      final response = await http.get(
        Uri.parse(url),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Accept': 'application/json',
          'Content-Type': 'application/json; charset=UTF-8',
        },
      );

      if (response.statusCode == 200) {
        // UTF-8로 디코딩
        final decodedBody = utf8.decode(response.bodyBytes);
        final Map<String, dynamic> data = json.decode(decodedBody);

        // 디버깅을 위한 출력
        print('Decoded response: $data');

        return data;
      } else {
        print('Failed to fetch posts. Status code: ${response.statusCode}');
        return {'content': [], 'last': true};
      }
    } catch (e) {
      print('Error fetching posts: $e');
      return {'content': [], 'last': true};
    }
  }

  Future<bool> createComment({
    required int postId,
    required String content,
    int? parentId,
  }) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return false;
      }

      final response = await http.post(
        Uri.parse('$baseUrl/posts/$postId/comments'),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Accept': 'application/json',
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: json.encode({
          'content': content,
          if (parentId != null) 'parentId': parentId,
        }),
      );

      print('Create comment response status: ${response.statusCode}');
      print('Create comment response body: ${utf8.decode(response.bodyBytes)}');

      return response.statusCode == 200 || response.statusCode == 201;
    } catch (e) {
      print('Error creating comment: $e');
      return false;
    }
  }

  Future<bool> createPost({
    required String title,
    required String content,
    List<File>? imageFiles,
    required String category,
  }) async {
    try {
      final accessToken = await _getAccessToken();
      if (accessToken == null) {
        print('Access token not found');
        return false;
      }

      var uri = Uri.parse('$baseUrl');
      var request = http.MultipartRequest('POST', uri);

      request.headers.addAll({
        'Authorization': 'Bearer $accessToken',
        'Accept': 'application/json',
      });

      // postCreateDTO를 MultipartFile로 추가
      var postCreateDTO = json.encode({
        'title': title,
        'content': content,
        'category': category,
      });

      request.files.add(
        http.MultipartFile.fromString(
          'postCreateDTO',
          postCreateDTO,
          contentType: MediaType('application', 'json'),
        ),
      );

      // 이미지 파일 추가
      if (imageFiles != null && imageFiles.isNotEmpty) {
        for (var i = 0; i < imageFiles.length; i++) {
          var file = imageFiles[i];
          var stream = http.ByteStream(file.openRead());
          var length = await file.length();

          var multipartFile = http.MultipartFile(
            'images',
            stream,
            length,
            filename: 'image_$i.jpg',
            contentType: MediaType('image', 'jpeg'),
          );

          request.files.add(multipartFile);
        }
      }

      var streamedResponse = await request.send();
      var response = await http.Response.fromStream(streamedResponse);

      final decodedBody = utf8.decode(response.bodyBytes);
      print('Response status: ${response.statusCode}');
      print('Response body: $decodedBody');

      return response.statusCode == 200 || response.statusCode == 201;
    } catch (e) {
      print('Error creating post: $e');
      return false;
    }
  }
}
