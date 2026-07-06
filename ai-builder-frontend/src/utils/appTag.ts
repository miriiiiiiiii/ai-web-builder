/** 根据 codeGenType 获取应用标签 */
export function getAppTag(codeGenType?: string): { label: string; color: string } {
  const tagMap: Record<string, { label: string; color: string }> = {
    html: { label: '网站', color: 'default' },
    react: { label: '用户应用', color: 'purple' },
    vue: { label: '用户应用', color: 'purple' },
    multi_file: { label: '工具', color: 'gold' },
  }

  if (codeGenType && tagMap[codeGenType]) {
    return tagMap[codeGenType]
  }

  return { label: '用户应用', color: 'purple' }
}
