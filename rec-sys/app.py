
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional, Union
import os
import torch

from rl_agent.config import params
from build_actor import build_sasrec_actor
from inference import recommend_topk_from_npy

class RecRequest(BaseModel):
    user_history: List[Union[int, str]] = Field(..., description="Recent item IDs, most-recent last")
    candidate_ids: Optional[List[Union[int, str]]] = Field(
        None, description="Shortlist to rank; if omitted, rank full catalog except history"
    )
    k: int = Field(10, ge=1, description="Top-K to return")

@asynccontextmanager
async def lifespan(app: FastAPI):
    torch.set_grad_enabled(False)
    device = params["device"]
    model_dir = os.path.join(os.getcwd(), "output/mdp/agent")
    actor, item_meta, item_ids, id2idx, idx2id, max_len = build_sasrec_actor(params, model_dir, device)
    app.state.device = device
    app.state.actor = actor
    app.state.item_meta = item_meta
    app.state.id2idx = id2idx
    app.state.idx2id = idx2id
    app.state.max_len = max_len
    print("[API] SASRec actor loaded.")
    yield
    # shutdown
    if "cuda" in str(device):
        torch.cuda.empty_cache()

app = FastAPI(title="Music RecSys API", version="1.0", lifespan=lifespan)

@app.get("/")
def health():
    return {"status": "ok"}

@app.post("/recommend")
def recommend(req: RecRequest):
    try:
        topk = recommend_topk_from_npy(
            actor=app.state.actor,
            item_meta=app.state.item_meta,
            id2idx=app.state.id2idx,
            idx2id=app.state.idx2id,
            max_seq_len=app.state.max_len,
            user_history_ext_ids=req.user_history,
            candidate_ext_ids=req.candidate_ids,     # <--- pass it through
            K=req.k,
            device=app.state.device,
        )
    except KeyError as e:
        raise HTTPException(status_code=400, detail=f"Unknown item id: {e}")
    return {"topk": topk}
