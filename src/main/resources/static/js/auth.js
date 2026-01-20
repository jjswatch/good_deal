function parseJwt(token) {
	try {
	    const base64 = token.split('.')[1];
	    return JSON.parse(atob(base64));
	  } catch {
	    return null;
	  }
}

function getCurrentUser() {
  const token = localStorage.getItem("token");
  if (!token) return null;

  const payload = parseJwt(token);
  if (!payload) return null;
  
  return {
    id: payload.uid,
    username: payload.sub,
    role: payload.role
  };
}

function isLoggedIn() {
  return !!localStorage.getItem("token");
}

function isAdmin() {
  const user = getCurrentUser();
  return user && user.role === "ADMIN";
}

function isUser() {
  const user = getCurrentUser();
  return user && user.role === "USER";
}

function logout() {
  localStorage.clear();
  alert("已登出");
  location.href = "/login.html";
}
