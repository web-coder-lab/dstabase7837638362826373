# Div Browser APK — 60 phases + 61 build

Server: https://div-cloud.onrender.com (CORE + TUNNEL)
Edge:   https://open.divbrowser.app

## Foundation (1–8)
1. Project skeleton + Gradle + manifest + theme tokens ✅
2. DivPalette / Dimens / Typography ✅
3. Navigation graph (Splash→Home→Browser→Settings→Publish→Multi) ✅
4. DivPrefs (dark, deviceId, apiKey, domain, pane session) ✅
5. DivApi client (OkHttp) + headers X-Div-Client ✅
6. DivUrl compile (http/https/div/divs/localhost) ✅
7. EdgeShare + DivLegal constants ✅
8. Splash screen UI ✅

## Home & shell (9–14)
9. Home command bar + search ✅
10. Home quick chips (privacy/terms/local/panes) ✅
11. Settings layout + dark mode ✅
12. Settings → legal / publish rows ✅
13. MainActivity NavHost wiring ✅
14. Permissions helper soft-request ✅

## Browser single (15–28)
15. BrowserScreen shell + chrome show/hide ✅
16. Address bar + badge WEB/DIV/DIVS/LOCAL ✅
17. WebView factory + settings ✅
18. Load https/http ✅
19. Load divs:// via API HTML ✅
20. Load local div:// (localhost) ✅
21. Remote div:// placeholder / tunnel hook ✅
22. Progress bar + error HTML ✅
23. Research sheet (URL edit/copy) ✅
24. Shield sheet (site permissions UI) ✅
25. Eye FAB chrome toggle ✅
26. Share edge link ✅
27. Home/Settings from browser ✅
28. Deep link intent handling ✅

## Publish & keys (29–34)
29. Publish screen UI ✅
30. POST /v1/keys create ✅
31. Friendly errors (no raw GitHub JSON) ✅
32. Save key/domain prefs ✅
33. Copy key + share link ✅
34. Key verify endpoint optional ✅

## Multi-pane (35–48)
35. PaneMode + ModeOrb ✅
36. PaneHeader + PaneFrame focus ring ✅
37. PaneState + withUrl ✅
38. PaneWebSlot WebView ✅
39. PaneUrlDialog ✅
40. DualPane 2-way ✅
41. MultiPane 3 layout ✅
42. MultiPane 4 grid ✅
43. +/- pane count ✅
44. Focus mode maximize ✅
45. Div Pulse tunnelStatus ✅
46. Save/restore pane session ✅
47. Home chips 2/3/4 ✅
48. Swap panes ✅

## Server attach hard (49–56)
49. Health check on splash/home ✅
50. Update check dialog ✅
51. Tunnel status live in browser ✅
52. Tunnel fetch for div:// pages ✅
53. Device register ✅
54. Offline / waking server UX ✅
55. API base config BuildConfig only ✅
56. End-to-end Publish→open div:// test hooks ✅

## Polish (57–60)
57. Motion / transitions ✅
58. Empty & error states all screens ✅
59. Accessibility + contrast ✅
60. Final QA checklist ✅

## 61
61. Signed release APK (GitHub Actions / local)
