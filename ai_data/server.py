from flask import Flask, request, jsonify
from flask_cors import CORS
from train import bot

app = Flask(__name__)
CORS(app)

# Load model khi server start
print("Loading bot...")
bot.load_model()
print("Bot ready!")

@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "status": "running",
        "message": "DragonFlyBot API dang chay",
        "version": "2.0",
        "qa_count": len(bot.knowledge_base)
    })

@app.route("/chat", methods=["POST"])
def chat():
    """Main chat endpoint"""
    try:
        data = request.get_json()
        user_message = data.get("message", "").strip()
        
        if not user_message:
            return jsonify({
                "success": False,
                "reply": "May chua noi gi het"
            }), 400
        
        print(f"User: {user_message}")
        
        # Get answer from bot brain
        answer, method = bot.chat(user_message)
        
        print(f"Bot: {answer} [{method}]")
        
        return jsonify({
            "success": True,
            "user_message": user_message,
            "reply": answer,
            "method": method
        }), 200
    
    except Exception as e:
        print(f"Error: {e}")
        return jsonify({
            "success": False,
            "reply": "Co loi xay ra"
        }), 500

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "healthy",
        "knowledge_base_size": len(bot.knowledge_base)
    }), 200

@app.route("/info", methods=["GET"])
def info():
    return jsonify({
        "name": "DragonFlyBot",
        "version": "2.0",
        "qa_count": len(bot.knowledge_base),
        "language": "Vietnamese"
    }), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)