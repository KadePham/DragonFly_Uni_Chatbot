# DragonFlyBot - Chatbot Hỗ trợ Sinh viên Đại học Duy Tân

## 1. Giới thiệu

**DragonFlyBot** là chatbot thông minh được phát triển để hỗ trợ sinh viên Đại học Duy Tân. Bot sử dụng Fine-tuned GPT2 model kết hợp với Retrieval system tùy chỉnh để cung cấp câu trả lời chính xác và linh hoạt.

### Tính năng
- ✓ Retrieval: Tìm kiếm câu trả lời từ knowledge base
- ✓ Generation: Sinh ra câu trả lời thông minh bằng model GPT2 đã train
- ✓ Hybrid approach: Kết hợp cả 2 phương pháp
- ✓ API Server: Dễ dàng tích hợp với ứng dụng khác
- ✓ CLI Interface: Chat trực tiếp từ terminal

---

## 2. Yêu cầu hệ thống

### Môi trường
- **Python**: 3.8 trở lên
- **RAM**: 4GB tối thiểu (8GB recommended)
- **Disk**: 2GB cho model
- **OS**: Windows, macOS, Linux

### Kiểm tra Python version
```bash
python --version
```

Nếu output là `Python 3.8+`, bạn có thể tiếp tục!

---

## 3. Cài đặt

### Bước 1: Clone hoặc download project

```bash
# Nếu dùng Git
git clone <repo-url>
cd DragonFlyBot

# Hoặc download từ GitHub/ZIP rồi giải nén
cd DragonFlyBot
```

### Bước 2: Tạo virtual environment (khuyến nghị)

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# macOS / Linux
python3 -m venv venv
source venv/bin/activate
```

Bạn sẽ thấy `(venv)` ở đầu terminal.

### Bước 3: Cài đặt dependencies

```bash
pip install -r requirements.txt
```

**Nội dung `requirements.txt`:**
```
torch==2.0.0
transformers==4.30.0
datasets==2.13.0
flask==2.3.0
flask-cors==4.0.0
```

**Chờ khoảng 5-10 phút** vì PyTorch file lớn (~500MB).

### Bước 4: Chuẩn bị data

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
- Một dòng trống giữa các cặp Q&A là tùy chọn

---

## 4. Chạy project

### Option 1: Training Model (Lần đầu)

**Command:**
```bash
python train.py train
```

**Output:**
```
Training new model...
Loading data from data.txt...
Loaded 4 Q&A pairs
Preparing dataset...
Loading GPT2 model...
Training started...

[1/5 01:23, Epoch 1/5]
Step 50: loss = 4.2341

[2/5 01:45, Epoch 2/5]
Step 50: loss = 3.8923

...

Saving model to ./my_gpt2_model...
Model saved successfully
```

**Thời gian:** 5-10 phút (tùy máy)

**Output tạo ra:**
- `my_gpt2_model/` - Folder chứa model đã train
  - `pytorch_model.bin` - Model weights
  - `config.json` - Model config
  - `tokenizer.json` - Tokenizer

**Chỉ cần chạy 1 lần! Lần sau sẽ load model có sẵn.**

---

### Option 2: Chat Mode (Interactive)

**Command:**
```bash
python train.py chat
```

**Output:**
```
Loading trained model...
Loading knowledge base...
Knowledge base loaded: 4 pairs
Chat mode - type 'quit' to exit
You: 
```

**Cách sử dụng:**

Nhập câu hỏi:
```
You: Truong Duy Tan o dau?
Bot: Duy Tan nam o Da Nang, thanh pho lon o mien Trung [retrieval]

You: Co may khoa o day?
Bot: Co cac khoa nhu CNTT, Kinh te, Y Duoc, Luat va nhung khoa khac [retrieval]

You: Toi muon hoc ai?
Bot: Ban co the lien he phong tuyen sinh de hoc them chi tiet [generated]

You: quit
```

**Giải thích output:**
- `[retrieval]` - Trả lời từ knowledge base
- `[generated]` - Sinh ra bằng model
- `[fallback]` - Không biết

**Thoát chat:** Gõ `quit` rồi Enter

---

### Option 3: API Server

**Command:**
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

Server đang chạy trên `http://localhost:5000`

#### Test API - Cách 1: Dùng curl

**Request:**
```bash
curl -X POST http://localhost:5000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Truong Duy Tan o dau?"}'
```

**Response:**
```json
{
  "success": true,
  "user_message": "Truong Duy Tan o dau?",
  "reply": "Duy Tan nam o Da Nang",
  "method": "retrieval"
}
```

#### Test API - Cách 2: Dùng Postman

1. Mở Postman (hoặc download từ postman.com)
2. Chọn **POST**
3. URL: `http://localhost:5000/chat`
4. Tab **Headers**: Thêm `Content-Type: application/json`
5. Tab **Body** → **raw** → **JSON**:
```json
{
  "message": "Truong Duy Tan o dau?"
}
```
6. Click **Send**

#### Test API - Cách 3: Python script

```python
import requests
import json

url = "http://localhost:5000/chat"
headers = {"Content-Type": "application/json"}
data = {"message": "Truong Duy Tan o dau?"}

response = requests.post(url, json=data, headers=headers)
print(response.json())
```

Chạy:
```bash
python test_api.py
```

---

## 5. API Endpoints

### 1. GET `/`
Kiểm tra server

```bash
curl http://localhost:5000/
```

Response:
```json
{
  "status": "running",
  "message": "DragonFlyBot API dang chay",
  "version": "2.0",
  "qa_count": 4
}
```

