import 'package:flutter_dotenv/flutter_dotenv.dart';

class EnvConfig {
  static String get authServiceUrl => dotenv.env['AUTH_SERVICE_URL'] ?? 'http://localhost:8001';
  static String get facilityServiceUrl => dotenv.env['FACILITY_SERVICE_URL'] ?? 'http://localhost:8000';
  static String get fcmServiceUrl => dotenv.env['FCM_SERVICE_URL'] ?? 'http://localhost:8080';
  static String get postServiceUrl => dotenv.env['POST_SERVICE_URL'] ?? 'http://localhost:8080';
  static String get userServiceUrl => dotenv.env['USER_SERVICE_URL'] ?? 'http://localhost:8080';
  static String get subscriptionServiceUrl => dotenv.env['SUBSCRIPTION_SERVICE_URL']?? 'http://localhost:8001';
  static String get kakaoNativeKey=>dotenv.env['KAKAO_NATIVE_KEY']?? 'aaa';
  static String get googleMapKey=>dotenv.env['GOOGLE_MAP_KEY']??'test';
  static Future<void> load() async {
    await dotenv.load(fileName: ".env");
  }
}