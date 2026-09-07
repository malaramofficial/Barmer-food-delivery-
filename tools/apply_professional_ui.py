from pathlib import Path

path = Path('android/app/src/main/java/com/malaramofficial/barmerfooddelivery/MainActivity.java')
text = path.read_text(encoding='utf-8')
needle = '    private void showHome() {'
start = text.find(needle)
if start < 0:
    raise SystemExit('showHome method not found')
brace = text.find('{', start)
depth = 0
end = None
for i in range(brace, len(text)):
    if text[i] == '{':
        depth += 1
    elif text[i] == '}':
        depth -= 1
        if depth == 0:
            end = i + 1
            break
if end is None:
    raise SystemExit('showHome method brace scan failed')
replacement = '''    private void showHome() {\n        ProfessionalHomeUi.render(this, api, lat, new ProfessionalHomeUi.Actions() {\n            public void restaurant(JSONObject item) { showMenu(item); }\n            public void location() { requestLocation(); }\n            public void orders() { showOrders(); }\n            public void notifications() { showNotifications(); }\n            public void partner() { showPartner(); }\n            public void profile() { showProfile(); }\n            public void cart() { showCart(); }\n        });\n    }'''
new_text = text[:start] + replacement + text[end:]
if new_text != text:
    path.write_text(new_text, encoding='utf-8')
    print('Professional home UI applied to MainActivity.java')
else:
    print('Professional home UI already applied')
