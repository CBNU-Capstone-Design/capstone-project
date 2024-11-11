import 'package:http/http.dart' as http;
import 'dart:convert';

import '../config/env_config.dart';

class SubscriptionResponse {
  final int resultCode;
  final String resultMessage;
  final SubscriptionData resultData;

  SubscriptionResponse({
    required this.resultCode,
    required this.resultMessage,
    required this.resultData,
  });

  factory SubscriptionResponse.fromJson(Map<String, dynamic> json) {
    return SubscriptionResponse(
      resultCode: json['resultCode'],
      resultMessage: json['resultMessage'],
      resultData: SubscriptionData.fromJson(json['resultData']),
    );
  }
}

class SubscriptionData {
  final int userId;
  final String subscriptionType;
  final DateTime startDate;
  final DateTime endDate;

  SubscriptionData({
    required this.userId,
    required this.subscriptionType,
    required this.startDate,
    required this.endDate,
  });

  factory SubscriptionData.fromJson(Map<String, dynamic> json) {
    return SubscriptionData(
      userId: json['userId'],
      subscriptionType: json['subscriptionType'],
      startDate: DateTime.parse(json['startDate']),
      endDate: DateTime.parse(json['endDate']),
    );
  }
}


class SubscriptionService {
  static String get baseUrl => EnvConfig.subscriptionServiceUrl;

  // 포인트 등록 API
  Future<bool> registerPoint(String userId) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/subscribe/register/point'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({'userId': userId}),
      );

      if (response.statusCode == 200) {
        print('포인트 등록 성공');
        return true;
      } else {
        print('포인트 등록 실패: ${response.body}');
        return false;
      }
    } catch (e) {
      print('포인트 등록 에러: $e');
      return false;
    }
  }

  // 구독 등록 API
  Future<bool> registerSubscription(String userId) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/subscribe/register/subscription'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'userId': userId,
          'days': 365,
          'type': 'FREE'
        }),
      );

      if (response.statusCode == 200) {
        print('구독 등록 성공');
        return true;
      } else {
        print('구독 등록 실패: ${response.body}');
        return false;
      }
    } catch (e) {
      print('구독 등록 에러: $e');
      return false;
    }
  }

  Future<SubscriptionData?> getSubscriptionInfo(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/subscribe/load/subscription/$userId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        final subscriptionResponse = SubscriptionResponse.fromJson(json.decode(response.body));
        return subscriptionResponse.resultData;
      } else {
        print('구독 정보 조회 실패: ${response.body}');
        return null;
      }
    } catch (e) {
      print('구독 정보 조회 에러: $e');
      return null;
    }
  }

  Future<bool> updateSubscription(String userId, String type, int days) async {
    try {
      final response = await http.put(
        Uri.parse('$baseUrl/subscribe/renewal/subscription'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'userId': userId,
          'days': days,
          'type': type,
        }),
      );

      if (response.statusCode == 200) {
        return true;
      } else {
        print('구독 등록 실패: ${response.body}');
        return false;
      }
    } catch (e) {
      print('구독 등록 에러: $e');
      return false;
    }
  }
}