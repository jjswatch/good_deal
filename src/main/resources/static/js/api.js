const API_BASE = location.hostname.includes("localhost")
  ? "http://localhost:8080/api"
  : "https://gooddeal-8hkw.onrender.com/api";

/* ========== 不需 JWT（login / register） ========== */
async function apiPostPublic(url, data) {
  const res = await fetch(API_BASE + url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });

  if (!res.ok) {
    const msg = await res.text();
    throw new Error(msg || "Request failed");
  }

  return res.json();
}

/* ========== 不需登入的 GET ========== */
async function apiGetPublic(url) {
  const res = await fetch(API_BASE + url);

  if (!res.ok) {
    const msg = await res.text();
    throw new Error(msg || "Request failed");
  }

  return res.json();
}

/* ========== 需要 JWT 的 API ========== */
async function apiFetch(url, options = {}) {
  const token = localStorage.getItem("token");

  options.headers = {
    ...(options.headers || {}),
    ...(token ? { Authorization: "Bearer " + token } : {})
  };

  if (!(options.body instanceof FormData)) {
    options.headers["Content-Type"] = "application/json";
  }

  const res = await fetch(API_BASE + url, options);

  if (res.status === 401) {
    alert("登入已失效，請重新登入");
    localStorage.clear();
    location.href = "login.html";
    throw new Error("Unauthorized");
  }
  
  if (res.status === 403) {
    alert("權限不足");
    throw new Error("Forbidden");
  }

  if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || "Request failed");
    }

    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

const apiGet = url => apiFetch(url);
const apiPost = (url, data) =>
  apiFetch(url, { method: "POST", body: JSON.stringify(data) });
const apiPut = (url, data) =>
	apiFetch(url, { method: "PUT", body: JSON.stringify(data) });
