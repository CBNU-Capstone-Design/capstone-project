import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import '../service/facility_service.dart';
import 'package:url_launcher/url_launcher.dart';
import 'ride_page.dart';
import '../service/authorization_service.dart';

class FacilityMapPage extends StatefulWidget {
  final String userId;
  static const String REQUIRED_TYPE = 'BASIC';

  FacilityMapPage({required this.userId});

  @override
  _FacilityMapPageState createState() => _FacilityMapPageState();
}

class _FacilityMapPageState extends State<FacilityMapPage>
    with SingleTickerProviderStateMixin {
  final AuthorizationService _authService = AuthorizationService();
  final FacilityService _facilityService = FacilityService();
  late AnimationController _animationController;
  late Animation<double> _slideAnimation;
  late Animation<double> _fadeAnimation;

  bool _isAuthorized = false;
  bool _isLoading = true;
  GoogleMapController? _controller;
  Set<Marker> _markers = {};
  String _selectedFacilityName = '';
  String _selectedFacilityAddress = '';
  String? _selectedFacilityGoogleUrl;
  LatLng? _selectedLocation;

  static final CameraPosition _initialPosition = CameraPosition(
    target: LatLng(36.6292, 127.4561),
    zoom: 14,
  );

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: Duration(milliseconds: 300),
      vsync: this,
    );

    _slideAnimation = Tween<double>(
      begin: 100.0,
      end: 0.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOut,
    ));

    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOut,
    ));

    _checkAuthorization();
  }

  @override
  void dispose() {
    _animationController.dispose();
    _controller?.dispose();
    super.dispose();
  }

  Future<void> _checkAuthorization() async {
    try {
      final hasAccess = await _authService.verifyAccess(
          widget.userId, FacilityMapPage.REQUIRED_TYPE);

      if (!hasAccess) {
        if (mounted) {
          await showDialog(
            context: context,
            barrierDismissible: false,
            builder: (BuildContext context) {
              return AlertDialog(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                title: Text(
                  '접근 제한',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                content: Text('현재 등급으로는 이 기능을 사용할 수 없습니다.'),
                actions: [
                  TextButton(
                    child: Text(
                      '확인',
                      style: TextStyle(
                        color: Theme.of(context).primaryColor,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    onPressed: () {
                      Navigator.of(context).pop();
                      Navigator.of(context).pop();
                    },
                  ),
                ],
              );
            },
          );
        }
      } else {
        setState(() => _isAuthorized = true);
        await _loadFacilities();
      }
    } catch (e) {
      _showErrorSnackBar('인증 확인 중 오류가 발생했습니다.');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _loadFacilities() async {
    try {
      final facilities = await _facilityService.getFacilities();
      setState(() {
        _markers = facilities.map((facility) {
          return Marker(
            markerId: MarkerId(facility['id'].toString()),
            position: LatLng(double.parse(facility['lat'].toString()),
                double.parse(facility['lng'].toString())),
            onTap: () {
              setState(() {
                _selectedFacilityName = facility['name']?.toString() ?? '';
                _selectedFacilityAddress =
                    facility['address']?.toString() ?? '';
                _selectedFacilityGoogleUrl =
                    facility['googlePlaceUrl']?.toString();
                _selectedLocation = LatLng(
                    double.parse(facility['lat'].toString()),
                    double.parse(facility['lng'].toString()));
              });
              _animationController.forward();
            },
            icon: BitmapDescriptor.defaultMarkerWithHue(
                BitmapDescriptor.hueAzure),
          );
        }).toSet();
      });
    } catch (e) {
      _showErrorSnackBar('시설 정보를 불러오는데 실패했습니다.');
    }
  }

  void _showErrorSnackBar(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red[400],
        behavior: SnackBarBehavior.floating,
        margin: EdgeInsets.all(16),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
    );
  }

  void _startRide() {
    if (_selectedLocation == null) return;
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => RidePage(destination: _selectedLocation),
      ),
    );
  }

  Widget _buildBottomSheet() {
    return Container(
      padding: EdgeInsets.fromLTRB(20, 20, 20, 32),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10,
            offset: Offset(0, -5),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '시설 정보',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
            ),
          ),
          SizedBox(height: 16),
          Container(
            padding: EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.grey[50],
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.grey[200]!),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      Icons.location_on,
                      color: Theme.of(context).primaryColor,
                      size: 20,
                    ),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _selectedFacilityName,
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
                SizedBox(height: 12),
                Row(
                  children: [
                    Icon(
                      Icons.map_outlined,
                      color: Colors.grey[600],
                      size: 20,
                    ),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _selectedFacilityAddress,
                        style: TextStyle(
                          fontSize: 15,
                          color: Colors.grey[800],
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          SizedBox(height: 20),
          Row(
            children: [
              if (_selectedFacilityGoogleUrl != null)
                Expanded(
                  child: OutlinedButton.icon(
                    icon: Icon(Icons.place),
                    label: Text('구글 지도에서 보기'),
                    style: OutlinedButton.styleFrom(
                      padding: EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      side: BorderSide(color: Theme.of(context).primaryColor),
                    ),
                    onPressed: () async {
                      if (_selectedFacilityGoogleUrl != null) {
                        await launch(_selectedFacilityGoogleUrl!);
                      }
                    },
                  ),
                ),
              if (_selectedFacilityGoogleUrl != null) SizedBox(width: 12),
              Expanded(
                child: ElevatedButton.icon(
                  icon: Icon(Icons.directions_bike),
                  label: Text('주행 시작'),
                  style: ElevatedButton.styleFrom(
                    padding: EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    elevation: 0,
                  ),
                  onPressed: _startRide,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text('시설 지도'),
        elevation: 0,
      ),
      body: Stack(
        children: [
          GoogleMap(
            initialCameraPosition: _initialPosition,
            markers: _markers,
            onMapCreated: (GoogleMapController controller) {
              _controller = controller;
            },
            myLocationEnabled: true,
            myLocationButtonEnabled: true,
            zoomControlsEnabled: false,
            mapToolbarEnabled: false,
          ),
          if (_selectedLocation != null)
            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              child: AnimatedBuilder(
                animation: _slideAnimation,
                builder: (context, child) {
                  return Transform.translate(
                    offset: Offset(0, _slideAnimation.value),
                    child: FadeTransition(
                      opacity: _fadeAnimation,
                      child: child,
                    ),
                  );
                },
                child: _buildBottomSheet(),
              ),
            ),
        ],
      ),
    );
  }
}
