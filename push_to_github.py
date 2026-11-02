import base64, json, os, time, urllib.request, urllib.error, urllib.parse, zipfile

ZIP_PATH = "moo_lan_tv.zip"

OWNER = "YOUR_GITHUB_USERNAME"
REPO = "moo_lan_tv"
TOKEN = "YOUR_PERSONAL_ACCESS_TOKEN"

API = f"https://api.github.com/repos/{OWNER}/{REPO}/contents/"
HEADERS = {
    "Authorization": "Bearer " + TOKEN,
    "Accept": "application/vnd.github+json",
    "User-Agent": "push-script",
}

def call(method, url, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            return json.loads(r.read().decode() or "{}")
    except urllib.error.HTTPError as e:
        if e.code == 404 and method == "GET":
            return None
        raise RuntimeError("%s -> %s %s" % (method, e.code, e.read().decode()[:300]))

if not os.path.exists(ZIP_PATH):
    raise SystemExit("مش لاقي الملف %s" % ZIP_PATH)

z = zipfile.ZipFile(ZIP_PATH)
names = [n for n in z.namelist() if not n.endswith("/")]
print("عدد الملفات:", len(names))

for i, name in enumerate(names, 1):
    url = API + urllib.parse.quote(name)
    body = {
        "message": "add " + name,
        "content": base64.b64encode(z.read(name)).decode(),
    }
    existing = call("GET", url)
    if existing and "sha" in existing:
        body["sha"] = existing["sha"]
    for attempt in range(3):
        try:
            call("PUT", url, body)
            break
        except RuntimeError as e:
            if attempt == 2:
                raise
            time.sleep(2)
    print("[%d/%d] %s" % (i, len(names), name))
    time.sleep(0.4)

print("تم الرفع بنجاح! افتح تبويب Actions في GitHub وانتظر بناء ملفين الـ APK.")
