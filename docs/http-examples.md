# ЛР1. Дослідження HTTP-взаємодії

## 1. Перегляд статичної сторінки через вбудований сервер

Java 18+ містить простий вебсервер `jwebserver`:

```bash
cd web
jwebserver -p 8000
```

Відкрийте http://localhost:8000 у браузері та вкладку **DevTools → Network**.

## 2. Запити через curl

```bash
# Лише заголовки відповіді (метод HEAD)
curl -I http://localhost:8000/

# Повний обмін: рядок запиту, заголовки запиту та відповіді
curl -v http://localhost:8000/css/style.css

# Неіснуючий ресурс — код стану 404
curl -i http://localhost:8000/missing.html

# Умовний запит: сервер може відповісти 304 Not Modified
curl -I -H "If-Modified-Since: Wed, 01 Jan 2031 00:00:00 GMT" http://localhost:8000/
```

## 3. Зовнішні сайти

```bash
curl -I https://ldubgd.edu.ua          # переадресація (301/302) і заголовок Location
curl -I https://www.google.com         # Set-Cookie, Cache-Control
curl -s -o /dev/null -w "%{http_code} %{time_total}s\n" https://developer.mozilla.org
```

## 4. Що зафіксувати у звіті

| Що дослідити | Де знайти |
|---|---|
| Метод, URL, версія протоколу | DevTools → Network → Headers |
| Код стану і його клас (2xx, 3xx, 4xx, 5xx) | Status |
| `Content-Type`, `Content-Length`, `Cache-Control` | Response Headers |
| `User-Agent`, `Accept`, `Accept-Language` | Request Headers |
| Cookies і сесія | Application → Cookies |
| Час завантаження ресурсів | Timing |
