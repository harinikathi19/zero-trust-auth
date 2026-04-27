// auth.js - signup and login functions used by pages
const BASE = 'http://localhost:8080';

// Track login behavior for anomaly detection
function getLoginBehavior() {
  const lastLoginTime = localStorage.getItem('lastLoginTime');
  const loginAttempts = parseInt(localStorage.getItem('loginAttempts') || '0');
  const lastFailureTime = localStorage.getItem('lastFailureTime');
  const currentTime = Date.now();
  
  let timeSinceLastLogin = null;
  let timeSinceLastFailure = null;
  
  if (lastLoginTime) {
    timeSinceLastLogin = currentTime - parseInt(lastLoginTime);
  }
  if (lastFailureTime) {
    timeSinceLastFailure = currentTime - parseInt(lastFailureTime);
  }
  
  const loginHour = new Date().getHours();
  const isMidnightLogin = loginHour >= 0 && loginHour < 4;
  
  return {
    loginAttempts,
    timeSinceLastLogin,
    timeSinceLastFailure,
    loginHour,
    isMidnightLogin,
    timestamp: currentTime
  };
}

async function signup(username, password){
  // Generate fingerprint for signup too
  const device = await window.fingerprint.simpleFingerprint();
  const geo = await window.fingerprint.getGeo();
  
  const res = await fetch(BASE + '/api/auth/signup', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Device-Hash': device.hash,
      'User-Agent': navigator.userAgent
    },
    body: JSON.stringify({
      username, 
      password,
      deviceHash: device.hash,
      deviceDetails: device.details,
      userAgent: navigator.userAgent,
      geo: geo || ''
    })
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  return res.text();
}

async function login(username, password, typingMetrics = null){
  // generate fingerprint and geo before login
  const device = await window.fingerprint.simpleFingerprint();
  const geo = await window.fingerprint.getGeo();
  const loginBehavior = getLoginBehavior();
  
  // Get IP from headers (backend will extract it)
  // send login data with device info in body so backend can use it
  const body = {
    username, 
    password,
    deviceHash: device.hash,
    deviceDetails: device.details,
    userAgent: navigator.userAgent,
    geo: geo || '',
    loginBehavior: {
      ...loginBehavior,
      typingMetrics
    }
  };
  
  const res = await fetch(BASE + '/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Device-Hash': device.hash,
      'User-Agent': navigator.userAgent,
      'geo': geo || ''
    },
    body: JSON.stringify(body)
  });
  
  if (!res.ok) {
    const text = await res.text();
    // Track failed login attempt
    localStorage.setItem('loginAttempts', (loginBehavior.loginAttempts + 1).toString());
    localStorage.setItem('lastFailureTime', Date.now().toString());
    throw new Error(text || res.statusText);
  }
  
  const data = await res.json();
  
  // Handle OTP challenge for high-risk logins
  if (data.requiresOTP) {
    return { requiresOTP: true, challengeId: data.challengeId, message: data.message };
  }
  
  // save token on successful login
  if (data && data.token) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('deviceHash', device.hash);
    localStorage.setItem('lastLoginTime', Date.now().toString());
    localStorage.setItem('loginAttempts', '0'); // Reset on success
    // redirect to dashboard
    window.location.href = '../pages/dashboard.html';
  }
  return data;
}

async function verifyOTP(challengeId, otp) {
  const device = await window.fingerprint.simpleFingerprint();
  const res = await fetch(BASE + '/api/auth/verify-otp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Device-Hash': device.hash
    },
    body: JSON.stringify({ challengeId, otp })
  });
  
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  
  const data = await res.json();
  if (data && data.token) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('deviceHash', device.hash);
    localStorage.setItem('lastLoginTime', Date.now().toString());
    localStorage.setItem('loginAttempts', '0');
    window.location.href = '../pages/dashboard.html';
  }
  return data;
}

async function authFetch(path, opts = {}) {
  const headers = opts.headers || {};
  const token = localStorage.getItem('token');
  // Refresh fingerprint for continuous validation
  const device = await window.fingerprint.simpleFingerprint();
  const geo = await window.fingerprint.getGeo();
  const deviceHash = device.hash;
  
  headers['Authorization'] = 'Bearer ' + token;
  headers['X-Device-Hash'] = deviceHash;
  headers['User-Agent'] = navigator.userAgent;
  headers['geo'] = geo || '';
  
  // Update stored device hash
  localStorage.setItem('deviceHash', deviceHash);
  
  opts.headers = headers;
  return fetch('http://localhost:8080' + path, opts);
}
