from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModel
import torch
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Enable cross-origin requests for mobile apps

# Load Vietnamese model for embeddings
tokenizer = AutoTokenizer.from_pretrained("NlpHUST/gpt2-vietnamese")
tokenizer.pad_token = tokenizer.eos_token
model = AutoModel.from_pretrained("NlpHUST/gpt2-vietnamese")

# Load knowledge base from data.txt
knowledge_base = {}
with open("data.txt", "r", encoding="utf-8") as f:
    lines = f.readlines()
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        if line.startswith("User:"):
            user_line = line.replace("User:", "").strip()
            if i + 1 < len(lines):
                next_line = lines[i + 1].strip()
                if next_line.startswith("Bot:"):
                    bot_line = next_line.replace("Bot:", "").strip()
                    if user_line and bot_line:
                        knowledge_base[user_line] = bot_line
            i += 2
        else:
            i += 1

print(f"✅ Server loaded {len(knowledge_base)} Q&A pairs")

def get_embedding(text):
    """Get text embedding"""
    if not text or not text.strip():
        return torch.zeros(768).unsqueeze(0)
    
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True, max_length=128)
    with torch.no_grad():
        outputs = model(**inputs)
    return outputs.last_hidden_state.mean(dim=1)

def find_best_match(user_input, threshold=0.70):
    """Find best matching answer"""
    if not knowledge_base:
        return None
    
    user_embedding = get_embedding(user_input)
    best_match = None
    best_score = threshold
    best_question = None
    
    for question, answer in knowledge_base.items():
        q_embedding = get_embedding(question)
        similarity = torch.nn.functional.cosine_similarity(user_embedding, q_embedding).item()
        
        if similarity > best_score:
            best_score = similarity
            best_match = answer
            best_question = question
    
    return best_match

@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "status": "running",
        "message": "🔥 DragonFlyBot API đang chạy ngon 😎",
        "version": "1.0"
    })

@app.route("/chat", methods=["POST"])
def chat():
    """Main chat endpoint for mobile apps"""
    try:
        data = request.get_json()
        user_message = data.get("message", "").strip()
        
        if not user_message:
            return jsonify({
                "success": False,
                "error": "Message cannot be empty",
                "reply": "Mày chưa nói gì hết ku 😅"
            }), 400
        
        answer = find_best_match(user_message)
        
        return jsonify({
            "success": True,
            "user_message": user_message,
            "reply": answer if answer else "Tôi không biết 🤔",
            "found": answer is not None
        }), 200
    
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e),
            "reply": "Có lỗi xảy ra, thử lại sau nha 😅"
        }), 500

@app.route("/health", methods=["GET"])
def health():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "knowledge_base_size": len(knowledge_base)
    }), 200

@app.route("/info", methods=["GET"])
def info():
    """Get bot info"""
    return jsonify({
        "name": "DragonFlyBot",
        "version": "1.0",
        "description": "Chatbot hỗ trợ sinh viên Đại học Duy Tân",
        "qa_count": len(knowledge_base),
        "language": "Vietnamese"
    }), 200

if __name__ == "__main__":
    # For development
    app.run(host="0.0.0.0", port=5000, debug=True)
    
    # For production, use gunicorn:
    # gunicorn -w 4 -b 0.0.0.0:5000 server:app
