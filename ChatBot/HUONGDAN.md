#  My-Uni-Bot – Android + Flask + Ngrok Setup Guide

## 1️⃣ Giới thiệu
My-Uni-Bot là chatbot hỗ trợ sinh viên Trường Đại học Duy Tân.  
Ứng dụng gồm 2 phần:
- **Mobile App:** Android Native (Kotlin + XML)
- **Server:** Python Flask + Transformers (GPT-2 Vietnamese)
- **Tunnel:** Ngrok để public server Flask ra Internet.

---

## 2️⃣ Cấu hình & Cài đặt

### 🧩 Cài Python
Tải và cài **Python 3.8+**, sau đó kiểm tra:
```bash
python --version
🧰 Cài thư viện cần thiết
bash
Copy code
pip install flask flask-cors transformers torch requests
3️⃣ Chạy Flask Server
📁 Mở terminal tại thư mục chứa server.py, sau đó chạy:
bash
Copy code
python server.py
Khi thấy dòng:

csharp
Copy code
 * Running on http://127.0.0.1:5000
→ nghĩa là server Flask đã khởi động thành công.

4️⃣ Bật Ngrok để mở cổng
🔑 Bước 1: Đăng nhập Ngrok
Nếu chưa có tài khoản, đăng ký tại https://dashboard.ngrok.com
Sao chép AuthToken, sau đó chạy:

bash
Copy code
ngrok config add-authtoken <token_ngrok_của_mày>
🌐 Bước 2: Chạy Ngrok
bash
Copy code
ngrok http 5000
Sau khi chạy, Ngrok sẽ hiển thị:

nginx
Copy code
Forwarding    https://abc12345.ngrok-free.app -> http://localhost:5000
➡️ Copy link HTTPS (ví dụ: https://abc12345.ngrok-free.app)

5️⃣ Kết nối Android App với Flask Server
Trong file Kotlin có hàm gọi API, sửa url thành link ngrok mới:

kotlin
Copy code
private fun callPythonBot(message: String) {
    val url = "https://abc12345.ngrok-free.app/chat"  // Thay link mới ở đây

    val json = JSONObject().put("message", message)

    val request = JsonObjectRequest(
        Request.Method.POST, url, json,
        { response ->
            val reply = response.getString("reply")
            receiveMessage(" $reply")
        },
        { error ->
            error.printStackTrace()
            receiveMessage(" Lỗi kết nối server Flask: ${error.message}")
        }
    )
    Volley.newRequestQueue(this).add(request)
}
6️⃣ Kiểm tra hoạt động
Mở Terminal 1 → chạy Flask server

bash
Copy code
python server.py
Mở Terminal 2 → bật Ngrok

bash
Copy code
ngrok http 5000
Dán link HTTPS vào trong Android code.

Mở Android Studio → Run App.

✅ Nếu mọi thứ ổn, app sẽ gửi message đến Flask, Flask xử lý bằng GPT-2 và trả lời lại ngay trong giao diện chat.

7️⃣ Mẹo thêm
Muốn tự động bật ngrok cùng Flask, thêm vào cuối server.py:

python
Copy code
import os
os.system("start cmd /k ngrok http 5000")
Khi cần test nhanh:

bash
Copy code
python server.py
ngrok http 5000
📘 Cấu trúc dự án
bash
Copy code
📁 My-Uni-Bot
├── chatbot.py        # Logic xử lý câu hỏi – trả lời
├── server.py         # Flask API server
├── data.txt          # Dữ liệu huấn luyện Q&A
├── train_bot.py      # Fine-tune GPT-2 model
└── android_app/      # App Android Native (Kotlin + XML)
🎯 Kết quả mong đợi
Khi chạy thành công:

App Android gửi câu hỏi → Flask nhận.

Chatbot xử lý bằng GPT-2 → Trả lời chính xác.

Android hiển thị phản hồi dưới dạng tin nhắn.


Giờ chỉ cần:

bash
Copy code
python server.py
ngrok http 5000
→ rồi mở app Android là chat mượt như butter
