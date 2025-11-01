from fastapi import FastAPI
from pydantic import BaseModel
from AgentsBasic import get_response

app = FastAPI()

class ChatRequest(BaseModel):
    message: str
    thread_id: str = "default"

class ChatResponse(BaseModel):
    response: str

@app.post("/chat")
def chat(req: ChatRequest) -> ChatResponse:
    reply = get_response(req.message, req.thread_id)
    return ChatResponse(response=reply)