import 'dart:async';
import 'dart:convert';
import 'package:cycle_app/config/env_config.dart';
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:geolocator/geolocator.dart' as geo;
import 'package:location/location.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_polyline_points/flutter_polyline_points.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:http/http.dart' as http;
import 'ride_history_page.dart';
import 'dart:math' as math;

class RidePage extends StatefulWidget {
  final LatLng? destination;

  RidePage({this.destination});

  @override
  _RidePageState createState() => _RidePageState();
}

class _RidePageState extends State<RidePage>
    with SingleTickerProviderStateMixin {
  GoogleMapController? _mapController;
  Location _locationController = new Location();
  FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
      FlutterLocalNotificationsPlugin();
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;

  final Set<Marker> _markers = {};
  final Set<Polyline> _polylines = {};
  final Set<Polyline> _routePolylines = {};
  List<LatLng> polylineCoordinates = [];

  StreamSubscription<LocationData>? _locationSubscription;
  LocationData? _currentLocation;
  LocationData? _startLocation;
  LatLng? _currentLatLng;

  bool _isRiding = false;
  bool _isLoading = true;
  bool _isInitialized = false;
  double _currentSpeed = 0.0;
  double _distance = 0.0;
  int _duration = 0;
  Timer? _durationTimer;
  Timer? _notificationTimer;
  DateTime? _rideStartTime;

  @override
  void initState() {
    super.initState();
    _initializeServices();
    _animationController = AnimationController(
      duration: Duration(milliseconds: 300),
      vsync: this,
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeOut),
    );

    _animationController.forward();
  }

  Future<void> _initializeServices() async {
    try {
      await _initNotifications();
      await _setupLocationAndRoute();
      if (widget.destination != null) {
        await _addDestinationMarker();
        await _getDirectionsToDestination();
      }
      setState(() => _isLoading = false);
    } catch (e) {
      print('Initialization error: $e');
      _showErrorSnackBar('초기화 중 오류가 발생했습니다.');
    }
  }

  Future<void> _initNotifications() async {
    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@mipmap/ic_launcher');
    final IOSInitializationSettings initializationSettingsIOS =
        IOSInitializationSettings();
    final InitializationSettings initializationSettings =
        InitializationSettings(
      android: initializationSettingsAndroid,
      iOS: initializationSettingsIOS,
    );
    await flutterLocalNotificationsPlugin.initialize(initializationSettings);
  }

  Future<void> _setupLocationAndRoute() async {
    try {
      await _locationController.changeSettings(
        accuracy: LocationAccuracy.high,
        interval: 1000,
        distanceFilter: 5,
      );

      bool serviceEnabled = await _locationController.serviceEnabled();
      if (!serviceEnabled) {
        serviceEnabled = await _locationController.requestService();
        if (!serviceEnabled) return;
      }

      PermissionStatus permission = await _locationController.hasPermission();
      if (permission == PermissionStatus.denied) {
        permission = await _locationController.requestPermission();
        if (permission != PermissionStatus.granted) return;
      }

      _currentLocation = await _locationController.getLocation();
      _currentLatLng = LatLng(
        _currentLocation!.latitude!,
        _currentLocation!.longitude!,
      );

      _updateMarker();

      _locationSubscription = _locationController.onLocationChanged.listen(
        (LocationData currentLocation) {
          setState(() {
            _currentLocation = currentLocation;
            _currentLatLng = LatLng(
              currentLocation.latitude!,
              currentLocation.longitude!,
            );
            _updateMarker();
            if (_isRiding) {
              _updatePolylines();
              _updateDistance();
              _updateSpeed(currentLocation);
              _moveCamera();
            }
          });
        },
      );

      setState(() => _isInitialized = true);
    } catch (e) {
      print('Location setup error: $e');
      _showErrorSnackBar('위치 서비스 설정 중 오류가 발생했습니다.');
    }
  }

  void _moveCamera() {
    if (_mapController == null || _currentLatLng == null) return;
    _mapController!.animateCamera(
      CameraUpdate.newLatLng(_currentLatLng!),
    );
  }

  Future<void> _addDestinationMarker() async {
    if (widget.destination == null) return;

    setState(() {
      _markers.add(Marker(
        markerId: MarkerId('destination'),
        position: widget.destination!,
        icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueRed),
        infoWindow: InfoWindow(title: '목적지'),
      ));
    });
  }

  void _showErrorSnackBar(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(message),
      backgroundColor: Colors.red[400],
      behavior: SnackBarBehavior.floating,
      margin: EdgeInsets.all(16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
    ));
  }

  Future<void> _getDirectionsToDestination() async {
    if (_currentLatLng == null || widget.destination == null) return;

    try {
      final response = await http.get(Uri.parse(
          'https://maps.googleapis.com/maps/api/directions/json?'
          'origin=${_currentLatLng!.latitude},${_currentLatLng!.longitude}'
          '&destination=${widget.destination!.latitude},${widget.destination!.longitude}'
          '&mode=bicycling'
          '&key=${EnvConfig.googleMapKey}'));

      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        if (data['status'] == 'OK') {
          String encodedPoints =
              data['routes'][0]['overview_polyline']['points'];
          List<LatLng> points = PolylinePoints()
              .decodePolyline(encodedPoints)
              .map((point) => LatLng(point.latitude, point.longitude))
              .toList();

          setState(() {
            _routePolylines.clear();
            _routePolylines.add(Polyline(
              polylineId: PolylineId('destination_route'),
              color: Colors.blue[700]!,
              points: points,
              width: 5,
              patterns: [
                PatternItem.dash(30),
                PatternItem.gap(10),
              ],
            ));
          });

          _fitMapBounds(points);
        }
      }
    } catch (e) {
      print('Directions error: $e');
      _showErrorSnackBar('경로를 불러오는데 실패했습니다.');
    }
  }

  void _updatePolylines() {
    if (_currentLocation != null) {
      polylineCoordinates.add(
        LatLng(_currentLocation!.latitude!, _currentLocation!.longitude!),
      );
      setState(() {
        _polylines.clear();
        _polylines.add(Polyline(
          polylineId: PolylineId("ride_route"),
          color: Colors.blue,
          points: polylineCoordinates,
          width: 5,
        ));
      });
    }
  }

  void _updateSpeed(LocationData currentLocation) {
    if (_startLocation != null) {
      double distanceInMeters = geo.Geolocator.distanceBetween(
        _startLocation!.latitude!,
        _startLocation!.longitude!,
        currentLocation.latitude!,
        currentLocation.longitude!,
      );
      double timeInSeconds =
          (currentLocation.time! - _startLocation!.time!) / 1000;
      if (timeInSeconds > 0) {
        setState(() {
          _currentSpeed = (distanceInMeters / timeInSeconds) * 3.6;
        });
      }
    }
  }

  void _updateDistance() {
    if (_startLocation != null && _currentLocation != null) {
      double newDistance = geo.Geolocator.distanceBetween(
            _startLocation!.latitude!,
            _startLocation!.longitude!,
            _currentLocation!.latitude!,
            _currentLocation!.longitude!,
          ) /
          1000;
      setState(() {
        _distance = newDistance;
      });
    }
  }

  void _updateMarker() {
    if (_currentLocation != null) {
      setState(() {
        _markers.removeWhere((marker) =>
            marker.markerId.value == "currentLocation" ||
            marker.markerId.value == "startLocation");

        if (_startLocation != null) {
          _markers.add(Marker(
            markerId: MarkerId("startLocation"),
            position: LatLng(
              _startLocation!.latitude!,
              _startLocation!.longitude!,
            ),
            icon: BitmapDescriptor.defaultMarkerWithHue(
              BitmapDescriptor.hueGreen,
            ),
            infoWindow: InfoWindow(title: '시작 지점'),
          ));
        }

        _markers.add(Marker(
          markerId: MarkerId("currentLocation"),
          position: LatLng(
            _currentLocation!.latitude!,
            _currentLocation!.longitude!,
          ),
          icon: BitmapDescriptor.defaultMarkerWithHue(
            BitmapDescriptor.hueAzure,
          ),
          infoWindow: InfoWindow(title: '현재 위치'),
        ));
      });
    }
  }

  void _startRide() {
    if (!_isInitialized) return;

    setState(() {
      _isRiding = true;
      _distance = 0.0;
      _duration = 0;
      _currentSpeed = 0.0;
      polylineCoordinates.clear();
      _polylines.clear();
      _startLocation = _currentLocation;
      _rideStartTime = DateTime.now();
    });

    if (_startLocation != null) {
      polylineCoordinates.add(
        LatLng(_startLocation!.latitude!, _startLocation!.longitude!),
      );
    }

    _durationTimer = Timer.periodic(Duration(seconds: 1), (timer) {
      setState(() => _duration++);
    });

    _notificationTimer = Timer.periodic(Duration(minutes: 5), (timer) {
      _showNotification(
        '주행 현황',
        '거리: ${_distance.toStringAsFixed(2)}km\n시간: ${_formatDuration(_duration)}',
      );
    });

    _showNotification('주행 시작', '안전한 주행 되세요!');
  }

  void _stopRide() async {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('주행 종료'),
          content: Text('주행을 종료하시겠습니까?'),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          actions: [
            TextButton(
              child: Text(
                '취소',
                style: TextStyle(color: Colors.grey[600]),
              ),
              onPressed: () => Navigator.pop(context),
            ),
            ElevatedButton(
              child: Text('종료'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              onPressed: () {
                Navigator.pop(context);
                _finalizeRide();
              },
            ),
          ],
        );
      },
    );
  }

  Future<void> _finalizeRide() async {
    setState(() => _isRiding = false);
    await _saveRideHistory();
    _durationTimer?.cancel();
    _notificationTimer?.cancel();

    await _showNotification(
      '주행 완료',
      '총 거리: ${_distance.toStringAsFixed(2)}km\n소요 시간: ${_formatDuration(_duration)}',
    );

    _showRideSummary();
  }

  void _showRideSummary() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        padding: EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.vertical(
            top: Radius.circular(20),
          ),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              '주행 결과',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            SizedBox(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildSummaryItem(
                  icon: Icons.straighten,
                  title: '총 거리',
                  value: '${_distance.toStringAsFixed(2)} km',
                  color: Colors.blue,
                ),
                _buildSummaryItem(
                  icon: Icons.timer,
                  title: '소요 시간',
                  value: _formatDuration(_duration),
                  color: Colors.orange,
                ),
                _buildSummaryItem(
                  icon: Icons.speed,
                  title: '평균 속도',
                  value:
                      '${(_distance / (_duration / 3600)).toStringAsFixed(1)} km/h',
                  color: Colors.green,
                ),
              ],
            ),
            SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  padding: EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: Text('확인'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryItem({
    required IconData icon,
    required String title,
    required String value,
    required Color color,
  }) {
    return Column(
      children: [
        Container(
          padding: EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: color),
        ),
        SizedBox(height: 8),
        Text(
          title,
          style: TextStyle(
            color: Colors.grey[600],
            fontSize: 14,
          ),
        ),
        SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 16,
          ),
        ),
      ],
    );
  }

  Future<void> _showNotification(String title, String body) async {
    const AndroidNotificationDetails androidPlatformChannelSpecifics =
        AndroidNotificationDetails(
      'ride_channel_id',
      'Ride Notifications',
      importance: Importance.max,
      priority: Priority.high,
      showWhen: false,
    );
    const NotificationDetails platformChannelSpecifics =
        NotificationDetails(android: androidPlatformChannelSpecifics);

    await flutterLocalNotificationsPlugin.show(
      0,
      title,
      body,
      platformChannelSpecifics,
    );
  }

  Future<void> _saveRideHistory() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      List<String> rideHistories = prefs.getStringList('ride_histories') ?? [];

      Map<String, dynamic> newRide = {
        'date': DateTime.now().toIso8601String(),
        'duration': _duration,
        'distance': _distance,
        'avgSpeed': _duration > 0 ? (_distance / (_duration / 3600)) : 0,
        'path': polylineCoordinates
            .map((point) => [point.latitude, point.longitude])
            .toList(),
      };

      rideHistories.add(jsonEncode(newRide));
      await prefs.setStringList('ride_histories', rideHistories);
    } catch (e) {
      print('Error saving ride history: $e');
      _showErrorSnackBar('주행 기록 저장에 실패했습니다');
    }
  }

  void _fitMapBounds(List<LatLng> points) {
    if (_mapController == null) return;

    double minLat = points.first.latitude;
    double maxLat = points.first.latitude;
    double minLng = points.first.longitude;
    double maxLng = points.first.longitude;

    for (LatLng point in points) {
      minLat = math.min(minLat, point.latitude);
      maxLat = math.max(maxLat, point.latitude);
      minLng = math.min(minLng, point.longitude);
      maxLng = math.max(maxLng, point.longitude);
    }

    _mapController!.animateCamera(
      CameraUpdate.newLatLngBounds(
        LatLngBounds(
          southwest: LatLng(minLat, minLng),
          northeast: LatLng(maxLat, maxLng),
        ),
        100,
      ),
    );
  }

  String _formatDuration(int seconds) {
    int hours = seconds ~/ 3600;
    int minutes = (seconds % 3600) ~/ 60;
    int remainingSeconds = seconds % 60;
    return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${remainingSeconds.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('주행'),
        elevation: 0,
        actions: [
          IconButton(
            icon: Icon(Icons.history),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => RideHistoryPage()),
              );
            },
          ),
        ],
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : Stack(
              children: [
                GoogleMap(
                  initialCameraPosition: CameraPosition(
                    target: _currentLatLng ?? LatLng(36.6292, 127.4561),
                    zoom: 14.0,
                  ),
                  markers: _markers,
                  polylines: {..._polylines, ..._routePolylines},
                  onMapCreated: (GoogleMapController controller) {
                    _mapController = controller;
                    if (_currentLatLng != null && widget.destination != null) {
                      _fitMapBounds([_currentLatLng!, widget.destination!]);
                    }
                  },
                  myLocationEnabled: true,
                  myLocationButtonEnabled: false,
                  zoomControlsEnabled: false,
                  mapToolbarEnabled: false,
                ),
                // 상태 표시 패널
                Positioned(
                  left: 0,
                  right: 0,
                  bottom: 0,
                  child: Column(
                    children: [
                      // 통계 패널
                      Container(
                        margin: EdgeInsets.fromLTRB(16, 0, 16, 16),
                        padding: EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(20),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.1),
                              blurRadius: 10,
                              offset: Offset(0, 4),
                            ),
                          ],
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceAround,
                          children: [
                            _buildStatItem(
                              title: '현재 속도',
                              value: _currentSpeed.toStringAsFixed(1),
                              unit: 'km/h',
                              icon: Icons.speed,
                              iconColor: Colors.blue,
                              bgColor: Colors.blue.withOpacity(0.1),
                            ),
                            Container(
                              height: 40,
                              width: 1,
                              color: Colors.grey[200],
                            ),
                            _buildStatItem(
                              title: '주행 거리',
                              value: _distance.toStringAsFixed(1),
                              unit: 'km',
                              icon: Icons.straighten,
                              iconColor: Colors.green,
                              bgColor: Colors.green.withOpacity(0.1),
                            ),
                            Container(
                              height: 40,
                              width: 1,
                              color: Colors.grey[200],
                            ),
                            _buildStatItem(
                              title: '주행 시간',
                              value: _formatDuration(_duration),
                              icon: Icons.timer,
                              iconColor: Colors.orange,
                              bgColor: Colors.orange.withOpacity(0.1),
                            ),
                          ],
                        ),
                      ),
                      // 주행 시작/종료 버튼
                      Padding(
                        padding: EdgeInsets.fromLTRB(16, 0, 16, 32),
                        child: SizedBox(
                          width: double.infinity,
                          height: 56,
                          child: ElevatedButton.icon(
                            icon:
                                Icon(_isRiding ? Icons.stop : Icons.play_arrow),
                            label: Text(
                              _isRiding ? '주행 종료' : '주행 시작',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            style: ElevatedButton.styleFrom(
                              backgroundColor:
                                  _isRiding ? Colors.red : Colors.blue,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(16),
                              ),
                              elevation: 0,
                            ),
                            onPressed: _isRiding ? _stopRide : _startRide,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
    );
  }

  Widget _buildStatItem({
    required String title,
    required String value,
    String? unit,
    required IconData icon,
    required Color iconColor,
    required Color bgColor,
  }) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          padding: EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: bgColor,
            shape: BoxShape.circle,
          ),
          child: Icon(
            icon,
            color: iconColor,
            size: 24,
          ),
        ),
        SizedBox(height: 8),
        Text(
          title,
          style: TextStyle(
            color: Colors.grey[600],
            fontSize: 12,
          ),
        ),
        SizedBox(height: 4),
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              value,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            if (unit != null) ...[
              SizedBox(width: 2),
              Text(
                unit,
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                ),
              ),
            ],
          ],
        ),
      ],
    );
  }

  @override
  void dispose() {
    _durationTimer?.cancel();
    _notificationTimer?.cancel();
    _locationSubscription?.cancel();
    _mapController?.dispose();
    _animationController.dispose();
    super.dispose();
  }
}
