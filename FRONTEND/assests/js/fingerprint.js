// fingerprint.js - generates a device fingerprint hash (SHA-256) using multiple signals
async function canvasHash() {
  try {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    ctx.textBaseline = 'top';
    ctx.font = "16px 'Arial'";
    ctx.fillStyle = '#f60';
    ctx.fillRect(125,1,62,20);
    ctx.fillStyle = '#069';
    ctx.fillText('ZeroTrust', 2, 2);
    const data = canvas.toDataURL();
    const enc = new TextEncoder().encode(data);
    const hashBuffer = await crypto.subtle.digest('SHA-256', enc);
    return Array.from(new Uint8Array(hashBuffer)).map(b=>b.toString(16).padStart(2,'0')).join('');
  } catch(e){ return ''; }
}

async function webGLHash() {
  try {
    const canvas = document.createElement('canvas');
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
    if (!gl) return '';
    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
    const vendor = gl.getParameter(debugInfo ? debugInfo.UNMASKED_VENDOR_WEBGL : gl.VENDOR) || '';
    const renderer = gl.getParameter(debugInfo ? debugInfo.UNMASKED_RENDERER_WEBGL : gl.RENDERER) || '';
    const version = gl.getParameter(gl.VERSION) || '';
    const shadingLang = gl.getParameter(gl.SHADING_LANGUAGE_VERSION) || '';
    const data = [vendor, renderer, version, shadingLang].join('||');
    const enc = new TextEncoder().encode(data);
    const hashBuffer = await crypto.subtle.digest('SHA-256', enc);
    return Array.from(new Uint8Array(hashBuffer)).map(b=>b.toString(16).padStart(2,'0')).join('');
  } catch(e){ return ''; }
}

function getInstalledFonts() {
  // Check for common fonts
  const fonts = ['Arial', 'Verdana', 'Times New Roman', 'Courier New', 'Georgia', 'Palatino', 'Garamond', 'Bookman', 'Comic Sans MS', 'Trebuchet MS', 'Impact'];
  const available = [];
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  const baseString = 'mmmmmmmmmmlli';
  const baseWidth = ctx.measureText(baseString).width;
  
  fonts.forEach(font => {
    ctx.font = `72px ${font}`;
    const width = ctx.measureText(baseString).width;
    if (width !== baseWidth) {
      available.push(font);
    }
  });
  return available.join(',');
}

async function simpleFingerprint() {
  const ua = navigator.userAgent || '';
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
  const scr = [screen.width, screen.height, screen.colorDepth, screen.pixelDepth].join('x');
  const platform = navigator.platform || '';
  const language = navigator.language || '';
  const languages = navigator.languages ? navigator.languages.join(',') : '';
  const hardwareConcurrency = navigator.hardwareConcurrency || '';
  const deviceMemory = navigator.deviceMemory || '';
  const canvas = await canvasHash();
  const webgl = await webGLHash();
  const fonts = getInstalledFonts();
  const timezoneOffset = new Date().getTimezoneOffset();
  const doNotTrack = navigator.doNotTrack || '';
  const cookieEnabled = navigator.cookieEnabled ? '1' : '0';
  const onlineStatus = navigator.onLine ? '1' : '0';
  
  const raw = [
    ua, platform, tz, scr, language, languages,
    hardwareConcurrency, deviceMemory, canvas, webgl, fonts,
    timezoneOffset, doNotTrack, cookieEnabled, onlineStatus
  ].join('||');
  
  const enc = new TextEncoder().encode(raw);
  const hashBuffer = await crypto.subtle.digest('SHA-256', enc);
  const hash = Array.from(new Uint8Array(hashBuffer)).map(b=>b.toString(16).padStart(2,'0')).join('');
  return {hash, raw, details: {
    userAgent: ua,
    platform,
    timezone: tz,
    screen: scr,
    language,
    languages,
    hardwareConcurrency,
    deviceMemory,
    canvasHash: canvas,
    webglHash: webgl,
    fonts,
    timezoneOffset,
    doNotTrack,
    cookieEnabled,
    onlineStatus
  }};
}

// attempt to get geo from browser (consent) else fallback to empty string
async function getGeo() {
  return new Promise((resolve)=> {
    if (navigator.geolocation) {
      const ok = (pos)=> resolve(pos.coords.latitude + ',' + pos.coords.longitude);
      const err = ()=> resolve('');
      navigator.geolocation.getCurrentPosition(ok, err, {timeout:3000});
    } else {
      resolve('');
    }
  });
}

// produce headers for authenticated requests
async function authHeaders() {
  const device = await simpleFingerprint();
  // store deviceHash locally
  localStorage.setItem('deviceHash', device.hash);
  const geo = await getGeo();
  return {
    'X-Device-Hash': device.hash,
    'User-Agent': navigator.userAgent,
    'geo': geo
  };
}

// export functions for other scripts
window.fingerprint = {
  simpleFingerprint,
  getGeo,
  authHeaders
};
