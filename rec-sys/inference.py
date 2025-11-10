
import numpy as np
import torch

def pad_left(seq, L, pad_val=0):
    seq = list(seq)[-L:]
    return [pad_val] * (L - len(seq)) + seq

@torch.no_grad()
def recommend_topk_from_npy(
        actor,
        item_meta: np.ndarray,     # (N+1, d_item), row 0 = PAD
        id2idx: dict,              # str(external_id) -> internal index
        idx2id: dict,              # internal index -> external id
        max_seq_len: int,
        user_history_ext_ids,      # list of external ids (same space as params['item_ids'])
        candidate_ext_ids=None,    # <---- NEW: optional candidate pool from API
        K: int = 10,
        device: str = "cuda",
):
    # 1) map history to internal indices
    H_int = [id2idx[str(x)] for x in user_history_ext_ids]
    H_int = pad_left(H_int, max_seq_len, pad_val=0)

    # 2) build candidate pool
    if candidate_ext_ids is None:
        # rank the whole catalog (1..N), excluding history
        all_internal = np.arange(1, item_meta.shape[0], dtype=np.int64)
        exclude = set(H_int)
        C_int = np.array([i for i in all_internal if i not in exclude], dtype=np.int64)
    else:
        # map each candidate external id -> internal index
        try:
            C_int = np.array([id2idx[str(x)] for x in candidate_ext_ids], dtype=np.int64)
        except KeyError as e:
            # let caller handle
            raise

    # 3) get features
    hist_feat = item_meta[np.asarray(H_int, dtype=np.int64)].astype(np.float32)  # (H, d_item)
    cand_feat = item_meta[C_int].astype(np.float32)                              # (L, d_item)

    # 4) tensors
    hist_t = torch.tensor(hist_feat, device=device).unsqueeze(0)  # (1, H, d_item)
    cand_t = torch.tensor(cand_feat, device=device).unsqueeze(0)  # (1, L, d_item)

    # 5) forward
    out = actor({'history_features': hist_t})
    action_emb = out['action_emb']                             # (1, d_model)
    logits = actor.score(action_emb, cand_t, do_softmax=False).squeeze(0)  # (L,)

    # 6) top-k
    k = min(K, logits.numel())
    topk_idx = torch.topk(logits, k=k, dim=-1).indices.cpu().numpy()
    topk_internal = C_int[topk_idx]

    # 7) map back to external ids
    topk_result = [idx2id[int(x)] for x in topk_internal]
    return topk_result
