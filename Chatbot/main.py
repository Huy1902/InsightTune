from typing import Optional
import json
from fastapi import FastAPI, UploadFile
from pydantic import BaseModel
from AgentsBasic import get_response
from enum import Enum
from fastapi import FastAPI, UploadFile
from faster_whisper import WhisperModel
import tempfile, os
app = FastAPI()
import os
model = WhisperModel("base", device="cpu")


class ResponseType(str, Enum):
    reply = "reply"
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




@app.post("/voice-to-text")
async def voice_to_text(file: UploadFile):
    try:
        # Lưu file tạm
        with tempfile.NamedTemporaryFile(delete=False, suffix=".m4a") as temp_audio:
            temp_audio.write(await file.read())
            temp_audio_path = temp_audio.name

        print(f"Nhận file: {temp_audio_path} ({os.path.getsize(temp_audio_path)} bytes)")

        segments, info = model.transcribe(temp_audio_path, language="vi")
        text = " ".join([segment.text for segment in segments])
        os.remove(temp_audio_path)
        print(f"Kết quả: {text}")
        return {"text": text}
    except Exception as e:
        print("Lỗi xử lý voice:", e)
        return {"error": str(e)}