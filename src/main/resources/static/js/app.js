function App() {
  return {
    SETTINGS_KEY: 'sonovel-settings',

    // ---------- 导航 ----------
    tabs: [
      {key: 'search', label: '搜索'},
      {key: 'download', label: '下载管理'},
      {key: 'source', label: '书源管理'},
      {key: 'setting', label: '设置'},
      {key: 'about', label: '关于'}
    ],
    tab: 'search',

    // ---------- 搜索 ----------
    searchKeyword: '',
    isSearching: false,
    suggestions: [],
    showSuggestions: false,
    bookCache: [],
    currentPage: 1,
    sourceStatuses: [],
    statusPanelOpen: true,
    searchSubTab: 'aggregated',

    // ---------- 下载菜单 / Loading ----------
    dropdownItem: null,
    dropdownStyle: '',
    openDropdownIndex: null,
    showTip: false,
    tipText: '正在加载...',
    downloadCurrent: 0,
    downloadTotal: 0,

    // ---------- 配置 ----------
    serverConfig: {searchLimit: -1, concurrency: -1, version: '', extName: 'epub'},
    settings: {format: '', language: '', searchLimit: '', concurrency: ''},

    // ---------- 书源管理 ----------
    sources: [],
    sourcesChecking: false,
    sourceEditor: {open: false, mode: 'add', id: null, json: ''},
    sourceQuery: '',
    sourceQueryResults: [],
    sourceQueryTried: false,
    highlightSourceId: null,
    highlightSearchId: null,

    // ---------- 详情 / 批量 ----------
    detailUrl: '',
    detailFormat: '',
    batchUrls: '',
    batchRunning: false,
    batchCurrent: 0,
    batchTotal: 0,
    batchResult: '',

    // ---------- 本地书 ----------
    localBooks: [],
    latestFileName: '',

    // ---------- 检查更新 ----------
    updateInfo: null,
    checkingUpdate: false,

    // ---------- 服务器配置表单 ----------
    configGroups: [
      {name: 'download', fields: [
        {key: 'extname', label: '导出格式', options: ['epub', 'txt', 'html', 'pdf']},
        {key: 'download-path', label: '下载路径', placeholder: 'downloads'},
        {key: 'txt-encoding', label: 'TXT 编码', placeholder: 'UTF-8'}
      ]},
      {name: 'source', fields: [
        {key: 'active-rules', label: '激活规则集', restart: true, placeholder: 'main.json'},
        {key: 'search-limit', label: '搜索条数上限', placeholder: '-1 不限'},
        {key: 'language', label: '书源语言', placeholder: '留空自动'}
      ]},
      {name: 'crawl', fields: [
        {key: 'concurrency', label: '并发数', placeholder: '-1 跟随默认'},
        {key: 'min-interval', label: '最小间隔(ms)'},
        {key: 'max-interval', label: '最大间隔(ms)'},
        {key: 'max-retries', label: '失败重试次数'}
      ]},
      {name: 'global', fields: [
        {key: 'cf-bypass', label: 'CF 绕过服务', placeholder: 'http://127.0.0.1:8000'},
        {key: 'gh-proxy', label: 'GitHub 代理', placeholder: '留空'},
        {key: 'auto-update', label: '自动更新', options: ['0', '1']}
      ]},
      {name: 'web', fields: [
        {key: 'enabled', label: '启用 Web', options: ['0', '1'], restart: true},
        {key: 'port', label: 'Web 端口', restart: true}
      ]},
      {name: 'proxy', fields: [
        {key: 'enabled', label: '启用代理', options: ['0', '1']},
        {key: 'host', label: '代理主机'},
        {key: 'port', label: '代理端口'}
      ]},
      {name: 'cookie', fields: [
        {key: 'qidian', label: '起点 Cookie', placeholder: '留空'}
      ]}
    ],
    configForm: {
      download: {'extname': 'epub', 'download-path': 'downloads', 'txt-encoding': 'UTF-8'},
      source: {'active-rules': 'main.json', 'search-limit': '-1', 'language': ''},
      crawl: {'concurrency': '-1', 'min-interval': '200', 'max-interval': '400', 'max-retries': '5'},
      global: {'cf-bypass': '', 'gh-proxy': '', 'auto-update': '0'},
      web: {'enabled': '1', 'port': '7765'},
      proxy: {'enabled': '0', 'host': '127.0.0.1', 'port': '7890'},
      cookie: {'qidian': ''}
    },

    // ---------- 样式 ----------
    sectionClass: 'bg-white rounded-xl shadow-md p-4 sm:p-6',
    h2Class: 'text-lg sm:text-2xl font-semibold text-primary border-l-4 border-primary pl-3',
    overlayClass: 'fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/40 backdrop-blur-sm',
    cardClass: 'w-full rounded-2xl bg-white p-6 shadow-2xl ring-1 ring-gray-900/5 max-h-[88vh] flex flex-col',

    // ---------- 图标 ----------
    iconSearch: '<svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    iconDownload: '<svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" stroke-linecap="round" stroke-linejoin="round"/><polyline points="7 10 12 15 17 10" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="15" x2="12" y2="3" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    iconDelete: '<svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6" stroke-linecap="round" stroke-linejoin="round"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    iconRefresh: '<svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    iconEdit: '<svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke-linecap="round" stroke-linejoin="round"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    iconExternal: '<svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6" stroke-linecap="round" stroke-linejoin="round"/><path d="M15 3h6v6M10 14L21 3" stroke-linecap="round" stroke-linejoin="round"/></svg>',

    // ---------- getters ----------
    get pageSize() {
      const v = parseInt(this.settings.searchLimit);
      return v > 0 ? v : 20
    },
    get paginatedBooks() {
      const s = (this.currentPage - 1) * this.pageSize;
      return this.bookCache.slice(s, s + this.pageSize)
    },
    get totalPages() {
      return Math.max(1, Math.ceil(this.bookCache.length / this.pageSize))
    },
    get paginationInfo() {
      const max = 5;
      let start = Math.max(1, this.currentPage - Math.floor(max / 2));
      let end = Math.min(this.totalPages, start + max - 1);
      if (end - start < max - 1) start = Math.max(1, end - max + 1);
      const pages = [];
      for (let i = start; i <= end; i++) pages.push(i);
      return {start, end, pages}
    },
    get defaultExtLabel() {
      return (this.settings.format || this.serverConfig.extName || 'epub').toUpperCase()
    },
    get versionLink() {
      return this.serverConfig.version
        ? `https://github.com/freeok/so-novel/releases/tag/v${this.serverConfig.version}` : '#'
    },

    // ---------- 初始化 ----------
    init() {
      this.loadSettings();
      this.fetchConfig();
      this.fetchLocalBooks(false);
      this.initSSE();
      this.$watch('settings',
        () => localStorage.setItem(this.SETTINGS_KEY, JSON.stringify(this.settings)),
        {deep: true});
    },

    switchTab(k) {
      this.tab = k;
      this.closeDownloadMenu();
      if (k === 'download') this.fetchLocalBooks(false);
      if (k === 'source') this.fetchSources();
      window.scrollTo({top: 0, behavior: 'smooth'});
    },

    // ---------- 搜索建议 ----------
    async fetchSuggestions() {
      const kw = this.searchKeyword.trim();
      if (!kw) {
        this.suggestions = []; this.showSuggestions = false; return;
      }
      try {
        const d = await Api.getSuggestions(kw);
        if (d && d.code === 200) {
          this.suggestions = d.data || [];
          this.showSuggestions = this.suggestions.length > 0;
        }
      } catch (_) {
        this.suggestions = []; this.showSuggestions = false;
      }
    },
    closeSuggestions() {
      setTimeout(() => this.showSuggestions = false, 200);
    },

    // ---------- 聚合搜索 ----------
    async aggregatedSearch() {
      const v = this.searchKeyword.trim();
      if (!v) {alert('请输入书名或作者！'); return;}
      this.closeDownloadMenu();
      this.displayTip('正在聚合搜索...');
      this.isSearching = true;
      try {
        const d = await Api.search(v);
        if (d && d.code === 200 && d.data) {
          this.bookCache = d.data.results || [];
          this.sourceStatuses = d.data.sourceStatus || [];
          this.statusPanelOpen = true;
          this.currentPage = 1;
        } else {
          alert('搜索失败: ' + ((d && d.message) || '未知错误'));
        }
      } catch (e) {
        alert('搜索失败: ' + e.message);
      } finally {
        this.hideTip();
        this.isSearching = false;
      }
    },

    // ---------- 独立搜索（仅单源，对应 TUI w） ----------
    async singleSourceSearch(id) {
      const kw = prompt('在该书源搜索，请输入书名或作者：');
      if (!kw || !kw.trim()) return;
      this.displayTip('正在搜索...');
      try {
        const d = await Api.singleSearch(id, kw.trim());
        if (d.code === 200) {
          this.tab = 'search';
          this.searchSubTab = 'aggregated';
          this.searchKeyword = kw.trim();
          this.bookCache = d.data || [];
          this.sourceStatuses = [];
          this.statusPanelOpen = false;
          this.currentPage = 1;
        } else {
          alert('搜索失败: ' + (d.message || ''));
        }
      } catch (e) {
        alert('搜索失败: ' + e.message);
      } finally {
        this.hideTip();
      }
    },

    // ---------- 需求3：源状态展示 ----------
    statusCount(s) {
      return this.sourceStatuses.filter(x => x.status === s).length;
    },
    statusDot(s) {
      return ({ok: 'bg-green-500', empty: 'bg-gray-300', interval: 'bg-amber-500',
        timeout: 'bg-red-500', error: 'bg-red-500'})[s] || 'bg-gray-300';
    },
    statusText(s) {
      return ({ok: 'text-green-600', empty: 'text-gray-400', interval: 'text-amber-600',
        timeout: 'text-red-500', error: 'text-red-500'})[s] || 'text-gray-400';
    },
    statusLabel(st) {
      if (st.status === 'ok') return st.count + ' 条';
      if (st.status === 'empty') return '无结果';
      if (st.status === 'interval') return '限流';
      if (st.status === 'timeout') return '超时';
      if (st.status === 'error') return (st.message || '错误').slice(0, 24);
      return st.status;
    },

    // ---------- 分页 ----------
    getGlobalIndex(i) {
      return (this.currentPage - 1) * this.pageSize + i + 1;
    },
    changePage(p) {
      this.closeDownloadMenu();
      if (p === 'prev') this.currentPage--;
      else if (p === 'next') this.currentPage++;
      else this.currentPage = parseInt(p);
      this.currentPage = Math.max(1, Math.min(this.currentPage, this.totalPages));
    },

    // ---------- 下载格式菜单 ----------
    toggleDownloadMenu(index, item, e) {
      if (this.openDropdownIndex === index) {this.closeDownloadMenu(); return;}
      const rect = e.currentTarget.getBoundingClientRect();
      const w = 144, h = 200;
      let top = rect.bottom + 6;
      if (top + h > window.innerHeight - 8) top = rect.top - h - 6;
      let left = Math.max(8, Math.min(rect.right - w, window.innerWidth - w - 8));
      this.openDropdownIndex = index;
      this.dropdownItem = item;
      this.dropdownStyle = `top:${top}px;left:${left}px`;
    },
    closeDownloadMenu() {
      this.openDropdownIndex = null;
      this.dropdownItem = null;
      this.dropdownStyle = '';
    },

    // ---------- 单本下载到服务器（搜索结果） ----------
    async handleDownloadToServer(item, format) {
      this.closeDownloadMenu();
      if (!item) {alert('参数错误，请重新搜索'); return;}
      this.downloadCurrent = 0; this.downloadTotal = 0;
      this.displayTip('正在解析章节目录...');
      const params = new URLSearchParams({...item});
      const ff = format || this.settings.format || '';
      if (ff) params.set('format', ff);
      if (this.settings.language) params.set('language', this.settings.language);
      if (this.settings.concurrency) params.set('concurrency', this.settings.concurrency);
      try {
        const resp = await Api.downloadBook(params);
        if (!resp.ok) {
          const d = await resp.json();
          throw new Error(d.message || '未知错误');
        }
        await this.fetchLocalBooks(false);
        location.href = Api.downloadFileFromServer(this.latestFileName);
      } catch (e) {
        alert('下载失败: ' + e.message);
      } finally {
        this.hideTip();
      }
    },

    // ---------- 详情页 URL 下载 ----------
    async downloadDetailUrl() {
      const url = this.detailUrl.trim();
      if (!url) {alert('请粘贴书籍详情页网址'); return;}
      this.downloadCurrent = 0; this.downloadTotal = 0;
      this.displayTip('正在解析章节目录...');
      const params = new URLSearchParams({url});
      if (this.detailFormat) params.set('format', this.detailFormat);
      if (this.settings.language) params.set('language', this.settings.language);
      try {
        const resp = await Api.downloadBook(params);
        if (!resp.ok) {
          const d = await resp.json();
          throw new Error(d.message || '未知错误');
        }
        await this.fetchLocalBooks(false);
        location.href = Api.downloadFileFromServer(this.latestFileName);
      } catch (e) {
        alert('下载失败: ' + e.message);
      } finally {
        this.hideTip();
      }
    },

    // ---------- 批量下载（前端串行循环） ----------
    async runBatch() {
      const urls = this.batchUrls.split('\n').map(x => x.trim()).filter(Boolean);
      if (urls.length === 0) {alert('请至少粘贴一个详情页网址'); return;}
      this.batchRunning = true;
      this.batchCurrent = 0;
      this.batchTotal = urls.length;
      this.batchResult = '';
      let ok = 0;
      const fails = [];
      for (let i = 0; i < urls.length; i++) {
        this.downloadCurrent = 0; this.downloadTotal = 0;
        this.displayTip(`批量下载：第 ${i + 1}/${urls.length} 本，解析中...`);
        this.batchResult = `正在下载第 ${i + 1}/${urls.length} 本...`;
        try {
          const params = new URLSearchParams({url: urls[i]});
          if (this.detailFormat) params.set('format', this.detailFormat);
          const resp = await Api.downloadBook(params);
          if (!resp.ok) {
            const d = await resp.json();
            throw new Error(d.message || '未知错误');
          }
          ok++;
        } catch (e) {
          fails.push(urls[i] + '  (' + e.message + ')');
        }
        this.batchCurrent = i + 1;
      }
      await this.fetchLocalBooks(false);
      this.batchResult = `完成：成功 ${ok} 本，失败 ${urls.length - ok} 本。`
        + (fails.length ? '\n失败列表：\n' + fails.join('\n') : '');
      this.batchRunning = false;
      this.hideTip();
    },

    // ---------- Loading ----------
    displayTip(t) {
      this.tipText = t || '正在加载...';
      this.showTip = true;
    },
    hideTip() {
      this.showTip = false;
      this.downloadCurrent = 0;
      this.downloadTotal = 0;
    },

    // ---------- 配置读取 ----------
    async fetchConfig() {
      try {
        const d = await Api.getConfig();
        if (d && d.data) {
          Object.assign(this.serverConfig, d.data);
          this.fillConfigForm();
        }
      } catch (_) {}
    },
    fillConfigForm() {
      const c = this.serverConfig;
      this.configForm.download.extname = c.extName;
      this.configForm.download['download-path'] = c.downloadPath;
      this.configForm.download['txt-encoding'] = c.txtEncoding;
      this.configForm.source['active-rules'] = c.activeRules;
      this.configForm.source['search-limit'] = String(c.searchLimit);
      this.configForm.source.language = c.language;
      this.configForm.crawl.concurrency = String(c.concurrency);
      this.configForm.crawl['min-interval'] = String(c.minInterval);
      this.configForm.crawl['max-interval'] = String(c.maxInterval);
      this.configForm.crawl['max-retries'] = String(c.maxRetries);
      this.configForm.global['cf-bypass'] = c.cfBypass || '';
      this.configForm.global['gh-proxy'] = c.ghProxy || '';
      this.configForm.global['auto-update'] = String(c.autoUpdate);
      this.configForm.web.enabled = String(c.webEnabled);
      this.configForm.web.port = String(c.webPort);
      this.configForm.proxy.enabled = String(c.proxyEnabled);
      this.configForm.proxy.host = c.proxyHost;
      this.configForm.proxy.port = String(c.proxyPort);
      this.configForm.cookie.qidian = c.qidianCookie || '';
    },
    async saveServerConfig() {
      this.displayTip('正在保存服务器配置...');
      try {
        const d = await Api.saveConfig(this.configForm);
        if (d && d.code === 200) {
          alert('已保存并写回 config.ini。带 ⟳ 的项（端口/启用、规则集）需重启程序才生效。');
          await this.fetchConfig();
        } else {
          alert('保存失败: ' + (d && d.message || '未知错误'));
        }
      } catch (e) {
        alert('保存失败: ' + e.message);
      } finally {
        this.hideTip();
      }
    },

    // ---------- 本地书 ----------
    async fetchLocalBooks(show) {
      if (show) this.displayTip('正在刷新...');
      try {
        const d = await Api.getLocalBooks();
        if (d && d.data) {
          d.data.sort((a, b) => b.timestamp - a.timestamp);
          if (d.data[0]) this.latestFileName = d.data[0].name;
          this.localBooks = d.data;
        }
      } catch (e) {
        if (show) alert('刷新失败: ' + e.message);
      } finally {
        if (show) this.hideTip();
      }
    },
    async handleDeleteBook(name) {
      if (!confirm(`确定删除「${name}」？`)) return;
      try {
        const d = await Api.deleteBook(name);
        if (d.code === 200) this.localBooks = this.localBooks.filter(x => x.name !== name);
        else alert('删除失败: ' + (d.message || ''));
      } catch (e) {
        alert('删除失败: ' + e.message);
      }
    },

    // ---------- 书源管理 ----------
    async fetchSources() {
      try {
        const d = await Api.getSources();
        if (d && d.data) this.sources = d.data;
      } catch (_) {}
    },
    async checkSources() {
      this.sourcesChecking = true;
      try {
        const d = await Api.checkSources();
        if (d && d.data) this.sources = d.data;
      } catch (_) {}
      this.sourcesChecking = false;
    },
    // ---------- 书源搜索：按序号或名字定位 ----------
    async findSource() {
      const q = this.sourceQuery.trim();
      if (!q) return;
      if (this.sources.length === 0) await this.fetchSources();

      let matches;
      if (/^\d+$/.test(q)) {
        matches = this.sources.filter(s => s.id === parseInt(q, 10));
      } else {
        matches = this.sources.filter(s => (s.name || '').includes(q));
      }
      this.sourceQueryResults = matches;
      this.sourceQueryTried = true;
      // 定位第一个匹配（多个时可用候选列表精确跳转）
      if (matches.length >= 1) this.locateSource(matches[0].id);
    },
    // 留在「搜索-书源搜索」页，在书源搜索列表内滚动定位并短暂高亮
    locateSource(id) {
      this.tab = 'search';
      this.searchSubTab = 'source';
      this.highlightSearchId = null;
      this.$nextTick(() => {
        requestAnimationFrame(() => {
          const el = document.getElementById('source-search-row-' + id);
          if (el) el.scrollIntoView({behavior: 'smooth', block: 'center'});
          this.highlightSearchId = id;
          clearTimeout(this._hlTimer);
          this._hlTimer = setTimeout(() => {
            if (this.highlightSearchId === id) this.highlightSearchId = null;
          }, 3200);
        });
      });
    },
    async openSourceEditor(mode, id) {
      this.sourceEditor.mode = mode;
      this.sourceEditor.open = true;
      if (mode === 'add') {
        this.sourceEditor.id = null;
        this.sourceEditor.json = JSON.stringify(this.emptyRule(), null, 2);
      } else {
        this.sourceEditor.id = id;
        const d = await Api.getSource(id);
        if (d.code === 200) this.sourceEditor.json = JSON.stringify(d.data, null, 2);
        else {alert('加载失败: ' + d.message); this.sourceEditor.open = false;}
      }
    },
    async saveSourceEditor() {
      let obj;
      try {
        obj = JSON.parse(this.sourceEditor.json);
      } catch (e) {
        alert('JSON 格式错误: ' + e.message);
        return;
      }
      if (!obj.name || !obj.url) {alert('name 和 url 必填'); return;}
      const json = JSON.stringify(obj);
      const ed = this.sourceEditor;
      const d = ed.mode === 'add'
        ? await Api.createSource(json)
        : await Api.updateSource(ed.id, json);
      if (d.code === 200) {
        ed.open = false;
        alert('已保存并写回规则文件');
        await this.fetchSources();
      } else {
        alert('保存失败: ' + (d.message || ''));
      }
    },
    async handleDeleteSource(id) {
      if (!confirm('确定删除该书源？删除后 ID 会重新编号。')) return;
      const d = await Api.deleteSource(id);
      if (d.code === 200) await this.fetchSources();
      else alert('删除失败: ' + (d.message || ''));
    },
    // 启用/禁用书源：后端写回成功后再本地翻转，保持 UI 与实际状态一致
    async toggleSource(s) {
      const next = !s.disabled;
      try {
        const d = await Api.patchSource(s.id, 'ruleDisabled', next);
        if (d && d.code === 200) {
          s.disabled = next;
        } else {
          alert('操作失败: ' + ((d && d.message) || '未知错误'));
        }
      } catch (e) {
        alert('操作失败: ' + e.message);
      }
    },
    emptyRule() {
      return {
        name: '', url: '', comment: '', language: '', needProxy: false, disabled: false,
        search: {disabled: false, baseUri: '', timeout: 15, url: '', method: 'get',
          data: '', cookies: '', result: '', bookName: '', author: '', latestChapter: '',
          lastUpdateTime: '', nextPage: ''},
        book: {baseUri: '', timeout: 15, url: '', bookName: '', author: '', intro: '',
          coverUrl: '', latestChapter: '', lastUpdateTime: '', status: ''},
        toc: {baseUri: '', timeout: 60, url: '', list: '', item: '', isDesc: true, nextPage: ''},
        chapter: {baseUri: '', timeout: 15, title: '', content: '', paragraphTagClosed: true,
          paragraphTag: '', filterTxt: '', filterTag: '', nextPage: '', nextChapterLink: ''}
      };
    },

    // ---------- 检查更新 ----------
    async doCheckUpdate() {
      this.checkingUpdate = true;
      try {
        const d = await Api.checkUpdate();
        if (d.code === 200) this.updateInfo = d.data;
        else alert('检查失败: ' + d.message);
      } catch (e) {
        alert('检查失败: ' + e.message);
      } finally {
        this.checkingUpdate = false;
      }
    },

    // ---------- SSE ----------
    initSSE() {
      const es = new EventSource('/download-progress');
      es.onmessage = e => {
        try {
          const d = JSON.parse(e.data);
          if (d.type === 'download-progress') {
            this.downloadCurrent = d.index;
            this.downloadTotal = d.total;
          }
        } catch (err) {
        }
      };
    },

    // ---------- 浏览器偏好 ----------
    loadSettings() {
      try {
        const s = JSON.parse(localStorage.getItem(this.SETTINGS_KEY) || '{}');
        this.settings.format = s.format || '';
        this.settings.language = s.language || '';
        this.settings.searchLimit = s.searchLimit || '';
        this.settings.concurrency = s.concurrency || '';
      } catch (_) {}
    },

    // ---------- 格式化 ----------
    formatDate(ts) {
      return new Date(ts).toLocaleString('zh-CN');
    },
    formatSize(bytes) {
      if (!bytes) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
    }

  };
}
