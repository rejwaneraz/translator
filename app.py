from flask import Flask, request, jsonify
import pdfplumber
import requests
import os
import re
import tempfile

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024

# ============================================
# HTML TEMPLATE
# ============================================

HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="bn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>📖 PDF Reader</title>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+Bengali:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body {
            font-family: 'Noto Sans Bengali', sans-serif;
            background: #0d1117;
            color: #e6edf3;
            min-height: 100vh;
        }
        
        .upload-screen {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 20px;
            text-align: center;
        }
        
        .upload-screen.hidden { display: none; }
        
        .logo { font-size: 5rem; margin-bottom: 20px; animation: bounce 2s infinite; }
        
        @keyframes bounce {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-10px); }
        }
        
        .title { font-size: 2rem; font-weight: 700; color: #58a6ff; margin-bottom: 10px; }
        .subtitle { color: #8b949e; margin-bottom: 40px; }
        
        .upload-box {
            background: #161b22;
            border: 2px dashed #30363d;
            border-radius: 20px;
            padding: 50px 30px;
            width: 100%;
            max-width: 350px;
        }
        
        .upload-icon { font-size: 3rem; margin-bottom: 20px; }
        
        .upload-btn {
            background: linear-gradient(135deg, #238636, #2ea043);
            color: white;
            border: none;
            padding: 16px 40px;
            border-radius: 12px;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
        }
        
        .upload-btn:active { transform: scale(0.98); }
        
        #fileInput { display: none; }
        .file-name { color: #58a6ff; margin-top: 20px; }
        
        .reader-screen { display: none; flex-direction: column; min-height: 100vh; }
        .reader-screen.active { display: flex; }
        
        .header {
            background: #161b22;
            padding: 14px 20px;
            display: flex;
            align-items: center;
            gap: 15px;
            position: sticky;
            top: 0;
            z-index: 100;
            border-bottom: 1px solid #30363d;
        }
        
        .back-btn { background: none; border: none; color: #e6edf3; font-size: 1.5rem; cursor: pointer; }
        .header-title { flex: 1; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .header-btn { background: none; border: none; color: #8b949e; font-size: 1.3rem; cursor: pointer; }
        
        .status-bar {
            background: #1c2128;
            padding: 12px 20px;
            display: none;
            align-items: center;
            gap: 12px;
            font-size: 0.9rem;
            color: #8b949e;
        }
        .status-bar.show { display: flex; }
        
        .spinner {
            width: 18px; height: 18px;
            border: 2px solid #30363d;
            border-top-color: #58a6ff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        
        .status-text { flex: 1; }
        .status-percent { color: #58a6ff; font-weight: 600; }
        
        .content { flex: 1; padding: 25px 20px; padding-bottom: 100px; overflow-y: auto; }
        
        .line { margin-bottom: 18px; animation: fadeIn 0.4s ease; line-height: 2; }
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        
        .line-text { font-size: 1.15rem; display: block; text-align: left; }
        
        .line.title .line-text {
            font-size: 1.6rem;
            font-weight: 700;
            color: #f0883e;
            text-align: center;
            margin: 25px 0;
            padding: 15px;
            background: rgba(240, 136, 62, 0.1);
            border-radius: 12px;
        }
        
        .line.dialogue .line-text {
            background: rgba(56, 139, 253, 0.1);
            padding: 14px 18px;
            border-left: 4px solid #58a6ff;
            border-radius: 0 12px 12px 0;
            color: #a5d6ff;
        }
        
        .line.character .line-text {
            background: rgba(46, 160, 67, 0.12);
            padding: 14px 18px;
            border-left: 4px solid #2ea043;
            border-radius: 0 12px 12px 0;
        }
        
        .char-name { color: #3fb950; font-weight: 700; }
        .line.normal .line-text { color: #e6edf3; }
        .line.loading .line-text { color: #6e7681; font-style: italic; }
        
        .bottom-bar {
            position: fixed;
            bottom: 0; left: 0; right: 0;
            background: #161b22;
            padding: 14px 20px;
            display: flex;
            align-items: center;
            gap: 15px;
            border-top: 1px solid #30363d;
        }
        
        .prog-text { color: #8b949e; font-size: 0.9rem; min-width: 45px; }
        .prog-bar { flex: 1; height: 6px; background: #30363d; border-radius: 3px; overflow: hidden; }
        .prog-fill { height: 100%; background: linear-gradient(90deg, #238636, #58a6ff); width: 0%; transition: width 0.3s; }
        
        .font-btns { display: flex; gap: 8px; }
        .font-btn {
            background: #21262d;
            border: 1px solid #30363d;
            color: #e6edf3;
            width: 36px; height: 36px;
            border-radius: 8px;
            cursor: pointer;
        }
        
        .toast {
            position: fixed;
            bottom: 80px; left: 50%;
            transform: translateX(-50%) translateY(100px);
            background: #238636;
            color: white;
            padding: 12px 25px;
            border-radius: 25px;
            font-size: 0.95rem;
            transition: transform 0.3s;
            z-index: 200;
        }
        .toast.show { transform: translateX(-50%) translateY(0); }
        .toast.error { background: #da3633; }
        
        .loading-overlay {
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(13, 17, 23, 0.95);
            display: none;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 300;
        }
        .loading-overlay.show { display: flex; }
        
        .big-spinner {
            width: 60px; height: 60px;
            border: 4px solid #30363d;
            border-top-color: #58a6ff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-bottom: 20px;
        }
        .loading-msg { color: #8b949e; font-size: 1.1rem; }
        
        body.light { background: #fff; color: #24292f; }
        body.light .header, body.light .bottom-bar { background: #f6f8fa; border-color: #d0d7de; }
        body.light .line.normal .line-text { color: #24292f; }
        
        body.sepia { background: #f4ecd8; color: #5c4b37; }
        body.sepia .header, body.sepia .bottom-bar { background: #e8dcc8; }
    </style>
</head>
<body>
    <div class="upload-screen" id="uploadScreen">
        <div class="logo">📖</div>
        <h1 class="title">PDF Reader</h1>
        <p class="subtitle">ইংরেজি PDF বাংলায় পড়ুন</p>
        <div class="upload-box">
            <div class="upload-icon">📄</div>
            <button class="upload-btn" onclick="document.getElementById('fileInput').click()">
                📁 PDF বাছুন
            </button>
            <input type="file" id="fileInput" accept=".pdf">
            <p class="file-name" id="fileName"></p>
        </div>
    </div>
    
    <div class="reader-screen" id="readerScreen">
        <header class="header">
            <button class="back-btn" onclick="goBack()">←</button>
            <span class="header-title" id="headerTitle">...</span>
            <button class="header-btn" onclick="cycleTheme()">🎨</button>
        </header>
        <div class="status-bar" id="statusBar">
            <div class="spinner"></div>
            <span class="status-text" id="statusText">অনুবাদ হচ্ছে...</span>
            <span class="status-percent" id="statusPercent">0%</span>
        </div>
        <div class="content" id="content"></div>
        <div class="bottom-bar">
            <span class="prog-text" id="progText">0%</span>
            <div class="prog-bar"><div class="prog-fill" id="progFill"></div></div>
            <div class="font-btns">
                <button class="font-btn" onclick="changeFont(-2)">A-</button>
                <button class="font-btn" onclick="changeFont(2)">A+</button>
            </div>
        </div>
    </div>
    
    <div class="loading-overlay" id="loadingOverlay">
        <div class="big-spinner"></div>
        <p class="loading-msg" id="loadingMsg">PDF পড়া হচ্ছে...</p>
    </div>
    
    <div class="toast" id="toast"></div>
    
    <script>
        let lines = [], translated = {}, fontSize = 18, theme = 'dark', fileName = '';
        
        document.getElementById('fileInput').addEventListener('change', async function(e) {
            const file = e.target.files[0];
            if (!file) return;
            if (!file.name.toLowerCase().endsWith('.pdf')) {
                showToast('❌ শুধু PDF!', true);
                return;
            }
            
            fileName = file.name.replace('.pdf', '');
            document.getElementById('fileName').textContent = '✅ ' + file.name;
            showLoading('PDF থেকে টেক্সট বের করা হচ্ছে...');
            
            const formData = new FormData();
            formData.append('pdf', file);
            
            try {
                const res = await fetch('/upload', { method: 'POST', body: formData });
                const data = await res.json();
                
                if (data.success) {
                    lines = data.lines;
                    translated = {};
                    hideLoading();
                    showReader();
                    renderLines();
                    startTranslation();
                } else {
                    hideLoading();
                    showToast('❌ ' + data.error, true);
                }
            } catch (err) {
                hideLoading();
                showToast('❌ সার্ভার সমস্যা!', true);
            }
        });
        
        function showReader() {
            document.getElementById('uploadScreen').classList.add('hidden');
            document.getElementById('readerScreen').classList.add('active');
            document.getElementById('headerTitle').textContent = fileName;
        }
        
        function goBack() {
            document.getElementById('readerScreen').classList.remove('active');
            document.getElementById('uploadScreen').classList.remove('hidden');
        }
        
        function showLoading(msg) {
            document.getElementById('loadingMsg').textContent = msg;
            document.getElementById('loadingOverlay').classList.add('show');
        }
        
        function hideLoading() {
            document.getElementById('loadingOverlay').classList.remove('show');
        }
        
        function renderLines() {
            const content = document.getElementById('content');
            content.innerHTML = '';
            lines.forEach(function(line, i) {
                const div = document.createElement('div');
                div.className = 'line ' + line.type;
                div.id = 'line-' + i;
                const span = document.createElement('span');
                span.className = 'line-text';
                span.style.fontSize = fontSize + 'px';
                if (translated[i]) {
                    span.innerHTML = formatText(translated[i], line.type);
                } else {
                    div.classList.add('loading');
                    span.textContent = '⏳ অনুবাদ হচ্ছে...';
                }
                div.appendChild(span);
                content.appendChild(div);
            });
        }
        
        function formatText(text, type) {
            if (type === 'character') {
                const m = text.match(/^([^:]+):/);
                if (m) return '<span class="char-name">' + m[1] + ':</span>' + text.substring(m[0].length);
            }
            return text;
        }
        
        function updateLine(i, text) {
            const div = document.getElementById('line-' + i);
            if (div) {
                div.classList.remove('loading');
                div.querySelector('.line-text').innerHTML = formatText(text, lines[i].type);
            }
        }
        
        async function startTranslation() {
            document.getElementById('statusBar').classList.add('show');
            for (let i = 0; i < lines.length; i++) {
                const pct = Math.round(((i+1)/lines.length)*100);
                document.getElementById('statusText').textContent = 'অনুবাদ: ' + (i+1) + '/' + lines.length;
                document.getElementById('statusPercent').textContent = pct + '%';
                
                try {
                    const res = await fetch('/translate', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({text: lines[i].text, index: i})
                    });
                    const data = await res.json();
                    if (data.success) {
                        translated[i] = data.translated;
                        updateLine(i, data.translated);
                    }
                } catch(e) {
                    translated[i] = lines[i].text;
                    updateLine(i, lines[i].text);
                }
                await new Promise(r => setTimeout(r, 250));
            }
            document.getElementById('statusBar').classList.remove('show');
            showToast('✅ অনুবাদ সম্পন্ন!');
        }
        
        document.getElementById('content').addEventListener('scroll', function() {
            const pct = Math.round((this.scrollTop/(this.scrollHeight-this.clientHeight))*100) || 0;
            document.getElementById('progText').textContent = pct + '%';
            document.getElementById('progFill').style.width = pct + '%';
        });
        
        function changeFont(d) {
            fontSize = Math.max(14, Math.min(28, fontSize+d));
            document.querySelectorAll('.line-text').forEach(el => el.style.fontSize = fontSize+'px');
        }
        
        function cycleTheme() {
            const t = ['dark','light','sepia'];
            theme = t[(t.indexOf(theme)+1)%3];
            document.body.className = theme==='dark'?'':theme;
            showToast('🎨 ' + theme);
        }
        
        function showToast(msg, isError) {
            const t = document.getElementById('toast');
            t.textContent = msg;
            t.className = 'toast' + (isError?' error':'');
            t.classList.add('show');
            setTimeout(() => t.classList.remove('show'), 2500);
        }
    </script>
</body>
</html>
'''


# ============================================
# PDF EXTRACTION
# ============================================

def extract_lines(pdf_path):
    all_lines = []
    
    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            text = page.extract_text()
            if not text:
                continue
            
            text = text.replace('"', '"').replace('"', '"')
            text = text.replace(''', "'").replace(''', "'")
            
            for line in text.split('\n'):
                line = line.strip()
                if not line:
                    continue
                
                line_type = detect_type(line)
                all_lines.append({'text': line, 'type': line_type})
    
    return smart_merge(all_lines)


def detect_type(line):
    normalized = line.replace('...', '…')
    ends_punct = bool(re.search(r'[.!?…,;:"\'\)]$', normalized))
    has_quotes = '"' in line or "'" in line
    
    if len(line) < 35 and not ends_punct and not has_quotes:
        if line.isupper():
            return 'title'
        words = line.split()
        if len(words) <= 4:
            lower = line.lower()
            if not any(w in lower for w in ['i ', 'he ', 'she ', 'the ', 'and ', 'but ', 'then ']):
                return 'title'
    
    if re.match(r'^[A-Za-z]{1,20}\s*:', line):
        return 'character'
    
    if line.startswith('"') or line.startswith("'"):
        return 'dialogue'
    
    return 'normal'


def smart_merge(lines):
    if not lines:
        return []
    
    merged = []
    buffer = None
    
    for i, line in enumerate(lines):
        text = line['text']
        line_type = line['type']
        
        if line_type in ['dialogue', 'character', 'title']:
            if buffer:
                merged.append(buffer)
                buffer = None
            merged.append(line)
            continue
        
        ends_complete = bool(re.search(r'[.!?…"\']$', text.replace('...', '…')))
        
        next_lower = False
        if i + 1 < len(lines) and lines[i+1]['type'] == 'normal':
            next_text = lines[i+1]['text']
            if next_text and next_text[0].islower():
                next_lower = True
        
        if buffer:
            if text and text[0].islower():
                buffer['text'] += ' ' + text
                if ends_complete and not next_lower:
                    merged.append(buffer)
                    buffer = None
            else:
                merged.append(buffer)
                if not ends_complete and next_lower:
                    buffer = {'text': text, 'type': 'normal'}
                else:
                    merged.append(line)
                    buffer = None
        else:
            if not ends_complete and next_lower:
                buffer = {'text': text, 'type': 'normal'}
            else:
                merged.append(line)
    
    if buffer:
        merged.append(buffer)
    
    return merged


# ============================================
# TRANSLATION
# ============================================

def translate_text(text):
    if not text or not text.strip():
        return text
    
    try:
        url = "https://translate.googleapis.com/translate_a/single"
        params = {'client': 'gtx', 'sl': 'auto', 'tl': 'bn', 'dt': 't', 'q': text}
        headers = {'User-Agent': 'Mozilla/5.0'}
        
        response = requests.get(url, params=params, headers=headers, timeout=15)
        
        if response.status_code == 200:
            result = response.json()
            parts = []
            if result and result[0]:
                for part in result[0]:
                    if part and part[0]:
                        parts.append(part[0])
            return ''.join(parts).strip() or text
        return text
    except:
        return text


# ============================================
# ROUTES
# ============================================

@app.route('/')
def home():
    return HTML_TEMPLATE


@app.route('/upload', methods=['POST'])
def upload():
    try:
        if 'pdf' not in request.files:
            return jsonify({'success': False, 'error': 'No file'})
        
        file = request.files['pdf']
        if not file.filename.lower().endswith('.pdf'):
            return jsonify({'success': False, 'error': 'PDF only'})
        
        # Save to temp file
        with tempfile.NamedTemporaryFile(delete=False, suffix='.pdf') as tmp:
            file.save(tmp.name)
            filepath = tmp.name
        
        lines = extract_lines(filepath)
        
        # Delete temp file
        try:
            os.unlink(filepath)
        except:
            pass
        
        if not lines:
            return jsonify({'success': False, 'error': 'No text found'})
        
        return jsonify({'success': True, 'lines': lines, 'count': len(lines)})
    
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})


@app.route('/translate', methods=['POST'])
def translate():
    try:
        data = request.json
        text = data.get('text', '')
        index = data.get('index', 0)
        
        translated = translate_text(text)
        return jsonify({'success': True, 'translated': translated, 'index': index})
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)})


if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port)