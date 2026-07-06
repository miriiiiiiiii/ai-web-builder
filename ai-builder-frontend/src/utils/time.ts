import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/** 格式化为相对时间，如 "5 小时前" */
export function formatRelativeTime(time?: string): string {
  if (!time) {
    return '-'
  }
  return dayjs(time).fromNow()
}

/** 格式化为完整时间 */
export function formatDateTime(time?: string): string {
  if (!time) {
    return '-'
  }
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}
