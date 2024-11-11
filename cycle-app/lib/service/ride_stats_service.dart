import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class RideStats {
  final Duration totalTime;
  final double totalDistance;
  final double averageSpeed;
  final double totalKcal;
  final List<RideHistory> history;

  RideStats({
    required this.totalTime,
    required this.totalDistance,
    required this.averageSpeed,
    required this.totalKcal,
    required this.history,
  });
}

class RideHistory {
  final DateTime date;
  final int duration;
  final double distance;
  final double avgSpeed;
  final List<List<double>> path;

  RideHistory({
    required this.date,
    required this.duration,
    required this.distance,
    required this.avgSpeed,
    required this.path,
  });

  factory RideHistory.fromJson(Map<String, dynamic> json) {
    return RideHistory(
      date: DateTime.parse(json['date']),
      duration: json['duration'],
      distance: json['distance'].toDouble(),
      avgSpeed: json['avgSpeed'].toDouble(),
      path: (json['path'] as List<dynamic>)
          .map((p) => (p as List<dynamic>)
              .map<double>((e) => double.parse(e.toString()))
              .toList())
          .toList(),
    );
  }
}

class RideStatsService {
  Future<RideStats> calculateRideStats() async {
    final prefs = await SharedPreferences.getInstance();
    List<String> rideHistories = prefs.getStringList('ride_histories') ?? [];

    if (rideHistories.isEmpty) {
      return RideStats(
        totalTime: Duration.zero,
        totalDistance: 0.0,
        averageSpeed: 0.0,
        totalKcal: 0.0,
        history: [],
      );
    }

    List<RideHistory> histories = rideHistories.map((rideJson) {
      return RideHistory.fromJson(jsonDecode(rideJson));
    }).toList();

    // 최신순 정렬
    histories.sort((a, b) => b.date.compareTo(a.date));

    // 총 주행시간 계산 개선
    int totalSeconds = 0;
    for (final history in histories) {
      if (history.duration > 0) {
        // 유효한 시간만 계산
        totalSeconds += history.duration; // duration이 이미 초 단위로 저장되어 있음
      }
    }

    double totalDistance = 0.0;
    double totalSpeedSum = 0.0;
    int validSpeedCount = 0;

    for (final history in histories) {
      if (history.distance > 0) {
        // 유효한 거리만 계산
        totalDistance += history.distance;
      }
      if (history.avgSpeed > 0) {
        // 유효한 속도만 계산
        totalSpeedSum += history.avgSpeed;
        validSpeedCount++;
      }
    }

    // 평균 속도 계산
    double averageSpeed =
        validSpeedCount > 0 ? totalSpeedSum / validSpeedCount : 0.0;

    // 칼로리 계산 개선
    double calculateCalories(double distance, double avgSpeed, int seconds) {
      double hours = seconds / 3600.0; // 초를 시간으로 변환
      double met;
      if (avgSpeed < 16) {
        met = 8.0; // 저강도
      } else if (avgSpeed < 19) {
        met = 10.0; // 중강도
      } else if (avgSpeed < 22) {
        met = 12.0; // 고강도
      } else {
        met = 16.0; // 매우 높은 강도
      }

      return (met * 65 * hours).roundToDouble(); // 65kg 기준
    }

    return RideStats(
      totalTime: Duration(seconds: totalSeconds),
      totalDistance: totalDistance,
      averageSpeed: averageSpeed,
      totalKcal: calculateCalories(totalDistance, averageSpeed, totalSeconds),
      history: histories,
    );
  }

  Future<List<RideHistory>> getRecentRides([int limit = 5]) async {
    final prefs = await SharedPreferences.getInstance();
    List<String> rideHistories = prefs.getStringList('ride_histories') ?? [];

    if (rideHistories.isEmpty) {
      return [];
    }

    List<RideHistory> histories = rideHistories.map((rideJson) {
      return RideHistory.fromJson(jsonDecode(rideJson));
    }).toList();

    histories.sort((a, b) => b.date.compareTo(a.date));
    return histories.take(limit).toList();
  }
}
