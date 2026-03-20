<template>
  <div class="questionnaire-container">
    <div class="questionnaire-card">
      <div class="questionnaire-header">
        <h1>技术技能问卷</h1>
        <p>请填写您所学过的技术和技能，以便我们为您提供更精准的职业规划服务。</p>
      </div>
      <el-form :model="form" label-width="100px">
        <!-- 编程语言 -->
        <el-form-item label="编程语言">
          <el-checkbox-group v-model="form.programmingLanguages">
            <el-checkbox label="JavaScript">JavaScript</el-checkbox>
            <el-checkbox label="TypeScript">TypeScript</el-checkbox>
            <el-checkbox label="Python">Python</el-checkbox>
            <el-checkbox label="Java">Java</el-checkbox>
            <el-checkbox label="C++">C++</el-checkbox>
            <el-checkbox label="C#">C#</el-checkbox>
            <el-checkbox label="Go">Go</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
          <el-input v-if="form.programmingLanguages.includes('其他')" v-model="form.otherProgrammingLanguage" placeholder="请输入其他编程语言" class="mt-2" />
        </el-form-item>

        <!-- 前端技术 -->
        <el-form-item label="前端技术">
          <el-checkbox-group v-model="form.frontendTechnologies">
            <el-checkbox label="HTML/CSS">HTML/CSS</el-checkbox>
            <el-checkbox label="Vue">Vue</el-checkbox>
            <el-checkbox label="React">React</el-checkbox>
            <el-checkbox label="Angular">Angular</el-checkbox>
            <el-checkbox label="Node.js">Node.js</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
          <el-input v-if="form.frontendTechnologies.includes('其他')" v-model="form.otherFrontendTechnology" placeholder="请输入其他前端技术" class="mt-2" />
        </el-form-item>

        <!-- 后端技术 -->
        <el-form-item label="后端技术">
          <el-checkbox-group v-model="form.backendTechnologies">
            <el-checkbox label="Spring Boot">Spring Boot</el-checkbox>
            <el-checkbox label="Express">Express</el-checkbox>
            <el-checkbox label="Django">Django</el-checkbox>
            <el-checkbox label="Flask">Flask</el-checkbox>
            <el-checkbox label="ASP.NET">ASP.NET</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
          <el-input v-if="form.backendTechnologies.includes('其他')" v-model="form.otherBackendTechnology" placeholder="请输入其他后端技术" class="mt-2" />
        </el-form-item>

        <!-- 数据库 -->
        <el-form-item label="数据库">
          <el-checkbox-group v-model="form.databases">
            <el-checkbox label="MySQL">MySQL</el-checkbox>
            <el-checkbox label="PostgreSQL">PostgreSQL</el-checkbox>
            <el-checkbox label="MongoDB">MongoDB</el-checkbox>
            <el-checkbox label="Redis">Redis</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
          <el-input v-if="form.databases.includes('其他')" v-model="form.otherDatabase" placeholder="请输入其他数据库" class="mt-2" />
        </el-form-item>

        <!-- 工具和框架 -->
        <el-form-item label="工具和框架">
          <el-checkbox-group v-model="form.tools">
            <el-checkbox label="Git">Git</el-checkbox>
            <el-checkbox label="Docker">Docker</el-checkbox>
            <el-checkbox label="Kubernetes">Kubernetes</el-checkbox>
            <el-checkbox label="Webpack">Webpack</el-checkbox>
            <el-checkbox label="Vite">Vite</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
          <el-input v-if="form.tools.includes('其他')" v-model="form.otherTool" placeholder="请输入其他工具和框架" class="mt-2" />
        </el-form-item>

        <!-- 技能水平 -->
        <el-form-item label="技能水平">
          <el-radio-group v-model="form.skillLevel">
            <el-radio label="初级">初级</el-radio>
            <el-radio label="中级">中级</el-radio>
            <el-radio label="高级">高级</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 项目经验 -->
        <el-form-item label="项目经验">
          <el-input
            v-model="form.projectExperience"
            type="textarea"
            :rows="4"
            placeholder="请简要描述您的项目经验，包括项目类型、使用的技术栈等"
          />
        </el-form-item>

        <!-- 证书 -->
        <el-form-item label="证书">
          <el-input
            v-model="form.certifications"
            type="textarea"
            :rows="2"
            placeholder="请列出您获得的相关证书"
          />
        </el-form-item>
      </el-form>
      <div class="questionnaire-actions">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleNext">下一步</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const $router = useRouter()

const form = ref({
  programmingLanguages: [] as string[],
  otherProgrammingLanguage: '',
  frontendTechnologies: [] as string[],
  otherFrontendTechnology: '',
  backendTechnologies: [] as string[],
  otherBackendTechnology: '',
  databases: [] as string[],
  otherDatabase: '',
  tools: [] as string[],
  otherTool: '',
  skillLevel: '',
  projectExperience: '',
  certifications: ''
})

const handleBack = () => {
  $router.push('/student/welcome')
}

const handleNext = () => {
  // 保存技能信息到本地存储
  localStorage.setItem('skillsQuestionnaire', JSON.stringify(form.value))
  // 跳转到MBTI测试页面
  $router.push('/student/questionnaire/mbti')
}
</script>

<style scoped>
.questionnaire-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  overflow-y: auto;
}

.questionnaire-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 40px;
  max-width: 800px;
  width: 100%;
  backdrop-filter: blur(10px);
}

.questionnaire-header {
  margin-bottom: 30px;
  text-align: center;
}

.questionnaire-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.questionnaire-header p {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.el-form {
  margin-bottom: 30px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.el-checkbox {
  margin-right: 0;
}

.mt-2 {
  margin-top: 8px;
}

.questionnaire-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 40px;
}

.questionnaire-actions .el-button {
  min-width: 120px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.questionnaire-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.questionnaire-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4391 100%);
}

@media (max-width: 768px) {
  .questionnaire-card {
    padding: 30px 20px;
  }
  
  .questionnaire-header h1 {
    font-size: 20px;
  }
  
  .questionnaire-actions {
    flex-direction: column;
  }
  
  .questionnaire-actions .el-button {
    width: 100%;
  }
}
</style>