document.addEventListener('DOMContentLoaded', init);
window.callSearch = async () => await callSearch();

async function init() {
    ui.clearHistory.addEventListener('click', clearHistory);
    await renderHistory();
}

/**
 * Обработка информации об истории поисковых запросов.
 */
async function renderHistory() {
    ui.input.disabled = true;
    ui.submit.disabled = true;
    try {
        const history = await getHistory()
        if (history.length === 0) {
            addMessage(ui.chat.dataset.welcome);
            return;
        }
        ui.clearHistory.disabled = false;
        history.forEach(item => {
            addMessage(item.message, true);
            addMessage(item.answer, false, item.duration);
        });
    } catch (error) {
        addMessage(ui.chat.dataset.welcome);
    } finally {
        ui.input.disabled = false;
        ui.submit.disabled = false;
        ui.input.focus();
    }
}

/**
 * Добавление сообщений в чат.
 *
 * @param text текст сообщения
 * @param question текст сообщение это вопрос (true/false)
 * @param duration время обработки запроса
 */
function addMessage(text, question = false, duration = null) {
    const row = document.createElement('div');
    row.className = `d-flex ${question ? 'justify-content-end' : 'justify-content-start'}`;
    row.dataset.chatOwn = question;

    const content = document.createElement('div');
    content.className = `d-flex flex-column ${question ? 'align-items-end' : 'align-items-start'}`;
    if (question) {
        content.style.maxWidth = '75%';
    } else {
        content.classList.add('w-100');
    }

    const bubble = document.createElement('div');
    bubble.className = question
        ? 'bg-primary text-white rounded-2 px-2 py-1'
        : 'bg-light border rounded-2 px-2 py-1 w-100';
    if (question) {
        bubble.style.whiteSpace = 'pre-wrap';
        bubble.textContent = text;
    } else {
        renderMarkdown(bubble, text);
    }
    content.appendChild(bubble);
    if (!question && duration !== null) {
        const timing = document.createElement('small');
        timing.className = 'text-muted mt-1 px-1';
        timing.textContent = `${ui.chat.dataset.responseTime}: `
            + `${(duration / 1000).toFixed(2)} ${ui.chat.dataset.seconds}`;
        content.appendChild(timing);
    }
    row.appendChild(content);
    ui.messages.appendChild(row);
    ui.messages.scrollTop = ui.messages.scrollHeight;
    return row;
}

/**
 * Обработка Markdown текста.
 *
 * @param element элемент
 * @param text обработываемый текст
 */
function renderMarkdown(element, text) {
    if (typeof marked !== 'undefined') {
        element.innerHTML = marked.parse(text);
    } else {
        element.textContent = text;
    }
}

/**
 * Получение истории поисковых запросов.
 */
async function getHistory() {
    const response = await fetch(API_URL)
        .then(response => process(response));
    return await response.json();
}

/**
 * Очистка истории поисковых запросов.
 */
async function clearHistory() {
    try {
        await fetch(API_URL, {method: 'DELETE'})
            .then(response => process(response));
        ui.messages.replaceChildren();
        addMessage(ui.chat.dataset.welcome);
        ui.clearHistory.disabled = true;
        ui.input.focus();
    } catch (error) {
        ui.clearHistory.disabled = false;
        addMessage(ui.chat.dataset.error);
    }
}

/**
 * Вызов поискового запроса и его постобработка.
 */
async function callSearch() {
    const message = ui.input.value.trim();
    if (message) {
        addMessage(message, true);
        ui.input.value = '';
        ui.input.disabled = true;

        const pending = addMessage('');
        pending.querySelector('.border').innerHTML =
            '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span>';

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({message})
            }).then(response => process(response));
            const payload = await response.json();
            pending.remove();
            addMessage(payload.answer || ui.chat.dataset.error, false, payload.duration);
            ui.clearHistory.disabled = false;
        } catch (error) {
            pending.remove();
            addMessage(ui.chat.dataset.error);
        } finally {
            ui.input.disabled = false;
            ui.input.focus();
        }
    }
}

/**
 * Обработка ответа api.
 */
async function process(response) {
    if (response.status === 401) {
        window.location.assign('/login');
        return response;
    }
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }
    return response;
}

const API_URL = '/api/employees/search';

const ui = {
    chat: document.getElementById('ai-chat'),
    messages: document.getElementById('chat-messages'),
    form: document.getElementById('chat-form'),
    input: document.getElementById('chat-input'),
    submit: document.getElementById('chat-submit'),
    clearHistory: document.getElementById('clear-chat-history')
}
