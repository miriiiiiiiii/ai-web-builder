<template>
  <div class="app-manage-page">
    <a-breadcrumb class="breadcrumb">
      <a-breadcrumb-item>
        <router-link to="/admin/appManage">管理页</router-link>
      </a-breadcrumb-item>
      <a-breadcrumb-item>应用管理</a-breadcrumb-item>
    </a-breadcrumb>

    <a-card class="search-card">
      <a-form layout="inline" class="search-form">
        <a-form-item label="应用名称">
          <a-input
            v-model:value="searchParams.appName"
            placeholder="搜索应用名称"
            allow-clear
            class="search-input"
            @press-enter="doSearch"
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchParams.userName"
            placeholder="请输入用户名"
            allow-clear
            class="search-input"
            @press-enter="doSearch"
          />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number
            v-model:value="searchParams.priority"
            placeholder="优先级"
            class="search-input-sm"
          />
        </a-form-item>
        <a-form-item class="search-actions">
          <a-space>
            <a-button @click="doReset">重置</a-button>
            <a-button type="primary" @click="doSearch">搜索</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="应用信息" class="table-card">
      <a-table
        size="small"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'cover'">
            <a-image v-if="record.cover" :src="record.cover" :width="60" />
            <span v-else>-</span>
          </template>
          <template v-else-if="column.dataIndex === 'user'">
            {{ record.user?.nickName ?? record.userId ?? '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-else-if="column.dataIndex === 'updateTime'">
            {{ formatDateTime(record.updateTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" style="font-size:13px" @click="doEdit(record.id)">
                编辑
              </a-button>
              <a-button
                size="small"
                style="font-size:13px"
                @click="doFeature(record)"
              >
                精选
              </a-button>
              <a-button
                danger
                size="small"
                style="font-size:13px"
                @click="doDelete(record.id)"
              >
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import {
  deleteAppByAdmin,
  listAppVoByPageByAdmin,
  updateAppByAdmin,
} from '@/api/appController'
import { formatDateTime } from '@/utils/time'

const columns = [
  { title: 'id', dataIndex: 'id', width: 170 },
  { title: '封面', dataIndex: 'cover', width: 80 },
  { title: '应用名称', dataIndex: 'appName', width: 160 },
  { title: '初始提示词', dataIndex: 'initPrompt', ellipsis: true, width: 200 },
  { title: '代码类型', dataIndex: 'codeGenType', width: 100 },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '创建者', dataIndex: 'user', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 160 },
  { title: '更新时间', dataIndex: 'updateTime', width: 160 },
  { title: '操作', key: 'action', fixed: 'right', width: 200 },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  appName: undefined,
  userName: undefined,
  priority: undefined,
})

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`,
}))

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({ ...searchParams })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const doReset = () => {
  searchParams.appName = undefined
  searchParams.userName = undefined
  searchParams.priority = undefined
  searchParams.pageNum = 1
  fetchData()
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  searchParams.pageNum = pag.current ?? 1
  searchParams.pageSize = pag.pageSize ?? 10
  fetchData()
}

const doEdit = (id?: number) => {
  if (!id) return
  window.open(`/app/edit/${id}`, '_blank')
}

const doFeature = (record: API.AppVO) => {
  if (!record.id) return
  Modal.confirm({
    title: '确认将该应用设为精选？',
    content: '设置后应用优先级将变为 99',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      const res = await updateAppByAdmin({
        id: record.id,
        priority: 99,
      })
      if (res.data.code === 0 && res.data.data) {
        message.success('设置精选成功')
        await fetchData()
      } else {
        message.error('操作失败，' + res.data.message)
      }
    },
  })
}

const doDelete = (id?: number) => {
  if (!id) return
  Modal.confirm({
    title: '确认删除该应用吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      const res = await deleteAppByAdmin({ id })
      if (res.data.code === 0 && res.data.data) {
        message.success('删除成功')
        await fetchData()
      } else {
        message.error('删除失败，' + res.data.message)
      }
    },
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-manage-page {
  width: 100%;
}

.breadcrumb {
  margin-bottom: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.search-input {
  width: 200px;
}

.search-input-sm {
  width: 120px;
}

.search-actions {
  margin-left: auto;
  margin-right: 0;
}

.table-card {
  background: #fff;
}

.table-card :deep(.ant-table-cell) {
  text-align: center !important;
}

@media (max-width: 768px) {
  .search-actions {
    margin-left: 0;
  }
}
</style>
