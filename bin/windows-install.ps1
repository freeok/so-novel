<#
.SYNOPSIS
  SoNovel Windows 一键安装脚本
.DESCRIPTION
  从 GitHub Releases 下载最新版 SoNovel 并安装到用户目录。
  用法:
    PowerShell: irm https://raw.githubusercontent.com/freeok/so-novel/main/bin/windows-install.ps1 | iex
    CMD:        powershell -c "irm https://raw.githubusercontent.com/freeok/so-novel/main/bin/windows-install.ps1 | iex"
#>

$ErrorActionPreference = "Stop"

$repo = "freeok/so-novel"
$apiUrl = "https://api.github.com/repos/$repo/releases/latest"
$installDir = "$env:USERPROFILE\SoNovel"

# 检查 tar 命令是否可用
if (-not (Get-Command "tar" -ErrorAction SilentlyContinue))
{
    Write-Host "未找到 tar 命令，请升级到 Windows 10 build 17063 或更高版本。" -ForegroundColor Red
    exit 1
}

Write-Host "正在获取最新版本..." -ForegroundColor Cyan
try
{
    $release = Invoke-RestMethod -Uri $apiUrl -UseBasicParsing
}
catch
{
    Write-Host "获取版本信息失败，请检查网络连接。" -ForegroundColor Red
    exit 1
}
$tag = $release.tag_name
Write-Host "最新版本: $tag" -ForegroundColor Green

$asset = $release.assets | Where-Object { $_.name -eq "sonovel-windows.tar.gz" }
if (-not $asset)
{
    Write-Host "未找到 Windows 发布包 (sonovel-windows.tar.gz)" -ForegroundColor Red
    exit 1
}
$downloadUrl = $asset.browser_download_url

$tmpFile = "$env:TEMP\sonovel-windows.tar.gz"

Write-Host "正在下载 SoNovel Windows 版..." -ForegroundColor Cyan
try
{
    Invoke-WebRequest -Uri $downloadUrl -OutFile $tmpFile -UseBasicParsing
}
catch
{
    Write-Host "下载失败，请检查网络连接。" -ForegroundColor Red
    exit 1
}

Write-Host "正在清理旧安装..." -ForegroundColor Cyan
if (Test-Path $installDir)
{
    Remove-Item -Recurse -Force $installDir
}

Write-Host "正在解压..." -ForegroundColor Cyan
tar -xzf $tmpFile -C "$env:USERPROFILE"

Remove-Item $tmpFile

Write-Host "安装完成！" -ForegroundColor Green
Write-Host "安装目录: $installDir" -ForegroundColor Yellow
Write-Host ""
Write-Host "正在启动 SoNovel..." -ForegroundColor Cyan
Set-Location $installDir
Start-Process "sonovel.exe"
