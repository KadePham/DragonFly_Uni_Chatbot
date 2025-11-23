from transformers import pipeline, AutoTokenizer, AutoModelForCausalLM
import torch

tokenizer = AutoTokenizer.from_pretrained("NlpHUST/gpt2-vietnamese")
model = AutoModelForCausalLM.from_pretrained("NlpHUST/gpt2-vietnamese")

# Load knowledge base
knowledge_base = {}
with open("data.txt", "r", encoding="utf-8") as f:
    lines = [l.strip() for l in f.readlines() if l.strip()]
    for i in range(0, len(lines), 2):
        if i+1 < len(lines):
            q = lines[i].replace("User:", "").strip()
            a = lines[i+1].replace("Bot:", "").strip()
            knowledge_base[q] = a

# Tạo context từ knowledge base
context = "\n".join([f"Q: {q}\nA: {a}" for q, a in list(knowledge_base.items())[:10]])

def generate_answer(user_input):
    prompt = f"""Bạn là DragonFlyBot, chatbot hỗ trợ sinh viên Đại học Duy Tân.

Kiến thức:
{context}

Câu hỏi: {user_input}
Trả lời:"""
    
    inputs = tokenizer(prompt, return_tensors="pt")
    outputs = model.generate(
        inputs["input_ids"],
        max_length=200,
        temperature=0.7,
        top_p=0.9,
        do_sample=True,
        pad_token_id=tokenizer.eos_token_id
    )
    
    reply = tokenizer.decode(outputs[0], skip_special_tokens=True)
    return reply.split("Trả lời:")[-1].strip()

# Test
print(generate_answer("Trường Duy Tân ở đâu?"))