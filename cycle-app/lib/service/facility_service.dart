import 'dart:convert';
import 'package:http/http.dart' as http;

import '../config/env_config.dart';

class FacilityService {
  static String get baseUrl => EnvConfig.facilityServiceUrl;

  Future<List<Map<String, dynamic>>> getFacilities() async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/facility/search'),
        headers: {
          'Content-Type': 'application/json; charset=UTF-8', 
        },
      );

      if (response.statusCode == 200) {
        // UTF-8로 디코딩
        final decodedResponse = utf8.decode(response.bodyBytes);
        List<dynamic> data = json.decode(decodedResponse);
        return data.cast<Map<String, dynamic>>();
      } else {
        throw Exception('Failed to load facilities: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Failed to load facilities: $e');
    }
  }
}