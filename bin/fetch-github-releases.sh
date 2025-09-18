#!/bin/bash
set -euo pipefail

# =============== 配置 ===============
REPO="${1:-freeok/so-novel}"          # 第1个参数：GitHub 仓库，默认 freeok/so-novel
OUTPUT_FILE="${2:-CHANGELOG_ALL.md}"  # 第2个参数：输出文件，默认 CHANGELOG_ALL.md
PER_PAGE=100                          # 每页抓取的 release 数量
# ===================================

# 依赖检查
command -v jq >/dev/null 2>&1 || { echo "❌ 需要安装 jq"; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "❌ 需要安装 curl"; exit 1; }

# 清空输出文件
: > "$OUTPUT_FILE"

PAGE=1
while true; do
  echo "📦 Fetching page $PAGE..."
  RESP=$(curl -s "https://api.github.com/repos/${REPO}/releases?per_page=${PER_PAGE}&page=${PAGE}")

  COUNT=$(echo "$RESP" | jq length)
  if [[ "$COUNT" -eq 0 ]]; then
    break
  fi

  # 提取 release 标题、tag 和正文
  echo "$RESP" | jq -r '
    .[] |
    "## " + .name + "\n\n" +
    (.body // "No details") + "\n\n---\n"
  ' >> "$OUTPUT_FILE"

  PAGE=$((PAGE + 1))
done

echo "✅ 所有 changelog 已保存到 $OUTPUT_FILE"