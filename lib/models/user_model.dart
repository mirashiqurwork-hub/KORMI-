class UserModel {
  final String id;
  final String name;
  final String? profileImageUrl; // ছবি না থাকলে null হবে

  UserModel({
    required this.id, 
    required this.name, 
    this.profileImageUrl
  });
}
