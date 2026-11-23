const Api = {

  // ---------- 服务器配置 ----------
  getConfig() {
    return fetch('/config').then(r => r.json())
  },
  saveConfig(payload) {
    return fetch('/config', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(payload)
    }).then(r => r.json())
  },

  // ---------- 本地已下书籍 ----------
  getLocalBooks() {
    return fetch('/local-books').then(r => r.json())
  },
  deleteBook(filename) {
    return fetch(`/book-delete?filename=${encodeURIComponent(filename)}`).then(r => r.json())
  },
  downloadFileFromServer(filename) {
    return `/book-download?filename=${encodeURIComponent(filename)}`
  },

  // ---------- 搜索 ----------
  search(keyword) {
    return fetch(`/search/aggregated?kw=${encodeURIComponent(keyword)}`).then(r => r.json())
  },
  singleSearch(sourceId, keyword) {
    return fetch(`/search/single?sourceId=${sourceId}&kw=${encodeURIComponent(keyword)}`).then(r => r.json())
  },
  getSuggestions(kw) {
    return fetch(`/suggestion?kw=${encodeURIComponent(kw)}`).then(r => r.json())
  },

  // ---------- 下载到服务器 ----------
  downloadBook(params) {
    return fetch(`/book-fetch?${params.toString()}`)
  },

  // ---------- 书源 ----------
  getSources() {
    return fetch('/sources').then(r => r.json())
  },
  checkSources() {
    return fetch('/sources/check').then(r => r.json())
  },
  getSource(id) {
    return fetch(`/source-manage?id=${id}`).then(r => r.json())
  },
  createSource(ruleJson) {
    return fetch('/source-manage', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: ruleJson
    }).then(r => r.json())
  },
  updateSource(id, ruleJson) {
    return fetch(`/source-manage?id=${id}`, {
      method: 'PUT',
      headers: {'Content-Type': 'application/json'},
      body: ruleJson
    }).then(r => r.json())
  },
  deleteSource(id) {
    return fetch(`/source-manage?id=${id}`, {method: 'DELETE'}).then(r => r.json())
  },
  patchSource(id, field, value) {
    return fetch(`/source-manage?id=${id}&field=${field}&value=${value}`, {method: 'PATCH'}).then(r => r.json())
  },

  // ---------- 检查更新 ----------
  checkUpdate() {
    return fetch('/check-update').then(r => r.json())
  }

}
