from typing import Optional
import json
from fastapi import FastAPI
from pydantic import BaseModel
from AgentsBasic import get_response
from enum import Enum
app = FastAPI()

class ResponseType(str, Enum):
    reply = "reply",
    playMusic = "playMusic"

class ChatRequest(BaseModel):
    message: str
    thread_id: str = "default"

class ChatResponse(BaseModel):
    type: ResponseType
    response: Optional[str] = None
    song_name: Optional[str] = None

@app.post("/chat")
def chat(req: ChatRequest) -> ChatResponse:
    result = get_response(req.message, req.thread_id)
    if not result:
        return ChatResponse(type=ResponseType.reply, response="Không nhận được phản hồi từ chatbot.")

        # Nếu result là string, parse lại
    if isinstance(result, str):
        try:
            result = json.loads(result)
        except:
            result = {"type": "reply", "reply": result}

        # Trả về theo đúng định dạng
    if result.get("type") == "reply":
        return ChatResponse(type=ResponseType.reply, response=result.get("reply", ""))
    elif result.get("type") == "playMusic":
        return ChatResponse(type=ResponseType.playMusic, song_name=result.get("song_name", ""))
    else:
        return ChatResponse(type=ResponseType.reply, response="Phản hồi không xác định.")