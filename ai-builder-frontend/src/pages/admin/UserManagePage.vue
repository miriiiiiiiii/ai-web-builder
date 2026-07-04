<template>
  <div class="user-manage-page">
    <a-breadcrumb class="breadcrumb">
      <a-breadcrumb-item>
        <router-link to="/admin/userManage">管理页</router-link>
      </a-breadcrumb-item>
      <a-breadcrumb-item>用户管理</a-breadcrumb-item>
    </a-breadcrumb>

    <a-card class="search-card">
      <a-form layout="inline" class="search-form">
        <a-form-item label="关键词">
          <a-input
            v-model:value="searchKeyword"
            placeholder="搜索账号、用户昵称"
            allow-clear
            class="search-input"
            @press-enter="doSearch"
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

    <a-card title="用户信息" class="table-card">
      <a-table
        size="small"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-image :src="record.userAvatar" :width="60" />
          </template>
          <template v-else-if="column.dataIndex === 'gender'">
            <span v-if="record.gender === 1">男</span>
            <span v-else-if="record.gender === 0">女</span>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button danger size="small" style="font-size:13px" @click="doDelete(record.id)">删除</a-button>
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
import dayjs from 'dayjs'
import { deleteUser, listUserVoByPage } from '@/api/userController'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '邮箱',
    dataIndex: 'userEmail',
    align: 'center',
    hideInSearch: true,
  },
  {
    title: '昵称',
    dataIndex: 'nickName',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '性别',
    dataIndex: 'gender',
    align: 'center',
    valueEnum: {
      1: { text: '男' },
      0: { text: '女' },
    },
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)
const searchKeyword = ref('')

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索
const doSearch = () => {
  searchParams.pageNum = 1
  const keyword = searchKeyword.value.trim()
  searchParams.keyword = keyword || undefined
  fetchData()
}

// 重置
const doReset = () => {
  searchKeyword.value = ''
  searchParams.keyword = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 表格分页变化
const handleTableChange: TableProps['onChange'] = (pag) => {
  searchParams.pageNum = pag.current ?? 1
  searchParams.pageSize = pag.pageSize ?? 10
  fetchData()
}

// 删除用户
const doDelete = (id?: number) => {
  if (!id) {
    return
  }
  Modal.confirm({
    title: '确认删除该用户吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      const res = await deleteUser({ id })
      if (res.data.code === 0 && res.data.data) {
        message.success('删除成功')
        await fetchData()
      } else {
        message.error('删除失败，' + res.data.message)
      }
    },
  })
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-manage-page {
  width: 100%;
}

.breadcrumb {
  margin-bottom: 16px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 28px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.search-input {
  width: 280px;
}

.search-actions {
  margin-left: auto;
  margin-right: 0;
}

.table-card {
  background: #fff;
}

@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .search-actions {
    margin-left: 0;
  }

  .search-input {
    width: 100%;
  }
}

.table-card :deep(.ant-table-cell) {
  text-align: center !important;
}
</style>
