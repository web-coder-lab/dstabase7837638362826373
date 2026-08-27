## Schema (studio.json)

- users: id, username, name, password_hash, role, status
- site, theme, socials, pages, policies
- portfolio[], reels[] — media paths under /media/public on API host
- conversations, messages, media (private chat attachments)
- contacts, calls, versions
- reel_likes, reel_comments, reel_saves

Passwords are never stored in plaintext.
