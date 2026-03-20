'use strict';

let stompClient = null;
let currentUsername = null;
let lastMessageId = null;

const typingUsers = new Set();
const typingTimeouts = {};

// ── Connect ──────────────────────────────────────────────────────────────────

function connect() {
    const usernameInput = document.getElementById('username-input');
    const username = usernameInput.value.trim();

    if (!username) {
        usernameInput.focus();
        return;
    }

    currentUsername = username;

    // зареди историята чрез HTTP GET
    // след това отвори WebSocket за новите съобщения
    loadMessageHistory().then(openWebSocket);
}

async function loadMessageHistory() {
    // Покажи чат екрана преди зареждането
    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('chat-screen').classList.remove('hidden');

    try {
        const response = await fetch('/api/messages');
        const messages = await response.json();

        if (messages.length > 0) {
            renderSeparator('— история —');
            messages.forEach(renderMessage);
            renderSeparator('— край на историята —');
        }
    } catch (err) {
        console.error('Грешка при зареждане на историята:', err);
    }
}

function openWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    requestNotificationPermission();

    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/typing', onTypingReceived);
    stompClient.subscribe('/topic/users', onUsersReceived);
    stompClient.subscribe('/topic/read', onReadReceiptReceived);

    document.getElementById('messages-list').addEventListener('scroll', function () {
        if (isAtBottom(this)) sendReadReceipt();
    });

    stompClient.send('/app/chat.addUser', {}, JSON.stringify({
        sender: currentUsername,
        content: currentUsername + ' се присъедини към чата',
        type: 'JOIN'
    }));

    sendReadReceipt();

    const messageInput = document.getElementById('message-input');
    messageInput.focus();
    messageInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            sendMessage();
        } else {
            sendTypingEvent();
        }
    });
    document.getElementById('username-input').removeEventListener('keydown', handleUsernameEnter);
}

function onError(error) {
    console.error('WebSocket грешка:', error);
    const statusDot = document.getElementById('status-indicator');
    if (statusDot) {
        statusDot.style.background = '#f44336';
        statusDot.style.textContent = "Online"
    }
}

// ── Typing ────────────────────────────────────────────────────────────────────

function sendTypingEvent() {
    if (!stompClient || !stompClient.connected) return;

    stompClient.send('/app/chat.typing', {}, JSON.stringify({
        sender: currentUsername,
        type: 'TYPING'
    }));
}

function onTypingReceived(payload) {
    const message = JSON.parse(payload.body);
    const sender = message.sender;

    if (sender === currentUsername) return; // не показвай за себе си

    typingUsers.add(sender);
    updateTypingIndicator();

    // след 2 секунди без нов typing event — скрий за този потребител
    clearTimeout(typingTimeouts[sender]);
    typingTimeouts[sender] = setTimeout(() => {
        typingUsers.delete(sender);
        updateTypingIndicator();
    }, 5000);
}

function onUsersReceived(payload) {
    const usernames = JSON.parse(payload.body);
    const list = document.getElementById('users-list');
    const countEl = document.getElementById('users-count');

    countEl.textContent = usernames.length;
    list.innerHTML = '';

    usernames.forEach(username => {
        const li = document.createElement('li');
        li.textContent = username;
        if (username === currentUsername) li.classList.add('self');
        list.appendChild(li);
    });
}

function updateTypingIndicator() {
    const indicator = document.getElementById('typing-indicator');
    if (typingUsers.size === 0) {
        indicator.classList.add('hidden');
        indicator.textContent = '';
        return;
    }

    const names = [...typingUsers].join(', ');
    indicator.textContent = typingUsers.size === 1
        ? `${names} пише в момента...`
        : `${names} пишат в момента...`;
    indicator.classList.remove('hidden');
}

// ── Send ─────────────────────────────────────────────────────────────────────

function sendMessage() {
    const input = document.getElementById('message-input');
    const content = input.value.trim();

    if (!content || !stompClient || !stompClient.connected) return;

    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
        sender: currentUsername,
        content: content,
        type: 'CHAT'
    }));

    input.value = '';
    input.focus();
}

// ── Render ────────────────────────────────────────────────────────────────────

