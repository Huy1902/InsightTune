# infer_run.py
import os, torch
from build_actor import build_sasrec_actor
from inference import recommend_topk_from_npy

def main():
    # reuse the SAME params dict you used for training
    from rl_agent.config import params  # or the dict you pasted earlier

    device = params['device']
    model_dir = os.path.join(os.getcwd(), "output/mdp/agent")

    actor, item_meta, item_ids, id2idx, idx2id, max_len = build_sasrec_actor(params, model_dir, device)

    # EXAMPLE: a user's recent external item ids (strings or ints are fine)
    user_history_ext = ["6I9VzXrHxO9rA9A5euc8Ak", "1AWQoqb9bSvzTjaLralEkT", "7H6ev70Weq6DdpZyyTmUXk", "2PpruBYCo4H7WOBJ7Q2EwM"]


    topk = recommend_topk_from_npy(
        actor,
        item_meta=item_meta,
        id2idx=id2idx,
        idx2id=idx2id,
        max_seq_len=max_len,
        user_history_ext_ids=user_history_ext,
        K=10,
        device=device
    )
    print("Top-K:", topk)

if __name__ == "__main__":
    torch.set_grad_enabled(False)
    main()
