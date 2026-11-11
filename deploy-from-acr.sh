#!/usr/bin/env bash
set -Eeuo pipefail

# Compose 和 Deploy-arc脚本所在目录
DEPLOY_DIR="/home/deploy-project/xiaoyan-serve"
LOCK_FILE="/tmp/xiaoyan-server-deploy.lock"

log() {
  printf '[%s] %s\n' "$(date '+%F %T')" "$*"
}

# 若上一次仍未结束，则本次直接跳过。
exec 9>"${LOCK_FILE}"
if ! flock -n 9; then
  log "已有部署任务正在运行，本次跳过"
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  log "错误：找不到 docker 命令"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  log "错误：docker compose 不可用"
  exit 1
fi

if [[ ! -f "${DEPLOY_DIR}/docker-compose.yml" || ! -f "${DEPLOY_DIR}/.env" ]]; then
  log "错误：${DEPLOY_DIR} 中缺少 docker-compose.yml 或 .env"
  exit 1
fi

cd "${DEPLOY_DIR}"

log "检查并拉取 ACR 最新镜像"
docker compose pull

log "应用 Compose 配置；镜像未变化时不会重建容器"
docker compose up -d --remove-orphans

log "部署检查完成"
