# Artist's Studio — Database (GitHub)

Only this folder is the live database. Do not store app code here.

```
Artists studio/
  Admin/
    Password login/credentials.json   ← admin username + password (edit anytime)
    users/
      users.json                      ← user index
      accounts/{username}.json        ← one file per user (profile + control)
      chats/{username}.json           ← per-user chat history
    sessions, audit, security, contacts, conversations, messages, ...
  Front/
    site, theme, pages, portfolio, reels, socials, policies, meta, draft
```
