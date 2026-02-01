self.addEventListener('install', (event) => {
    console.log('SW Installed');
});

self.addEventListener('fetch', (event) => {
    // 這裡可以留空，但檔案必須存在並被註冊
});