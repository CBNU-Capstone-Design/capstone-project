import 'package:http/http.dart' as http;
import 'dart:convert';

import '../config/env_config.dart';

class PointResponse {
  final int resultCode;
  final String resultMessage;
  final PointData resultData;

  PointResponse({
    required this.resultCode,
    required this.resultMessage,
    required this.resultData,
  });

  factory PointResponse.fromJson(Map<String, dynamic> json) {
    return PointResponse(
      resultCode: json['resultCode'],
      resultMessage: json['resultMessage'],
      resultData: PointData.fromJson(json['resultData']),
    );
  }
}

class PointData {
  final int userId;
  final int point;

  PointData({
    required this.userId,
    required this.point,
  });

  factory PointData.fromJson(Map<String, dynamic> json) {
    return PointData(
      userId: json['userId'],
      point: json['point'],
    );
  }
}

class PointService {
  static String get baseUrl => '${EnvConfig.authServiceUrl}/api';

  Future<PointData?> getPointInfo(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/subscribe/load/point/$userId'),
        headers: {'Content-Type': 'application/json'},
      );

      if (response.statusCode == 200) {
        final pointResponse = PointResponse.fromJson(json.decode(response.body));
        return pointResponse.resultData;
      } else {
        print('포인트 조회 실패: ${response.body}');
        return null;
      }
    } catch (e) {
      print('포인트 조회 에러: $e');
      return null;
    }
  }

  // 포인트 충전 메서드 추가
  Future<bool> rechargePoint(String userId, int point) async {
    try {
      final response = await http.put(
        Uri.parse('$baseUrl/subscribe/recharge/point'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'userId': userId,
          'point': point,
        }),
      );

      if (response.statusCode == 200) {
        print('포인트 충전 성공');
        return true;
      } else {
        print('포인트 충전 실패: ${response.body}');
        return false;
      }
    } catch (e) {
      print('포인트 충전 에러: $e');
      return false;
    }
  }
}