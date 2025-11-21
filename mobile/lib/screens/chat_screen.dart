import 'package:flutter/material.dart';
import 'package:flutter_chat_ui/flutter_chat_ui.dart'; // Thư viện UI xịn xò
import 'package:provider/provider.dart';
import '../providers/chat_provider.dart';

class ChatScreen extends StatelessWidget {
  const ChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Lấy data từ Provider
    final chatProvider = context.watch<ChatProvider>();

    return Scaffold(
      appBar: AppBar(title: const Text("My Uni Bot")),
      body: Chat(
        messages: chatProvider.messages, // Danh sách tin lấy từ Provider
        onSendPressed: (partialText) {
          // Khi bấm gửi -> Gọi hàm logic bên Provider
          context.read<ChatProvider>().sendMessage(partialText.text);
        },
        user: chatProvider.user, // User hiện tại
        theme: const DefaultChatTheme( // Giao diện mặc định
          primaryColor: Colors.blueAccent,
        ),
      ),
    );
  }
}