# Security rules

1. Never store raw API keys in `_index.json` — only domain → keyHash.
2. Prefer keyHash + encrypted blob if repo is public.
3. Audit every create/revoke in `audit/audit.json`.
4. Server token: least privilege, contents write on this repo only.
5. Do not write outside `Div browser/`.
6. Artists studio + chessking folders must never be modified by Div Browser.
