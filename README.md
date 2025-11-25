 

## 📋 Mục lục

- [Tính năng](#tính-năng)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt](#cài-đặt)
- [Hướng dẫn chạy](#hướng-dẫn-chạy)
- [API Documentation](#api-documentation)
- [Xử lý sự cố](#xử-lý-sự-cố)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)

---

## 🎯 Tính năng

✅ **Retrieval System** - Tìm kiếm câu trả lời từ knowledge base
✅ **Generation** - Sinh ra câu trả lời thông minh bằng model GPT2 đã train
✅ **Hybrid Approach** - Kết hợp cả 2 phương pháp
✅ **REST API** - Dễ dàng tích hợp với ứng dụng mobile/web
✅ **CLI Interface** - Chat trực tiếp từ terminal
✅ **Xử lý lỗi** - Fallback mechanism khi không biết

---

## 💻 Yêu cầu hệ thống

- **Python**: 3.8 trở lên
- **RAM**: 4GB tối thiểu (8GB recommended)
- **Disk**: 2GB cho model
- **OS**: Windows, macOS, Linux

### Kiểm tra Python version

```bash
python --version
```

---

## 🔧 Cài đặt

### Bước 1: Clone hoặc download project

```bash
# Nếu dùng Git
git clone <repo-url>
cd DragonFlyBot

# Hoặc download ZIP rồi giải nén
cd DragonFlyBot
```

### Bước 2: Tạo virtual environment (khuyến nghị)

**Windows:**
```bash
python -m venv venv
venv\Scripts\activate
```

**macOS / Linux:**
```bash
python3 -m venv venv
source venv/bin/activate
```

Bạn sẽ thấy `(venv)` ở đầu terminal.

### Bước 3: Cài đặt dependencies

```bash
pip install -r requirements.txt
```

**Thời gian:** 5-10 phút (PyTorch file lớn ~500MB)

### Bước 4: Chuẩn bị dữ liệu

Tạo file `data.txt` cùng thư mục với `train.py`:

```
User: Ban la ai?
Bot: Toi la DragonFlyBot, chatbot ho tro sinh vien Duy Tan

User: Truong Duy Tan o dau?
Bot: Duy Tan nam o Da Nang, thanh pho lon o mien Trung

User: Cac khoa nao o Duy Tan?
Bot: Duy Tan co nhieu khoa: CNTT, Kinh te, Y Duoc, Luat, v.v.

User: Hoc phi bao nhieu?
Bot: Tuy khoa, hoc ki co khoang 5-10 trieu dong
```

**Format quan trọng:**
- Mỗi câu hỏi bắt đầu bằng `User:`
- Mỗi câu trả lời bắt đầu bằng `Bot:`

---

## 🚀 Hướng dẫn chạy

### Option 1: Training Model (Lần đầu)

```bash
python train.py train
```

**Output:**
```
Starting training...
Loading data from data.txt...
Loaded 4 Q&A pairs
Preparing dataset...
Loading GPT2 model...
Training started...

[1/5 01:23, Epoch 1/5]
Step 50: loss = 4.2341

...

Saving model to ./my_gpt2_model...
Model saved successfully
```

**⏱️ Thời gian:** 5-10 phút (tùy máy)

**📁 Output tạo ra:**
- `my_gpt2_model/` - Folder chứa model đã train
  - `pytorch_model.bin` - Model weights
  - `config.json` - Model config
  - `tokenizer.json` - Tokenizer

**⚠️ Chỉ cần chạy 1 lần! Lần sau sẽ load model có sẵn.**

---

### Option 2: Chat Mode (Interactive)

```bash
python train.py chat
```

**Output:**
```
Loading trained model...
Knowledge base loaded: 4 pairs
Chat mode - type 'quit' to exit
You: 
```

**Ví dụ:**
```
You: Truong Duy Tan o dau?
Bot: Duy Tan nam o Da Nang [retrieval]

You: Hoc ở Duy Tan co tot khong?
Bot: Duy Tan la truong dai hoc uy tin... [generated]

You: quit
```

**Giải thích:**
- `[retrieval]` - Trả lời từ knowledge base
- `[generated]` - Sinh ra bằng model
- `[fallback]` - Không biết

---

### Option 3: API Server

**Khởi động server:**
```bash
python server.py
```

**Output:**
```
Loading bot...
Loading trained model...
Knowledge base loaded: 4 pairs
Bot ready!
 * Running on http://127.0.0.1:5000
 * Debug mode: on
```

Server sẵn sàng tại `http://localhost:5000`

---

## 📡 API Documentation

### 1. GET `/`
Kiểm tra trạng thái server

**Command:**
```bash
curl http://localhost:5000/
```

**Response:**
```json
{
  "status": "running",
  "message": "DragonFlyBot API dang chay",
  "version": "2.0",
  "qa_count": 4
}
```

---

### 2. POST `/chat`
Gửi câu hỏi và nhận câu trả lời (Main endpoint)

**Command:**
```bash
curl -X POST http://localhost:5000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Truong Duy Tan o dau?"}'
```

**Request:**
```json
{
  "message": "Ban la ai?"
}
```

**Response:**
```json
{
  "success": true,
  "user_message": "Ban la ai?",
  "reply": "Toi la DragonFlyBot, chatbot ho tro sinh vien Duy Tan",
  "method": "retrieval"
}
```

**Method types:**
- `retrieval` - Trả lời từ knowledge base (nhanh, chính xác)
- `generated` - Sinh ra bằng model (thông minh, linh hoạt)
- `fallback` - Không biết

---

### 3. GET `/health`
Health check

**Command:**
```bash
curl http://localhost:5000/health
```

**Response:**
```json
{
  "status": "healthy",
  "knowledge_base_size": 4
}
```

---

### 4. GET `/info`
Thông tin bot

**Command:**
```bash
curl http://localhost:5000/info
```

**Response:**
```json
{
  "name": "DragonFlyBot",
  "version": "2.0",
  "qa_count": 4,
  "language": "Vietnamese"
}
```

---

## 🧪 Test API - 3 Cách

### Cách 1: Dùng cURL (Command line)

```bash
curl -X POST http://localhost:5000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Truong Duy Tan o dau?"}'
```

### Cách 2: Dùng Postman (GUI)

1. Mở Postman (download từ postman.com nếu chưa có)
2. Chọn **POST**
3. URL: `http://localhost:5000/chat`
4. Tab **Headers**: Thêm `Content-Type: application/json`
5. Tab **Body** → **raw** → chọn **JSON**
6. Nhập:
```json
{
  "message": "Truong Duy Tan o dau?"
}
```
7. Click **Send**

### Cách 3: Python script

Tạo file `test_api.py`:
```python
import requests
import json

url = "http://localhost:5000/chat"
headers = {"Content-Type": "application/json"}
data = {"message": "Truong Duy Tan o dau?"}

response = requests.post(url, json=data, headers=headers)
result = response.json()

print(f"Status: {result['success']}")
print(f"Reply: {result['reply']}")
print(f"Method: {result['method']}")
```

Chạy:
```bash
pip install requests
python test_api.py
```

---

## 🔄 Workflow Chi tiết

### Lần đầu tiên

```bash
# 1. Train model
python train.py train
# Output: Model saved successfully

# 2. Chat hoặc API
python train.py chat
# Hoặc
python server.py
```

### Lần sau

```bash
# Không cần train lại, chỉ load model có sẵn
python train.py chat
# Hoặc
python server.py
```

**Model được load nhanh hơn nhiều (vài giây thay vì vài phút)**

---

## 🐛 Xử lý sự cố

### ❌ Lỗi: "data.txt not found"

**Nguyên nhân:** Chưa tạo file data.txt

**Giải pháp:**
```bash
# Tạo file data.txt
echo. > data.txt

# Copy Q&A vào file
```

---

### ❌ Lỗi: "CUDA out of memory"

**Nguyên nhân:** Máy không đủ RAM

**Giải pháp:** Sửa trong `train.py`:
```python
per_device_train_batch_size=1  # Giảm xuống 1
```

---

### ❌ Lỗi: "ModuleNotFoundError: No module named 'transformers'"

**Nguyên nhân:** Chưa cài dependencies

**Giải pháp:**
```bash
pip install -r requirements.txt
```

---

### ❌ Lỗi: "Address already in use" (khi chạy server)

**Nguyên nhân:** Port 5000 đang dùng

**Giải pháp:** Sửa port trong `server.py`:
```python
app.run(host="0.0.0.0", port=5001, debug=True)  # Thay 5001
```

---

### ❌ Lỗi: "Model not found"

**Nguyên nhân:** Chưa train model

**Giải pháp:**
```bash
python train.py train
```

---

## 📁 Cấu trúc thư mục

```
DragonFlyBot/
├── train.py                    # Dau nao bot - xu ly logic
├── server.py                   # API Server - Flask
├── data.txt                    # Knowledge base - Q&A data
├── my_gpt2_model/              # Model trained (auto tao)
│   ├── config.json
│   ├── pytorch_model.bin
│   ├── tokenizer.json
│   └── special_tokens_map.json
├── requirements.txt            # Dependencies
├── .env                        # Config (optional)
├── .gitignore                  # Git ignore
└── README.md                   # Documentation
```

---

## 📊 So sánh các mode

| Feature | Train | Chat | API Server |
|---------|-------|------|-----------|
| Training | ✅ | ❌ | ❌ |
| Interactive chat | ❌ | ✅ | ❌ |
| Remote access | ❌ | ❌ | ✅ |
| Integration | ❌ | ❌ | ✅ |
| Speed | Slow | Fast | Fast |

---

## 💡 Tips & Tricks

### 1. Tăng quality - Thêm data

Thêm nhiều Q&A vào `data.txt`:
```
User: Diem cat nhan lop 10?
Bot: Tuy chuyen de, thong thuong tu 18-20

User: Co hoc bong khong?
Bot: Co, sinh vien can dap ung dieu kien
```

Train lại:
```bash
python train.py train
```

### 2. Tăng tốc độ - Optimize

Sửa trong `train.py`:
```python
num_train_epochs=3        # Giảm từ 5
per_device_train_batch_size=2  # Tăng nếu có RAM
```

### 3. Production deployment

Dùng Gunicorn:
```bash
pip install gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 server:app
```

---

## ❓ FAQ

**Q: Có phải train mỗi lần chạy?**
A: Không! Chỉ train 1 lần. Lần sau load model có sẵn (nhanh hơn).

**Q: Có thể thay đổi data mà không train lại?**
A: Không. Nếu thay data, phải train lại `python train.py train`.

**Q: Model size bao nhiêu?**
A: ~500MB (pytorch_model.bin).

**Q: Chạy offline được không?**
A: Được! Không cần internet sau khi download GPT2.

**Q: Làm sao tích hợp vào app?**
A: Chạy `python server.py`, gọi API từ app.

**Q: Tại sao response chậm lần đầu?**
A: Model đang load. Lần sau nhanh hơn.

---

## 📚 Tài liệu tham khảo

- [Hugging Face Transformers](https://huggingface.co/transformers/)
- [PyTorch](https://pytorch.org/)
- [Flask](https://flask.palletsprojects.com/)
- [GPT2 Paper](https://openai.com/research/language-models-are-unsupervised-multitask-learners)

---

## 📝 Requirements

```
torch==2.0.0
transformers==4.30.0
datasets==2.13.0
flask==2.3.0
flask-cors==4.0.0
requests==2.31.0
```

---

## 📄 License

MIT License - Free to use and modify

---

## 👨‍💻 Author

DragonFlyBot Team

---

## 🤝 Support

Nếu có vấn đề, vui lòng:
1. Kiểm tra mục [Xử lý sự cố](#xử-lý-sự-cố)
2. Xem [FAQ](#-faq)
3. Check file `data.txt` có đúng format không
4. Đảm bảo Python version >= 3.8

---

**Happy chatting! 🐉✨**
