from transformers import GPT2Tokenizer, GPT2LMHeadModel, Trainer, TrainingArguments, DataCollatorForLanguageModeling
from datasets import Dataset
import torch
import os
import json

class DragonFlyBot:
    """Main bot brain - handles training and generation"""
    
    def __init__(self):
        self.model = None
        self.tokenizer = None
        self.knowledge_base = {}
        self.model_path = "./my_gpt2_model"
        self.data_file = "data.txt"
        
    def load_data(self):
        """Load Q&A from data.txt"""
        print("Loading data from data.txt...")
        
        if not os.path.exists(self.data_file):
            raise FileNotFoundError("data.txt not found!")
        
        qa_pairs = []
        with open(self.data_file, "r", encoding="utf-8") as f:
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
                                self.knowledge_base[user_line] = bot_line
                                qa_pairs.append(f"User: {user_line}\nBot: {bot_line}")
                i += 2
        
        print(f"Loaded {len(self.knowledge_base)} Q&A pairs")
        return qa_pairs
    
    def prepare_dataset(self, qa_pairs):
        """Prepare training dataset"""
        print("Preparing dataset...")
        
        # Tao dataset tu Q&A pairs
        texts = []
        for qa in qa_pairs:
            texts.append(qa)
        
        dataset = Dataset.from_dict({"text": texts})
        return dataset
    
    def tokenize_function(self, examples):
        """Tokenize text"""
        return self.tokenizer(
            examples["text"],
            truncation=True,
            padding="max_length",
            max_length=128
        )
    
    def train(self):
        """Train GPT2 model"""
        print("Starting training...")
        
        # Load data
        qa_pairs = self.load_data()
        
        # Load model va tokenizer
        print("Loading GPT2 model...")
        self.tokenizer = GPT2Tokenizer.from_pretrained("gpt2")
        self.tokenizer.pad_token = self.tokenizer.eos_token
        self.model = GPT2LMHeadModel.from_pretrained("gpt2")
        
        # Prepare dataset
        dataset = self.prepare_dataset(qa_pairs)
        tokenized_dataset = dataset.map(
            self.tokenize_function,
            batched=True,
            remove_columns=["text"]
        )
        
        # Data collator
        data_collator = DataCollatorForLanguageModeling(
            tokenizer=self.tokenizer,
            mlm=False
        )
        
        # Training arguments
        training_args = TrainingArguments(
            output_dir="./model",
            overwrite_output_dir=True,
            num_train_epochs=5,
            per_device_train_batch_size=1,
            save_steps=50,
            logging_steps=5,
            report_to="none",
            learning_rate=5e-5
        )
        
        # Trainer
        trainer = Trainer(
            model=self.model,
            args=training_args,
            data_collator=data_collator,
            train_dataset=tokenized_dataset
        )
        
        # Train
        print("Training started...")
        trainer.train()
        
        # Save model
        self.save_model()
    
    def save_model(self):
        """Save trained model"""
        print(f"Saving model to {self.model_path}...")
        self.model.save_pretrained(self.model_path)
        self.tokenizer.save_pretrained(self.model_path)
        print("Model saved successfully")
    
    def load_model(self):
        """Load trained model"""
        if not os.path.exists(self.model_path):
            print("Model not found, training new model...")
            self.train()
        else:
            print("Loading trained model...")
            self.tokenizer = GPT2Tokenizer.from_pretrained(self.model_path)
            self.model = GPT2LMHeadModel.from_pretrained(self.model_path)
            self.load_knowledge_base()
    
    def load_knowledge_base(self):
        """Load knowledge base for retrieval"""
        print("Loading knowledge base...")
        
        with open(self.data_file, "r", encoding="utf-8") as f:
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
                                self.knowledge_base[user_line] = bot_line
                i += 2
        
        print(f"Knowledge base loaded: {len(self.knowledge_base)} pairs")
    
    def get_answer(self, user_input):
        """Get answer from knowledge base"""
        # Exact match or similar
        for question, answer in self.knowledge_base.items():
            if user_input.lower() in question.lower() or question.lower() in user_input.lower():
                return answer, "retrieval"
        
        return None, "not_found"
    
    def generate_answer(self, user_input):
        """Generate answer using trained model"""
        try:
            prompt = f"User: {user_input}\nBot:"
            input_ids = self.tokenizer.encode(prompt, return_tensors="pt")
            
            output = self.model.generate(
                input_ids,
                max_length=100,
                num_beams=5,
                temperature=0.7,
                top_p=0.9,
                do_sample=True
            )
            
            response = self.tokenizer.decode(output[0], skip_special_tokens=True)
            
            if "Bot:" in response:
                answer = response.split("Bot:")[-1].strip()
                return answer if answer else None
            
            return None
        except Exception as e:
            print(f"Error generating: {e}")
            return None
    
    def chat(self, user_input):
        """Main chat function"""
        # Step 1: Try retrieval
        answer, method = self.get_answer(user_input)
        if answer:
            return answer, "retrieval"
        
        # Step 2: Generate
        generated = self.generate_answer(user_input)
        if generated:
            return generated, "generated"
        
        # Fallback
        return "Toi khong biet", "fallback"

# Global bot instance
bot = DragonFlyBot()

# Command line interface
if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1:
        command = sys.argv[1]
        
        if command == "train":
            print("Training new model...")
            bot.train()
        
        elif command == "chat":
            bot.load_model()
            
            print("Chat mode - type 'quit' to exit")
            while True:
                user_input = input("You: ").strip()
                if user_input.lower() == "quit":
                    break
                
                answer, method = bot.chat(user_input)
                print(f"Bot: {answer} [{method}]")
        
        else:
            print("Usage:")
            print("  python train.py train    - Train model")
            print("  python train.py chat     - Chat mode")
    else:
        print("Usage:")
        print("  python train.py train    - Train model")
        print("  python train.py chat     - Chat mode")
