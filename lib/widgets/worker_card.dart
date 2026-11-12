import 'package:flutter/material.dart';
import '../models/user_model.dart';
import 'user_avatar.dart';

class WorkerCard extends StatelessWidget {
  final UserModel worker;
  final VoidCallback onTap;

  const WorkerCard({Key? key, required this.worker, required this.onTap}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        leading: UserAvatar(
          imageUrl: worker.profileImageUrl,
          name: worker.name,
          radius: 24,
        ),
        title: Text(worker.name, style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: const Text("Worker"),
        trailing: const Icon(Icons.arrow_forward_ios, size: 16),
        onTap: onTap,
      ),
    );
  }
}
