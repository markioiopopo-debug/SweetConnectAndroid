<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SweetConnect - แอปคู่รัก แชท & GPS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Prompt:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        body { font-family: 'Prompt', sans-serif; background-color: #0f172a; color: #f8fafc; }
        #map { height: 260px; width: 100%; border-radius: 0.75rem; z-index: 10; }
    </style>
</head>
<body class="h-screen w-screen overflow-hidden flex flex-col bg-slate-950 text-slate-100">

    <!-- HEADER -->
    <header class="bg-slate-900 border-b border-slate-800 p-3 flex justify-between items-center">
        <div class="flex items-center space-x-2">
            <div class="w-8 h-8 rounded-full bg-pink-500/20 border border-pink-500 flex items-center justify-center">
                <i class="fa-solid fa-heart text-pink-500 animate-pulse text-sm"></i>
            </div>
            <div>
                <h1 class="font-bold text-sm text-pink-400">SweetConnect</h1>
                <p class="text-[10px] text-slate-400">พื้นที่ส่วนตัวของคุณและแฟน</p>
            </div>
        </div>
        <button onclick="requestNotification()" class="bg-slate-800 text-pink-400 border border-pink-500/30 px-3 py-1 rounded-full text-xs">
            <i class="fa-solid fa-bell"></i> เปิดแจ้งเตือน
        </button>
    </header>

    <!-- NAV TABS -->
    <nav class="flex border-b border-slate-800 bg-slate-900 text-xs font-medium">
        <button onclick="switchTab('chat')" id="tab-btn-chat" class="flex-1 py-2.5 text-center text-pink-400 border-b-2 border-pink-500 bg-slate-800/40">
            <i class="fa-solid fa-comments mr-1"></i> แชทคู่รัก
        </button>
        <button onclick="switchTab('gps')" id="tab-btn-gps" class="flex-1 py-2.5 text-center text-slate-400">
            <i class="fa-solid fa-location-dot mr-1"></i> ตำแหน่ง GPS
        </button>
    </nav>

    <!-- MAIN CONTENT -->
    <main class="flex-1 overflow-y-auto p-3 relative">
        <!-- CHAT TAB -->
        <div id="tab-chat" class="h-full flex flex-col space-y-3">
            <div id="chat-box" class="flex-1 bg-slate-900 border border-slate-800 rounded-xl p-3 overflow-y-auto space-y-2 text-xs min-h-[240px]">
                <div class="flex justify-center my-1">
                    <span class="bg-slate-800 text-slate-400 text-[10px] px-2.5 py-0.5 rounded-full">ห้องแชทส่วนตัวสองคน 💖</span>
                </div>
                <div class="flex flex-col items-start space-y-0.5">
                    <span class="text-[10px] text-pink-400 ml-1">แฟนของคุณ ❤️</span>
                    <div class="bg-slate-800 text-slate-200 p-2.5 rounded-2xl rounded-tl-none border border-slate-700 max-w-[80%]">
                        คิดถึงจังเลยครับ ทำอะไรอยู่เหรอ?
                    </div>
                </div>
            </div>

            <button onclick="sendLoveAlert()" class="bg-gradient-to-r from-pink-600 to-rose-600 text-white py-2 rounded-xl text-xs font-bold">
                <i class="fa-solid fa-heart-pulse"></i> ส่งความคิดถึงให้แฟน (Send Love Alert)
            </button>

            <div class="flex gap-2">
                <input type="text" id="chat-input" placeholder="พิมพ์ข้อความหาแฟน..." class="flex-1 bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-pink-500">
                <button onclick="sendMessage()" class="bg-pink-600 text-white px-4 py-2 rounded-xl text-xs font-bold">ส่ง</button>
            </div>
        </div>

        <!-- GPS TAB -->
        <div id="tab-gps" class="hidden space-y-3">
            <div class="bg-slate-900 border border-slate-800 p-3 rounded-xl flex justify-between items-center text-xs">
                <div>
                    <div class="font-bold text-slate-200">ตำแหน่ง GPS เรียลไทม์</div>
                    <div class="text-[10px] text-slate-400" id="gps-status">กดปุ่มอัปเดตเพื่อดึงพิกัด...</div>
                </div>
                <button onclick="updateGPS()" class="bg-emerald-600 text-white px-3 py-1.5 rounded-lg text-xs font-bold">
                    <i class="fa-solid fa-rotate-right"></i> อัปเดตพิกัด
                </button>
            </div>
            <div id="map" class="border border-slate-800 shadow-xl"></div>
        </div>
    </main>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        let map = null, userMarker = null;

        function requestNotification() {
            if ("Notification" in window) {
                Notification.requestPermission().then(p => {
                    if (p === "granted") new Notification("SweetConnect 💖", { body: "เปิดระบบแจ้งเตือนเรียบร้อยแล้ว" });
                });
            }
        }

        function switchTab(t) {
            document.getElementById('tab-chat').classList.toggle('hidden', t !== 'chat');
            document.getElementById('tab-gps').classList.toggle('hidden', t !== 'gps');
            if (t === 'gps') initMap();
        }

        function sendMessage() {
            const input = document.getElementById('chat-input');
            if (!input.value.trim()) return;
            const box = document.getElementById('chat-box');
            const div = document.createElement('div');
            div.className = "flex flex-col items-end space-y-0.5";
            div.innerHTML = `<span class="text-[10px] text-slate-400 mr-1">คุณ</span><div class="bg-pink-600 text-white p-2.5 rounded-2xl rounded-tr-none max-w-[80%]">${input.value}</div>`;
            box.appendChild(div); box.scrollTop = box.scrollHeight;
            input.value = '';
        }

        function sendLoveAlert() {
            const box = document.getElementById('chat-box');
            const div = document.createElement('div');
            div.className = "flex justify-center my-1";
            div.innerHTML = `<div class="bg-pink-900/60 border border-pink-500/50 text-pink-300 text-xs px-3 py-1 rounded-full">คุณส่งหัวใจให้แฟนแล้ว! ❤️</div>`;
            box.appendChild(div); box.scrollTop = box.scrollHeight;
        }

        function initMap() {
            if (!map) {
                map = L.map('map').setView([13.7563, 100.5018], 13);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
            }
            updateGPS();
        }

        function updateGPS() {
            if ("geolocation" in navigator) {
                navigator.geolocation.getCurrentPosition(pos => {
                    const { latitude: lat, longitude: lng } = pos.coords;
                    document.getElementById('gps-status').textContent = "อัปเดตพิกัดสำเร็จ";
                    if (map) {
                        map.setView([lat, lng], 15);
                        if (userMarker) map.removeLayer(userMarker);
                        userMarker = L.marker([lat, lng]).addTo(map).bindPopup("ตำแหน่งของคุณ 📍").openPopup();
                    }
                });
            }
        }
    </script>
</body>
</html>
