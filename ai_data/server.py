from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
import os

app = Flask(__name__)
CORS(app)

# Load model va tokenizer
print("Loading model...")
tokenizer = AutoTokenizer.from_pretrained("NlpHUST/gpt2-vietnamese")
model = AutoModelForCausalLM.from_pretrained("NlpHUST/gpt2-vietnamese")
print("Model loaded successfully!")

# Load knowledge base
knowledge_base = {}
try:
    with open("data.txt", "r", encoding="utf-8") as f:
        lines = [l.strip() for l in f.readlines() if l.strip()]
        for i in range(0, len(lines), 2):
            if i+1 < len(lines):
                q = lines[i].replace("User:", "").strip()
                a = lines[i+1].replace("Bot:", "").strip()
                if q and a:
                    knowledge_base[q] = a
    print(f"Loaded {len(knowledge_base)} Q&A pairs")
except FileNotFoundError:
    print("ERROR: data.txt not found!")
    knowledge_base = {}

# Tao context tu knowledge base
context = "\n".join([f"Q: {q}\nA: {a}" for q, a in list(knowledge_base.items())[:10]])

def find_best_match(user_input, threshold=0.6):
    """Tim cau tra loi tuong tu nhat trong knowledge base"""
    best_answer = None
    best_score = threshold
    
    for question, answer in knowledge_base.items():
        # So sanh don gian: neu user_input co trong question
        if user_input.lower() in question.lower() or question.lower() in user_input.lower():
            return answer, "retrieval"
    
    return None, "not_found"

def generate_answer(user_input):
    """Sinh cau tra loi bang model"""
    try:
        prompt = f"""Ban la DragonFlyBot, chatbot ho tro sinh vien Dai hoc Duy Tan.
Tra loi ngan gon, than thien, vui ve bang tieng Viet.

Kien thuc:
{context}

Cau hoi: {user_input}
Tra loi:"""
        
        inputs = tokenizer(prompt, return_tensors="pt", truncation=True, max_length=512)
        
        with torch.no_grad():
            outputs = model.generate(
                inputs["input_ids"],
                max_length=200,
                temperature=0.7,
                top_p=0.9,
                do_sample=True,
                pad_token_id=tokenizer.eos_token_id
            )
        
        reply = tokenizer.decode(outputs[0], skip_special_tokens=True)
        
        # Extract answer after "Tra loi:"
        if "Tra loi:" in reply:
            answer = reply.split("Tra loi:")[-1].strip()
        else:
            answer = reply.strip()
        
        return answer if answer else None
    except Exception as e:
        print(f"Error generating: {e}")
        return None

@app.route("/", methods=["GET"])
def home():
    """Home endpoint"""
    return jsonify({
        "status": "running",
        "name": "DragonFlyBot API",
        "version": "1.0",
        "qa_count": len(knowledge_base)
    })

@app.route("/chat", methods=["POST"])
def chat():
    """Main chat endpoint"""
    try:
        data = request.get_json()
        
        if not data or "message" not in data:
            return jsonify({
                "success": False,
                "error": "Invalid request - missing 'message' field",
                "reply": "Vui long gui message"
            }), 400
        
        user_input = data.get("message", "").strip()
        
        if not user_input:
            return jsonify({
                "success": False,
                "error": "Empty message",
                "reply": "Ban chua noi gi het"
            }), 400
        
        print(f"User: {user_input}")
        
        # Step 1: Try retrieval (nhanh, chinh xac)
        answer, method = find_best_match(user_input)
        
        if answer:
            print(f"Bot: {answer} [retrieval]")
            return jsonify({
                "success": True,
                "user_message": user_input,
                "reply": answer,
                "method": "retrieval"
            }), 200
        
        # Step 2: Generate (thong minh, linh hoat)
        print("Generating answer...")
        generated = generate_answer(user_input)
        
        if generated:
            print(f"Bot: {generated[:60]}... [generated]")
            return jsonify({
                "success": True,
                "user_message": user_input,
                "reply": generated,
                "method": "generated"
            }), 200
        
        # Fallback
        return jsonify({
            "success": True,
            "user_message": user_input,
            "reply": "Toi khong biet, thu hoi cai khac xem sao",
            "method": "fallback"
        }), 200
    
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({
            "success": False,
            "error": str(e),
            "reply": "Co loi xay ra"
        }), 500

@app.route("/health", methods=["GET"])
def health():
    """Health check"""
    return jsonify({
        "status": "healthy",
        "knowledge_base_size": len(knowledge_base)
    }), 200

@app.route("/info", methods=["GET"])
def info():
    """Bot info"""
    return jsonify({
        "name": "DragonFlyBot",
        "version": "1.0",
        "description": "Chatbot ho tro sinh vien Dai hoc Duy Tan",
        "qa_count": len(knowledge_base),
        "language": "Vietnamese"
    }), 200

if __name__ == "__main__":
    print("=" * 60)
    print("DRAGONFLY BOT SERVER")
    print("=" * 60)
    app.run(host="0.0.0.0", port=5000, debug=True)