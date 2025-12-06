# 🏛️ DragonFlyBot - Trợ lý ảo tuyển sinh ĐH Duy Tân

> **Hệ thống Chatbot RAG (Retrieval-Augmented Generation) thông minh, sử dụng mô hình ngôn ngữ lớn (LLM) Llama 3.2 chạy trực tiếp trên máy cá nhân.**

---

## 🌟 Điểm nổi bật
* **Local Privacy:** Chạy hoàn toàn Offline trên máy, bảo mật dữ liệu 100%.
* **Chính xác cao:** Trả lời dựa trên dữ liệu thực tế của trường, không bịa đặt ("Strict Mode").
* **Hiệu năng:** Tối ưu hóa tốc độ phản hồi (3-5s) nhờ model lượng tử hóa và caching.
* **API Chuẩn:** Dễ dàng tích hợp với Web/App qua RESTful API.

## 🛠️ Công nghệ (Tech Stack)
* **Core:** Python 3.12, FastAPI, Uvicorn.
* **AI Engine:** LangChain, Ollama (Llama 3.2).
* **Database:** ChromaDB (Vector Store), Nomic-Embed-Text.

---

## 📦 Hướng dẫn Cài đặt (3 Bước đơn giản)

## Bước 1: Cài đặt & Chuẩn bị Model AI
1. Tải và cài đặt phần mềm **Ollama** tại: [https://ollama.com](https://ollama.com).
2. Mở Terminal (CMD/PowerShell) và chạy 2 lệnh sau để tải "bộ não" cho AI:
   ```bash
   ollama pull llama3.2
   ollama pull nomic-embed-text

## Bước 2: Thiết lập môi trường Python
Tại thư mục dự án, chạy lần lượt các lệnh sau:

1. Tạo môi trường ảo
  ```bash
  python -m venv venv

2. Kích hoạt môi trường (Windows)
  ```bash
  .\venv\Scripts\activate

3. Cài đặt thư viện cần thiết
  ```bash
  pip install -r requirements.txt

## Bước 3: Khởi động Server
  ```bash
  python -m uvicorn ai_model:app --reload

Server sẽ chạy tại: http://127.0.0.1:8000

## 🔌 Hướng dẫn sử dụng API

Bạn có thể test nhanh bằng Postman hoặc Thunder Client:

1. Gửi câu hỏi

    URL: POST http://127.0.0.1:8000/chat

    Body (JSON):
    JSON

    {
      "question": "Học phí ngành Công nghệ thông tin là bao nhiêu?"
    }

2. Kết quả trả về
    JSON

    {
      "result": "Học phí ngành CNTT hiện tại là 25.000.000 VNĐ/năm."
    }

## 🌐 (Tùy chọn) Public ra Internet với Ngrok

Để Frontend ở máy khác kết nối được vào API, bạn cần dùng Ngrok.

    Tải và giải nén Ngrok vào thư mục dự án.

    Mở Terminal mới (giữ nguyên Terminal đang chạy Server), gõ lệnh:
    Bash

    # Thay TOKEN của bạn vào bên dưới
    ```bash
    ngrok config add-authtoken <TOKEN_CUA_BAN>

    # Mở cổng public
    ```bash
    ngrok http 8000

    Copy đường dẫn https://xxxx.ngrok-free.app để sử dụng.

## 📂 Cấu trúc dự án
Plaintext

DragonFlyBot/
├── 📄 ai_model.py       # Code xử lý chính (Backend + AI)
├── 📄 data.txt          # Dữ liệu kiến thức nhà trường
├── 📄 requirements.txt  # Danh sách thư viện
├── 📄 README.md         # Tài liệu hướng dẫn
└── 📁 venv/             # Môi trường ảo (Local)

Author: KadePham 🚀