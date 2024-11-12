import 'package:http/http.dart' as http;
import 'dart:convert';

import '../config/env_config.dart';

class AuthorizationResponse {
  final int resultCode;
  final String resultMessage;
  final AuthorizationData resultData;

  AuthorizationResponse({
    required this.resultCode,
    required this.resultMessage,
    required this.resultData,
  });

  factory AuthorizationResponse.fromJson(Map<String, dynamic> json) {
    return AuthorizationResponse(
      resultCode: json['resultCode'],
      resultMessage: json['resultMessage'],
      resultData: AuthorizationData.fromJson(json['resultData']),
    );
  }
}

class AuthorizationData {
  final int userId;
  final String authorization;

  AuthorizationData({
    required this.userId,
    required this.authorization,
  });

  factory AuthorizationData.fromJson(Map<String, dynamic> json) {
    return AuthorizationData(
      userId: json['userId'],
      authorization: json['authorization'],
    );
  }
}

class AuthorizationService {
  static String get baseUrl => '${EnvConfig.authServiceUrl}/api';

  Future<bool> verifyAccess(String userId, String pageRequiredType) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/subscribe/verification/rights'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'userId': int.parse(userId),
          'type': pageRequiredType,
        }),
      );

      if (response.statusCode == 200) {
        final authResponse = AuthorizationResponse.fromJson(json.decode(response.body));
        return authResponse.resultData.authorization == 'AUTHENTICATED';
      }
      return false;
    } catch (e) {
      print('권한 검증 에러: $e');
      return false;
    }
  }
}