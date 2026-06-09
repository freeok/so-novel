const Api = {

  getConfig() {
    return fetch('/config').then(r => r.json())
  },

  getLocalBooks() {
    return fetch('/local-books').then(r => r.json())
  },

  search(keyword) {
    return fetch(`/search/aggregated?kw=${encodeURIComponent(keyword)}`)
      .then(r => r.json())
  },

  downloadBook(params) {
    return fetch(`/book-fetch?${params.toString()}`)
  },

  deleteBook(filename) {
    return fetch(`/book-delete?filename=${encodeURIComponent(filename)}`).then(r => r.json())
  },

}