### 2. POST `/chat`
Chat với bot

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
  "reply": "Toi la DragonFlyBot...",
  "method": "retrieval"
}
```

### 3. GET `/health`
Health check

```bash
curl http://localhost:5000/health
```

Response:
```json
{
  "status": "healthy",
  "knowledge_base_size": 4
}
```

### 4. GET `/info`
Bot info

```bash
curl http://localhost:5000/info
```

Response:
```json
{
  "name": "DragonFlyBot",
  "version": "2.0",
  "qa_count": 4,
  "language": "Vietnamese"
}
```

---

## 6. Workflow chi tiết

### Lần đầu tiên

```
1. python train.py train
   ↓
   - Load data.txt
   - Load GPT2 model
   - Train model với data Duy Tân
   - Lưu vào my_gpt2_model/
   ↓
   Done! Model sẵn sàng

2. python train.py chat
   ↓
   - Load my_gpt2_model/
   - Load knowledge base
   - Sẵn sàng chat
```

### Lần sau

```
1. python train.py chat
   ↓
   - Load my_gpt2_model/ (nhanh!)
   - Load knowledge base
   - Chat ngay (không train lại)

HOẶC

1. python server.py
   ↓
   - Load my_gpt2_model/ (nhanh!)
   - Load knowledge base
   - API sẵn sàng
```

---

## 7. Xử lý sự cố

### Lỗi: "data.txt not found"

**Nguyên nhân:** Chưa tạo file data.txt

**Giải pháp:**
```bash
# Tạo file data.txt cùng thư mục với train.py
echo. > data.txt

# Rồi copy nội dung Q&A vào
```

### Lỗi: "CUDA out of memory"

**Nguyên nhân:** Máy không đủ RAM

**Giải pháp:** Sửa trong `train.py`:
```python
per_device_train_batch_size=1  # Giảm xuống 1
```

### Lỗi: "ModuleNotFoundError: No module named 'transformers'"

**Nguyên nhân:** Chưa cài dependencies

**Giải pháp:**
```bash
pip install -r requirements.txt
```

### Lỗi: "Model not found after training"

**Nguyên nhân:** Ổ đĩa đầy hoặc quyền hạn

**Giải pháp:**
```bash
# Xóa model cũ
rmdir /s my_gpt2_model

# Train lại
python train.py train
```

### Lỗi: "Address already in use" (khi chạy server)

**Nguyên nhân:** Port 5000 đang dùng

**Giải pháp:** Sửa trong `server.py`:
```python
app.run(host="0.0.0.0", port=5001, debug=True)  # Thay 5001
```

---

## 8. Tối ưu performance

### 1. Tăng quality - Thêm data

Thêm nhiều Q&A vào `data.txt`:
```
User: Diem cat nhan lop 10 bao nhieu?
Bot: Tuy chuyen de, thong thuong tu 18-20 diem

User: Co hoc bong khong?
Bot: Co, sinh vien dap ung dieu kien co the dang ky

...
```

Rồi train lại:
```bash
python train.py train
```

### 2. Tăng tốc độ - Optimize model

Sửa trong `train.py`:
```python
num_train_epochs=3  # Giảm từ 5 xuống 3
per_device_train_batch_size=2  # Tăng nếu có RAM
```

### 3. Production deployment

Dùng Gunicorn thay Flask:
```bash
pip install gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 server:app
```

---

## 9. Cấu trúc thư mục

```
DragonFlyBot/
├── train.py                    # Dau nao bot
├── server.py                   # API server
├── data.txt                    # Knowledge base
├── my_gpt2_model/              # Model trained (auto tao)
│   ├── config.json
│   ├── pytorch_model.bin
│   ├── tokenizer.json
│   └── special_tokens_map.json
├── requirements.txt            # Dependencies
├── .env                        # Config (optional)
├── .gitignore
└── README.md                   # Documentation
```

---

## 10. Ví dụ chạy đầy đủ

### Scenario: Setup + Train + Chat

```bash
# 1. Tạo virtual environment
python -m venv venv
venv\Scripts\activate

# 2. Cài dependencies
pip install -r requirements.txt

# 3. Tạo data.txt với 4 Q&A

# 4. Train model (lần đầu)
python train.py train
# Output: Model saved successfully

# 5. Chat interactive
python train.py chat
# You: Truong Duy Tan o dau?
# Bot: Duy Tan nam o Da Nang [retrieval]
# You: quit

# 6. Chạy API server
python server.py
# Running on http://127.0.0.1:5000

# 7. Mở terminal mới, test API
curl -X POST http://localhost:5000/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Truong Duy Tan o dau?"}'
```

---

## 11. Q&A

**Q: Có phải train mỗi lần chạy?**
A: Không! Chỉ train 1 lần. Sau đó load model có sẵn (nhanh hơn).

**Q: Có thể thay đổi data mà không train lại?**
A: Không. Nếu thay data, phải train lại `python train.py train`.

**Q: Model size bao nhiêu?**
A: ~500MB (pytorch_model.bin).

**Q: Chạy offline được không?**
A: Được! Không cần internet sau khi download GPT2.

**Q: Làm sao để tích hợp vào app?**
A: Chạy `python server.py`, rồi gọi API từ app.

---

## 12. Tài liệu tham khảo

- [Hugging Face Transformers](https://huggingface.co/transformers/)
- [PyTorch](https://pytorch.org/)
- [Flask](https://flask.palletsprojects.com/)

---

## 13. License

MIT License - Free to use and modify
