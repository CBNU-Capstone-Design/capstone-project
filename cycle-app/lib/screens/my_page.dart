import 'package:flutter/material.dart';
import 'package:kakao_flutter_sdk/kakao_flutter_sdk.dart';
import 'package:intl/intl.dart';
import 'login_page.dart';
import '../service/ride_stats_service.dart';
import '../service/point_service.dart';
import '../service/subscription_service.dart';
import 'ride_history_page.dart';

class MyPage extends StatefulWidget {
  final String username;
  final String? profileImageUrl;
  final String userId;

  MyPage({
    required this.username,
    this.profileImageUrl,
    required this.userId,
  });

  @override
  _MyPageState createState() => _MyPageState();
}

class _MyPageState extends State<MyPage> with SingleTickerProviderStateMixin {
  final RideStatsService _rideStatsService = RideStatsService();
  final SubscriptionService _subscriptionService = SubscriptionService();
  final PointService _pointService = PointService();

  RideStats? _rideStats;
  PointData? _pointData;
  SubscriptionData? _subscriptionData;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadAllData();
  }

  Future<void> _loadAllData() async {
    setState(() => _isLoading = true);
    try {
      await Future.wait([
        _loadRideStats(),
        _loadPointInfo(),
        _loadSubscriptionInfo(),
      ]);
    } catch (e) {
      print('Error loading data: $e');
      _showErrorSnackBar('데이터를 불러오는데 실패했습니다.');
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  Future<void> _loadRideStats() async {
    try {
      final stats = await _rideStatsService.calculateRideStats();
      if (mounted) {
        setState(() => _rideStats = stats);
      }
    } catch (e) {
      print('Error loading ride stats: $e');
      throw e;
    }
  }

  Future<void> _loadPointInfo() async {
    try {
      final pointData = await _pointService.getPointInfo(widget.userId);
      if (mounted && pointData != null) {
        setState(() => _pointData = pointData);
      }
    } catch (e) {
      print('Error loading point info: $e');
      throw e;
    }
  }

  Future<void> _loadSubscriptionInfo() async {
    try {
      final subscriptionData =
          await _subscriptionService.getSubscriptionInfo(widget.userId);
      if (mounted && subscriptionData != null) {
        setState(() => _subscriptionData = subscriptionData);
      }
    } catch (e) {
      print('Error loading subscription info: $e');
      throw e;
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
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    );
  }

  Future<void> _showPointChargeDialog() async {
    final TextEditingController pointController = TextEditingController();
    final formatCurrency = NumberFormat('#,###', 'ko_KR');

    final recommendedAmounts = [10000, 30000, 50000, 100000];

    return showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(Icons.monetization_on, color: Theme.of(context).primaryColor),
            SizedBox(width: 8),
            Text('포인트 충전'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '현재 포인트: ${_pointData != null ? formatCurrency.format(_pointData!.point) : 0}P',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: recommendedAmounts.map((amount) {
                return ChoiceChip(
                  label: Text('${formatCurrency.format(amount)}P'),
                  selected: pointController.text == amount.toString(),
                  onSelected: (selected) {
                    if (selected) {
                      pointController.text = amount.toString();
                    }
                  },
                );
              }).toList(),
            ),
            SizedBox(height: 16),
            TextField(
              controller: pointController,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: '충전할 포인트',
                hintText: '직접 입력',
                suffix: Text('P'),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(
              '취소',
              style: TextStyle(color: Colors.grey[600]),
            ),
          ),
          ElevatedButton(
            onPressed: () async {
              final amount = int.tryParse(pointController.text);
              if (amount == null || amount <= 0) {
                _showErrorSnackBar('올바른 금액을 입력해주세요.');
                return;
              }

              Navigator.pop(context);

              try {
                final success = await _pointService.rechargePoint(
                  widget.userId,
                  amount,
                );

                if (success) {
                  await _loadPointInfo();
                  if (!mounted) return;
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content:
                          Text('${formatCurrency.format(amount)}P가 충전되었습니다'),
                      backgroundColor: Colors.green,
                    ),
                  );
                } else {
                  throw Exception('충전 실패');
                }
              } catch (e) {
                _showErrorSnackBar('충전에 실패했습니다. 다시 시도해주세요.');
              }
            },
            style: ElevatedButton.styleFrom(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            child: Text('충전하기'),
          ),
        ],
      ),
    );
  }

  Widget _buildPointCard() {
    final formatCurrency = NumberFormat('#,###', 'ko_KR');
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 20),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
      elevation: 4,
      child: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              Color(0xFF1E88E5),
              Color(0xFF1976D2),
            ],
          ),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Padding(
          padding: EdgeInsets.all(20),
          child: Column(
            children: [
              Row(
                children: [
                  Container(
                    padding: EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.2),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(
                      Icons.monetization_on,
                      color: Colors.white,
                      size: 24,
                    ),
                  ),
                  SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'BikeDay 포인트',
                          style: TextStyle(
                            color: Colors.white70,
                            fontSize: 14,
                          ),
                        ),
                        SizedBox(height: 4),
                        Text(
                          _pointData != null
                              ? '${formatCurrency.format(_pointData!.point)}P'
                              : '0P',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _showPointChargeDialog,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Color(0xFF1E88E5),
                    padding: EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  child: Text(
                    '포인트 충전',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSubscriptionCard() {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 20),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
      elevation: 4,
      child: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              Color(0xFF9C27B0),
              Color(0xFF7B1FA2),
            ],
          ),
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          children: [
            Padding(
              padding: EdgeInsets.all(20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    _subscriptionData != null
                        ? DateFormat('yyyy.MM.dd')
                            .format(_subscriptionData!.startDate)
                        : '-',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                      color: Colors.white,
                    ),
                  ),
                  Text(
                    _subscriptionData != null
                        ? DateFormat('yyyy.MM.dd')
                            .format(_subscriptionData!.endDate)
                        : '-',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
            ),
            InkWell(
              onTap: () {
                _showSubscriptionRenewalDialog();
              },
              child: Container(
                width: double.infinity,
                padding: EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.only(
                    bottomLeft: Radius.circular(20),
                    bottomRight: Radius.circular(20),
                  ),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      Icons.arrow_upward,
                      color: Color(0xFF9C27B0),
                    ),
                    SizedBox(width: 8),
                    Text(
                      '구독 갱신하기',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF9C27B0),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  double calculateCalories(
      double distance, double avgSpeed, Duration totalTime) {
    double met;
    if (avgSpeed < 19) {
      met = 10.0;
    } else if (avgSpeed < 22) {
      met = 12.0;
    } else {
      met = 16.0;
    }

    double hours = totalTime.inSeconds / 3600.0;
    return met * 65 * hours;
  }

  Future<void> _showSubscriptionRenewalDialog() async {
    final subscriptionTypes = ['PREMIUM', 'STANDARD', 'BASIC'];
    final subscriptionInfo = {
      'PREMIUM': {
        'price': 45000,
        'features': ['모든 기능 이용 가능', '프리미엄 전용 코스 제공', 'VIP 혜택'],
      },
      'STANDARD': {
        'price': 30000,
        'features': ['대부분의 기능 이용 가능', '기본 코스 제공'],
      },
      'BASIC': {
        'price': 15000,
        'features': ['기본 기능 이용'],
      },
    };

    double getDiscountRate(int months) {
      if (months >= 4) return 0.30;
      if (months == 3) return 0.20;
      if (months == 2) return 0.10;
      if (months == 1) return 0.05;
      return 0.0;
    }

    String? selectedType = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(Icons.workspace_premium,
                color: Theme.of(context).primaryColor),
            SizedBox(width: 8),
            Text('구독 플랜 선택'),
          ],
        ),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: subscriptionTypes.map((type) {
              final info = subscriptionInfo[type]!;
              return Card(
                margin: EdgeInsets.only(bottom: 16),
                child: ListTile(
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            type,
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            '${NumberFormat('#,###').format(info['price'])}원/월',
                            style: TextStyle(
                              color: Theme.of(context).primaryColor,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                      SizedBox(height: 8),
                      ...List<Widget>.from(
                        (info['features'] as List).map((feature) => Text(
                              '• $feature',
                              style: TextStyle(
                                fontSize: 14,
                                color: Colors.grey[600],
                              ),
                            )),
                      ),
                    ],
                  ),
                  onTap: () => Navigator.pop(context, type),
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );

    if (selectedType == null) return;

    final periods = [1, 2, 3, 6, 12];
    final int? selectedMonths = await showDialog<int>(
      context: context,
      builder: (context) => AlertDialog(
        title: Row(
          children: [
            Icon(Icons.calendar_today, color: Theme.of(context).primaryColor),
            SizedBox(width: 8),
            Text('구독 기간 선택'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: periods.map((months) {
            final basePrice = subscriptionInfo[selectedType]!['price'] as int;
            final totalBasePrice = basePrice * months;
            final discountRate = getDiscountRate(months);
            final finalPrice = (totalBasePrice * (1 - discountRate)).round();
            final savedAmount = totalBasePrice - finalPrice;

            return Card(
              margin: EdgeInsets.only(bottom: 8),
              child: ListTile(
                title: Text('$months개월'),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (discountRate > 0)
                      Text(
                        '${(discountRate * 100).toInt()}% 할인',
                        style: TextStyle(
                          color: Colors.red,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    Text(
                      '월 ${NumberFormat('#,###').format(finalPrice ~/ months)}원',
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                  ],
                ),
                trailing: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '${NumberFormat('#,###').format(finalPrice)}원',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).primaryColor,
                      ),
                    ),
                    if (savedAmount > 0)
                      Text(
                        '${NumberFormat('#,###').format(savedAmount)}원 절약',
                        style: TextStyle(
                          color: Colors.red,
                          fontSize: 12,
                        ),
                      ),
                  ],
                ),
                onTap: () => Navigator.pop(context, months),
              ),
            );
          }).toList(),
        ),
      ),
    );

    if (selectedMonths == null) return;

    try {
      final success = await _subscriptionService.updateSubscription(
        widget.userId,
        selectedType,
        selectedMonths * 30,
      );

      if (success) {
        await Future.wait([
          _loadSubscriptionInfo(),
          _loadPointInfo(),
        ]);
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('구독이 성공적으로 갱신되었습니다'),
            backgroundColor: Colors.green,
          ),
        );
      } else {
        throw Exception('구독 갱신 실패');
      }
    } catch (e) {
      print('Subscription renewal error: $e');
      _showErrorSnackBar('구독 갱신에 실패했습니다. 포인트가 부족하거나 오류가 발생했습니다.');
    }
  }

  Widget _buildStatsCard() {
    if (_rideStats == null) return SizedBox.shrink();

    String formatDuration(Duration duration) {

      int totalSeconds = duration.inSeconds;

      int hours = totalSeconds ~/ 3600;
      int minutes = (totalSeconds % 3600) ~/ 60;

      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}';
    }

    final List<Map<String, dynamic>> statItems = [
      {
        'title': '누적 주행 시간',
        'value': formatDuration(_rideStats!.totalTime),
        'icon': Icons.timer_outlined,
        'color': Colors.blue,
      },
      {
        'title': '누적 주행 거리',
        'value': '${_rideStats!.totalDistance.toStringAsFixed(1)}km',
        'icon': Icons.straighten,
        'color': Colors.green,
      },
      {
        'title': '평균 속도',
        'value': '${_rideStats!.averageSpeed.toStringAsFixed(1)}km/h',
        'icon': Icons.speed,
        'color': Colors.orange,
      },
      {
        'title': '소모 칼로리',
        'value': '${calculateCalories(
          _rideStats!.totalDistance,
          _rideStats!.averageSpeed,
          _rideStats!.totalTime,
        ).toStringAsFixed(0)}kcal',
        'icon': Icons.local_fire_department_outlined,
        'color': Colors.red,
      }
    ];

    return Container(
      margin: EdgeInsets.symmetric(horizontal: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 4, vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '주행 통계',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                TextButton.icon(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (context) => RideHistoryPage()),
                    );
                  },
                  icon: Icon(Icons.history),
                  label: Text('주행기록'),
                  style: TextButton.styleFrom(
                    foregroundColor: Theme.of(context).primaryColor,
                  ),
                ),
              ],
            ),
          ),
          GridView.builder(
            shrinkWrap: true,
            physics: NeverScrollableScrollPhysics(),
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              childAspectRatio: 1.5,
              crossAxisSpacing: 16,
              mainAxisSpacing: 16,
            ),
            itemCount: statItems.length,
            itemBuilder: (context, index) {
              final item = statItems[index];
              return Card(
                elevation: 2,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        item['icon'] as IconData,
                        color: (item['color'] as MaterialColor)[300],
                        size: 28,
                      ),
                      SizedBox(height: 8),
                      Text(
                        item['value'] as String,
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.black87,
                        ),
                      ),
                      SizedBox(height: 4),
                      Text(
                        item['title'] as String,
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[600],
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  void _logout() async {
    try {
      await UserApi.instance.logout();
      if (!mounted) return;
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (context) => LoginPage()),
        (Route<dynamic> route) => false,
      );
    } catch (error) {
      print('로그아웃 실패: $error');
      _showErrorSnackBar('로그아웃에 실패했습니다. 다시 시도해주세요.');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('마이페이지'),
        elevation: 0,
      ),
      body: RefreshIndicator(
        onRefresh: _loadAllData,
        child: _isLoading
            ? Center(child: CircularProgressIndicator())
            : SingleChildScrollView(
                physics: AlwaysScrollableScrollPhysics(),
                child: Column(
                  children: [
                    Container(
                      padding: EdgeInsets.all(24),
                      decoration: BoxDecoration(
                        color: Theme.of(context).primaryColor,
                        borderRadius: BorderRadius.only(
                          bottomLeft: Radius.circular(32),
                          bottomRight: Radius.circular(32),
                        ),
                      ),
                      child: Column(
                        children: [
                          CircleAvatar(
                            radius: 50,
                            backgroundImage: widget.profileImageUrl != null
                                ? NetworkImage(widget.profileImageUrl!)
                                : AssetImage('assets/default_profile.png')
                                    as ImageProvider,
                          ),
                          SizedBox(height: 16),
                          Text(
                            widget.username,
                            style: TextStyle(
                              fontSize: 24,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                          if (_subscriptionData != null) ...[
                            SizedBox(height: 8),
                            Container(
                              padding: EdgeInsets.symmetric(
                                horizontal: 12,
                                vertical: 6,
                              ),
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.2),
                                borderRadius: BorderRadius.circular(20),
                              ),
                              child: Text(
                                _subscriptionData!.subscriptionType,
                                style: TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                    SizedBox(height: 24),
                    _buildPointCard(),
                    SizedBox(height: 24),
                    _buildSubscriptionCard(),
                    SizedBox(height: 24),
                    _buildStatsCard(),
                    SizedBox(height: 24),
                    Container(
                      margin: EdgeInsets.all(20),
                      width: double.infinity,
                      child: ElevatedButton(
                        onPressed: _logout,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.red,
                          padding: EdgeInsets.symmetric(vertical: 16),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                          elevation: 0,
                        ),
                        child: Text(
                          '로그아웃',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ),
                    SizedBox(height: 20),
                  ],
                ),
              ),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
  }
}
