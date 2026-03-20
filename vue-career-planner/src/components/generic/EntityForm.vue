<template>
  <el-form
    :model="formData"
    :rules="rules"
    ref="formRef"
    label-width="120px"
    @submit.prevent="handleSubmit"
  >
    <el-form-item
      v-for="field in fields"
      :key="field.prop"
      :label="field.label"
      :prop="field.prop"
      :required="field.required"
    >
      <!-- 输入框 -->
      <el-input
        v-if="field.type === 'input'"
        v-model="formData[field.prop]"
        :placeholder="field.placeholder"
        :disabled="field.disabled"
        :maxlength="field.maxlength"
      />

      <!-- 密码框 -->
      <el-input
        v-else-if="field.type === 'password'"
        v-model="formData[field.prop]"
        type="password"
        :placeholder="field.placeholder"
        :disabled="field.disabled"
        show-password
      />

      <!-- 文本域 -->
      <el-input
        v-else-if="field.type === 'textarea'"
        v-model="formData[field.prop]"
        type="textarea"
        :placeholder="field.placeholder"
        :disabled="field.disabled"
        :rows="field.rows || 3"
        :maxlength="field.maxlength"
      />

      <!-- 下拉选择 -->
      <el-select
        v-else-if="field.type === 'select'"
        v-model="formData[field.prop]"
        :placeholder="field.placeholder"
        :disabled="field.disabled"
      >
        <el-option
          v-for="option in field.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <!-- 日期选择器 -->
      <el-date-picker
        v-else-if="field.type === 'date'"
        v-model="formData[field.prop]"
        :type="field.dateType || 'date'"
        :placeholder="field.placeholder"
        :disabled="field.disabled"
        style="width: 100%"
      />

      <!-- 开关 -->
      <el-switch
        v-else-if="field.type === 'switch'"
        v-model="formData[field.prop]"
        :disabled="field.disabled"
      />

      <!-- 单选框组 -->
      <el-radio-group
        v-else-if="field.type === 'radio'"
        v-model="formData[field.prop]"
        :disabled="field.disabled"
      >
        <el-radio
          v-for="option in field.options"
          :key="option.value"
          :label="option.value"
        >
          {{ option.label }}
        </el-radio>
      </el-radio-group>

      <!-- 复选框组 -->
      <el-checkbox-group
        v-else-if="field.type === 'checkbox'"
        v-model="formData[field.prop]"
        :disabled="field.disabled"
      >
        <el-checkbox
          v-for="option in field.options"
          :key="option.value"
          :label="option.value"
        >
          {{ option.label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-form-item>

    <!-- 操作按钮 -->
    <el-form-item>
      <el-button type="primary" native-type="submit">提交</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'

interface Option {
  label: string
  value: any
}

interface Field {
  prop: string
  label: string
  type: 'input' | 'password' | 'textarea' | 'select' | 'date' | 'switch' | 'radio' | 'checkbox'
  required?: boolean
  disabled?: boolean
  placeholder?: string
  maxlength?: number
  rows?: number
  options?: Option[]
  dateType?: 'date' | 'datetime' | 'daterange' | 'datetimerange'
}

interface Props {
  model: any
  fields: Field[]
  rules: any
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'submit', data: any): void
  (e: 'reset'): void
}>()

const formRef = ref()
const formData = reactive({ ...props.model })

// 监听model变化，更新formData
watch(
  () => props.model,
  (newModel) => {
    Object.assign(formData, newModel)
  },
  { deep: true }
)

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    emit('submit', { ...formData })
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('reset')
}
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>