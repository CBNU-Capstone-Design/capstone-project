import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../config/env_config.dart';

class FCMService {
  final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;
  String? _userId;

  Future<void> initialize(String userId) async {
    _userId = userId;
    await _firebaseMessaging.requestPermission();

    String? token = await _firebaseMessaging.getToken();

    if (token != null) {
      await _sendTokenToServer(token);
    }

    _firebaseMessaging.onTokenRefresh.listen(_sendTokenToServer);
  }

  Future<void> _sendTokenToServer(String token) async {
    if (_userId == null) {
      print('Error: userId is not set');
      return;
    }

    String url = _getServerUrl();

    try {
      final response = await http.post(
        Uri.parse('$url/api/fcm/token'),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(<String, String>{
          'token': token,
          'userId': _userId!,
        }),
      );

      if (response.statusCode == 200) {
        print('FCM token sent to server successfully');
      } else {
        print(
            'Failed to send FCM token to server. Status code: ${response.statusCode}');
      }
    } catch (e) {
      print('Error sending FCM token to server: $e');
    }
  }

  String _getServerUrl() {
    return EnvConfig.fcmServiceUrl;
  }
}