function renderMessage(message) {
    const list = document.getElementById('messages-list');
    const li = document.createElement('li');

    if (message.type === 'CHAT') {
        const isOwn = message.sender === currentUsername;
        li.classList.add('message-item', isOwn ? 'own' : 'other');

        if (!isOwn) {
            const senderEl = document.createElement('span');
            senderEl.classList.add('message-sender');
            senderEl.textContent = message.sender;
            li.appendChild(senderEl);
        }

        const bubble = document.createElement('div');
        bubble.classList.add('message-bubble');
        bubble.textContent = message.content;
        li.appendChild(bubble);

        if (message.sentAt) {
            const timeEl = document.createElement('span');
            timeEl.classList.add('message-time');
            timeEl.textContent = formatTime(message.sentAt);
            li.appendChild(timeEl);
        }

    } else {
        li.classList.add('message-item', 'event');
        const bubble = document.createElement('div');
        bubble.classList.add('message-bubble');
        bubble.textContent = message.content;
        li.appendChild(bubble);
    }

    if (message.id && message.type === 'CHAT') {
        li.dataset.messageId = message.id;
        if (!lastMessageId || message.id > lastMessageId) {
            lastMessageId = message.id;
        }
    }

    list.appendChild(li);
    list.scrollTop = list.scrollHeight;
}

function renderSeparator(text) {
    const list = document.getElementById('messages-list');
    const li = document.createElement('li');
    li.classList.add('message-item', 'separator');
    li.textContent = text;
    list.appendChild(li);
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    renderMessage(message);

    if (message.type === 'CHAT') {
        const list = document.getElementById('messages-list');
        if (isAtBottom(list)) sendReadReceipt();

        if (message.sender !== currentUsername && Notification.permission === 'granted') {
            const n = new Notification(message.sender, {
                body: message.content,
                icon: '/favicon.ico'
            });
            n.onclick = () => { window.focus(); n.close(); };
        }
    }
}

function formatTime(sentAt) {
    // sentAt е масив от LocalDateTime: [yyyy, MM, dd, HH, mm, ss]
    // или ISO стринг — обработваме и двата случая
    if (Array.isArray(sentAt)) {
        const [, month, day, hour, minute] = sentAt;
        return `${String(day).padStart(2,'0')}.${String(month).padStart(2,'0')} ${String(hour).padStart(2,'0')}:${String(minute).padStart(2,'0')}`;
    }
    const d = new Date(sentAt);
    return `${String(d.getDate()).padStart(2,'0')}.${String(d.getMonth()+1).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}

// ── Read receipts ─────────────────────────────────────────────────────────────

function isAtBottom(el) {
    return el.scrollHeight - el.scrollTop - el.clientHeight < 50;
}

function sendReadReceipt() {
    if (!stompClient || !stompClient.connected || !lastMessageId) return;
    stompClient.send('/app/chat.read', {}, JSON.stringify({
        username: currentUsername,
        messageId: lastMessageId
    }));
}

function onReadReceiptReceived(payload) {
    const userLastRead = JSON.parse(payload.body);
    if (!lastMessageId) return;

    const readers = Object.entries(userLastRead)
        .filter(([username, msgId]) => username !== currentUsername && msgId >= lastMessageId)
        .map(([username]) => username);

    const el = document.getElementById('read-receipt-indicator');
    el.textContent = readers.length > 0 ? 'Seen by ' + readers.join(', ') : '';
}

// ── Notifications ─────────────────────────────────────────────────────────────

async function requestNotificationPermission() {
    if (!('Notification' in window)) return;
    if (Notification.permission === 'default') {
        await Notification.requestPermission();
    }
}

// ── Clear history ─────────────────────────────────────────────────────────────

async function clearHistory() {
    try {
        const res = await fetch('/api/messages', { method: 'DELETE' });
        if (res.ok) {
            document.getElementById('messages-list').innerHTML = '';
        }
    } catch (err) {
        console.error('Грешка при изчистване на историята:', err);
    }
}


// ── Dark mode ─────────────────────────────────────────────────────────────────

(function initTheme() {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') applyDark(true);
})();

function applyDark(on) {
    document.body.classList.toggle('dark', on);
    document.getElementById('theme-toggle').textContent = on ? '☀️' : '🌙';
}

document.getElementById('theme-toggle').addEventListener('click', function () {
    const isDark = document.body.classList.toggle('dark');
    this.textContent = isDark ? '☀️' : '🌙';
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
});

// ── Init ─────────────────────────────────────────────────────────────────────

document.getElementById('username-input').addEventListener('keydown', handleUsernameEnter);

function handleUsernameEnter(e) {
    if (e.key === 'Enter') connect();
}
