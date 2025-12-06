import logging
import time
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_community.vectorstores import Chroma
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter 
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate

# --- CONFIGURATION ---
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("DragonFlyBot")

app = FastAPI(title="DragonFlyBot API", description="RAG Chatbot for DTU Admissions")

# CORS Middleware for Frontend connection
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- GLOBAL VARIABLES ---
DB_PATH = "./data.txt"
EMBEDDING_MODEL = "nomic-embed-text"
LLM_MODEL = "llama3.2"

print("--- 🚀 SYSTEM INITIALIZATION ---")

try:
    # 1. Data Ingestion
    loader = TextLoader(DB_PATH, encoding="utf-8")
    documents = loader.load()

    # 2. Text Splitting (Optimized for Context Retention)
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=800, 
        chunk_overlap=100,
        separators=["\n\n", "\n", "User:", "Bot:", " "]
    )
    texts = text_splitter.split_documents(documents)

    # 3. Vector Database & Retriever
    embeddings = OllamaEmbeddings(model=EMBEDDING_MODEL)
    db = Chroma.from_documents(texts, embeddings)
    
    # Using MMR to fetch diverse relevant documents
    retriever = db.as_retriever(
        search_type="mmr", 
        search_kwargs={"k": 4, "fetch_k": 10}
    )

    # 4. LLM & Prompt Engineering
    template = """
    Bạn là hệ thống tra cứu thông tin tự động của Đại học Duy Tân.
    Sử dụng thông tin trong phần [Context] để trả lời câu hỏi.
    
    QUY TẮC:
    1. Trả lời dựa trên thông tin thực tế trong Context.
    2. Giữ nguyên các số liệu, ngày tháng, tên riêng.
    3. Nếu không tìm thấy thông tin, trả lời: "Dữ liệu hiện tại không có thông tin này."
    4. Trả lời ngắn gọn, súc tích.
    
    Context:
    {context}

    Câu hỏi: {question}
    Trả lời:
    """
    QA_CHAIN_PROMPT = PromptTemplate.from_template(template)

    llm = ChatOllama(
        model=LLM_MODEL, 
        temperature=0.1,  # Low temperature for factual accuracy
        num_ctx=4096,     # Extended context window
        keep_alive="1h"   # Keep model loaded for performance
    )

    qa_chain = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=retriever,
        chain_type_kwargs={"prompt": QA_CHAIN_PROMPT},
        return_source_documents=False # Disable for production to save bandwidth
    )

    logger.info("✅ System Ready.")

except Exception as e:
    logger.error(f"Initialization Error: {e}")

# --- API ENDPOINTS ---

class QuestionRequest(BaseModel):
    question: str

@app.post("/chat")
async def chat_endpoint(request: QuestionRequest):
    start_time = time.time()
    logger.info(f"Incoming query: {request.question}")
    
    try:
        response = qa_chain.invoke({"query": request.question})
        process_time = round(time.time() - start_time, 2)
        logger.info(f"Processed in {process_time}s")

        return {
            "result": response["result"]
        }
        
    except Exception as e:
        logger.error(f"Processing Error: {e}")
        return {"result": "Xin lỗi, hệ thống đang gặp sự cố kỹ thuật."}