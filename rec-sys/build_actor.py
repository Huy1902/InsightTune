# infer_build_actor.py
import os
import torch
from rl_agent.SASRec import SASRec

class _EnvSpec:
    """Shape-only stub so SASRec can be constructed without the full env."""
    def __init__(self, n_item, item_vec_size, max_seq_len):
        self.action_space = {
            'item_id': ('nomial', n_item),
            'item_feature': ('continuous', item_vec_size, 'normal'),
        }
        self.observation_space = {
            'history': ('sequence', max_seq_len, ('continuous', item_vec_size)),
        }

def build_sasrec_actor(params, model_dir: str, device: str):
    # read shapes directly from your npy arrays & params
    item_meta = params['item_meta']   # np.ndarray, rows: [PAD, item1, item2, ...]
    item_ids  = params['item_ids']    # array/list of external ids, aligned with rows
    max_len   = params['max_seq_len']

    item_vec_size = int(item_meta.shape[1])
    n_item = int(item_meta.shape[0] - 1)  # exclude PAD row at index 0

    env_spec = _EnvSpec(n_item, item_vec_size, max_len)
    sasrec_params = {
        'sasrec_n_layer':   params['sasrec_n_layer'],
        'sasrec_d_model':   params['sasrec_d_model'],
        'sasrec_n_head':    params['sasrec_n_head'],
        'sasrec_dropout':   params['sasrec_dropout'],
        'sasrec_d_forward': params['sasrec_d_forward'],
    }

    actor = SASRec(env_spec, sasrec_params).to(device)
    actor.eval()

    # load weights
    f = os.path.join(model_dir, "mdp_model_seed26_actor")
    blob = torch.load(f, map_location=device)
    state = blob.get("state_dict", blob)
    missing, unexpected = actor.load_state_dict(state, strict=False)
    if missing or unexpected:
        print("SASRec load info -> missing:", missing, "| unexpected:", unexpected)

    # simple mapping (training used str keys)
    id2idx = {str(sid): i for i, sid in enumerate(item_ids)}
    idx2id = {i: str(sid) for i, sid in enumerate(item_ids)}
    return actor, item_meta, item_ids, id2idx, idx2id, max_len
