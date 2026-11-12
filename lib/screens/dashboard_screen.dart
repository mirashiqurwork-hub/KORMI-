import 'package:flutter/material.dart';
import '../models/user_model.dart';
import '../widgets/user_avatar.dart';
import '../widgets/worker_card.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({Key? key}) : super(key: key);

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  // ডেমো ডেটা (আপনার ব্যাকএন্ড থেকে এই ডেটা আসবে)
  final UserModel currentUser = UserModel(
    id: '1',
    name: 'Admin',
    // imageUrl: 'https://example.com/your-image.jpg', // এখানে আপনার ছবির লিংক দিবেন
  );

  final List<UserModel> workers = [
    UserModel(id: '2', name: 'Rahim', profileImageUrl: 'https://i.pravatar.cc/150?img=11'),
    UserModel(id: '3', name: 'Karim', profileImageUrl: 'https://i.pravatar.cc/150?img=12'),
    UserModel(id: '4', name: 'Jamal'), // ছবি নেই, তাই নামের প্রথম অক্ষর দেখাবে
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F6F8), // ব্যাকগ্রাউন্ড কালার
      body: SingleChildScrollView(
        child: Column(
          children: [
            // ১. টপ হেডার সেকশন
            _buildHeader(),

            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // ২. Workforce at a glance বক্স (আপনার স্ক্রিনশটের মতো)
                  _buildWorkforceGlanceCard(),
                  
                  const SizedBox(height: 24),
                  
                  // ৩. মেইন মডিউলস গ্রিড (Active, Inactive, Workers ইত্যাদি)
                  const Text(
                    "Main Modules",
                    style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFF2C3E50)),
                  ),
                  const SizedBox(height: 16),
                  _buildMainModulesGrid(),

                  const SizedBox(height: 24),

                  // ৪. ওয়ার্কার লিস্ট (যেখানে কার্ডের ভেতর ছবি দেখাবে)
                  const Text(
                    "Workers List",
                    style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFF2C3E50)),
                  ),
                  const SizedBox(height: 16),
                  ListView.builder(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: workers.length,
                    itemBuilder: (context, index) {
                      return WorkerCard(
                        worker: workers[index],
                        onTap: () {
                          // প্রোফাইল পেজে যাওয়ার কোড এখানে লিখবেন
                        },
                      );
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ==========================================
  // হেডার উইজেট (প্রোফাইল ছবি ও ড্রপডাউন মেনু)
  // ==========================================
  Widget _buildHeader() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.only(top: 60, left: 20, right: 20, bottom: 30),
      decoration: const BoxDecoration(
        color: Color(0xFF0F1E3D), // ডার্ক নেভি কালার
        borderRadius: BorderRadius.only(
          bottomLeft: Radius.circular(30),
          bottomRight: Radius.circular(30),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: const [
              Text("Welcome back,", style: TextStyle(color: Colors.white70, fontSize: 14)),
              Text("Admin", style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold)),
            ],
          ),
          
          // ড্রপডাউন মেনু সহ অ্যাভাটার
          PopupMenuButton<String>(
            offset: const Offset(0, 50),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            onSelected: (value) {
              if (value == 'profile') {
                // প্রোফাইল পেজে যান
              } else if (value == 'logout') {
                // লগআউট করুন
              }
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
              const PopupMenuItem<String>(
                value: 'profile', 
                child: Row(
                  children: [Icon(Icons.person, color: Colors.blueGrey), SizedBox(width: 8), Text('My Profile')],
                )
              ),
              const PopupMenuDivider(),
              const PopupMenuItem<String>(
                value: 'logout', 
                child: Row(
                  children: [Icon(Icons.logout, color: Colors.red), SizedBox(width: 8), Text('Logout', style: TextStyle(color: Colors.red))],
                )
              ),
            ],
            child: Container(
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white, width: 2),
              ),
              child: UserAvatar(
                imageUrl: currentUser.profileImageUrl,
                name: currentUser.name,
                radius: 22,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ==========================================
  // Workforce at a glance উইজেট
  // ==========================================
  Widget _buildWorkforceGlanceCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFF1A2A47), // গাঢ় নীল
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: const [
              Icon(Icons.trending_up, color: Colors.amber),
              SizedBox(width: 8),
              Text("Workforce at a glance (Today)", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _buildStatBox("0", "Present", Colors.green),
              _buildStatBox("0", "Absent", Colors.red),
              _buildStatBox("0", "Off Day", Colors.amber),
            ],
          )
        ],
      ),
    );
  }

  Widget _buildStatBox(String count, String label, Color color) {
    return Container(
      width: 90,
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.1),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          Text(count, style: TextStyle(color: color, fontSize: 22, fontWeight: FontWeight.bold)),
          const SizedBox(height: 4),
          Text(label, style: const TextStyle(color: Colors.white70, fontSize: 12)),
        ],
      ),
    );
  }

  // ==========================================
  // Main Modules গ্রিড (যেখানে ছবি দেখাতে চেয়েছিলেন)
  // ==========================================
  Widget _buildMainModulesGrid() {
    return GridView.count(
      crossAxisCount: 3,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: 16,
      crossAxisSpacing: 16,
      childAspectRatio: 0.9,
      children: [
        _buildModuleItem(Icons.check_circle_outline, "Active", workers.isNotEmpty ? workers[0] : null),
        _buildModuleItem(Icons.cancel_outlined, "Inactive", null), // ছবি না দিলে আইকনই থাকবে
        _buildModuleItem(Icons.people_outline, "Workers", workers.isNotEmpty ? workers[1] : null),
        _buildModuleItem(Icons.fact_check_outlined, "Attendance", null),
        _buildModuleItem(Icons.groups_outlined, "Manpower", null),
        _buildModuleItem(Icons.build_outlined, "Production", null),
      ],
    );
  }

  // মডিউল আইটেম (এখানে ছবি এবং আইকন দুটোই হ্যান্ডেল করা হয়েছে)
  Widget _buildModuleItem(IconData icon, String title, UserModel? user) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(color: Colors.grey.withOpacity(0.1), spreadRadius: 1, blurRadius: 5, offset: const Offset(0, 2)),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // যদি ইউজার অবজেক্ট থাকে (ছবির জন্য), তবে ছবি দেখাবে। না থাকলে আইকন দেখাবে।
          if (user != null)
            UserAvatar(imageUrl: user.profileImageUrl, name: user.name, radius: 24)
          else
            Icon(icon, size: 36, color: const Color(0xFF0F1E3D)),
          
          const SizedBox(height: 12),
          Text(
            title,
            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: Color(0xFF0F1E3D)),
          ),
        ],
      ),
    );
  }
}
