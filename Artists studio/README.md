# Artist's Studio — GitHub Database (Phase F)

PostgreSQL-style layout on GitHub (JSON tables).

```
Artists studio/
  Admin/          # private / ops tables
    users/
    sessions/
    audit/
    security/
    notifications/
    contacts/
    conversations/
    messages/
    media/
    calls/
    versions/
  Front/          # public CMS tables
    site/
    theme/
    pages/
    portfolio/
    reels/ (+ likes, comments, saves)
    socials/
    policies/
    draft/
    meta/         # seq + published_at
```

Primary driver: app `github-db.js` via GitHub Contents API.
No demo photos/reels uploaded.
