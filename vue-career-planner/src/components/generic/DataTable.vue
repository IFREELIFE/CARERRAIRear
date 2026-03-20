<template>
  <div class="data-table">
    <!-- 搜索和筛选 -->
    <div class="table-header" v-if="showHeader">
      <el-input
        v-model="searchQuery"
        placeholder="搜索"
        prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" style="width: 100%">
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :sortable="column.sortable"
      >
        <template v-if="column.formatter" #default="scope">
          {{ column.formatter(scope.row, scope.column, scope.row[column.prop], scope.$index) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" v-if="showActions">
        <template #default="scope">
          <el-button
            size="small"
            type="primary"
            @click="handleEdit(scope.row)"
            v-if="showEdit"
          >
            编辑
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row)"
            v-if="showDelete"
          >
            删除
          </el-button>
          <el-button
            size="small"
            @click="handleView(scope.row)"
            v-if="showView"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="table-footer" v-if="showPagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Column {
  prop: string
  label: string
  width?: string | number
  sortable?: boolean | string
  formatter?: (row: any, column: any, cellValue: any, index: number) => string
}

interface Props {
  data: any[]
  columns: Column[]
  total: number
  showHeader?: boolean
  showPagination?: boolean
  showActions?: boolean
  showEdit?: boolean
  showDelete?: boolean
  showView?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showHeader: true,
  showPagination: true,
  showActions: true,
  showEdit: true,
  showDelete: true,
  showView: true
})

const emit = defineEmits<{
  (e: 'search', query: string): void
  (e: 'pageChange', page: number, size: number): void
  (e: 'edit', row: any): void
  (e: 'delete', row: any): void
  (e: 'view', row: any): void
}>()

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const tableData = computed(() => props.data)

const handleSearch = () => {
  emit('search', searchQuery.value)
}

const resetSearch = () => {
  searchQuery.value = ''
  emit('search', '')
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  emit('pageChange', currentPage.value, size)
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  emit('pageChange', page, pageSize.value)
}

const handleEdit = (row: any) => {
  emit('edit', row)
}

const handleDelete = (row: any) => {
  emit('delete', row)
}

const handleView = (row: any) => {
  emit('view', row)
}
</script>

<style scoped>
.data-table {
  width: 100%;
}

.table-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 10px;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>