import 'package:flutter/material.dart';
import 'package:flutter_chat_types/flutter_chat_types.dart' as types; // Dùng thư viện chat type
import 'package:uuid/uuid.dart'; // Tạo ID ngẫu nhiên cho tin nhắn

class ChatProvider with ChangeNotifier {
  // Danh sách tin nhắn (State quan trọng nhất)
  final List<types.Message> _messages = [];
  final user = const types.User(id: 'user-123'); // Giả lập User hiện tại
  final bot = const types.User(id: 'bot-ai', firstName: 'Trợ lý ảo'); // Giả lập Bot

  List<types.Message> get messages => _messages;

  // Hàm gửi tin nhắn (Logic chính)
  void sendMessage(String text) {
    // 1. Tạo tin nhắn của User và hiện lên ngay
    final textMessage = types.TextMessage(
      author: user,
      createdAt: DateTime.now().millisecondsSinceEpoch,
      id: const Uuid().v4(),
      text: text,
    );

    _messages.insert(0, textMessage); // Thêm vào đầu danh sách
    notifyListeners(); // Báo cho UI cập nhật ngay

    // 2. Giả lập Bot trả lời (Sau này sẽ thay bằng gọi API thật)
    _fakeBotResponse(); 
  }

  void _fakeBotResponse() async {
    await Future.delayed(const Duration(seconds: 1)); // Giả vờ suy nghĩ 1s
    
    final botMessage = types.TextMessage(
      author: bot,
      createdAt: DateTime.now().millisecondsSinceEpoch,
      id: const Uuid().v4(),
      text: "Chào bạn, mình là Bot (đang chạy logic giả). Server chưa kết nối nhé!",
    );

    _messages.insert(0, botMessage);
    notifyListeners();
  }
}