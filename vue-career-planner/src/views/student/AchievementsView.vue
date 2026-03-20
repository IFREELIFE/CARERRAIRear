<template>
  <div class="achievements-view">
    <h2>我的成就</h2>
    
    <el-card shadow="hover" class="achievements-card">
      <div class="achievements-header">
        <div class="achievement-stats">
          <div class="stat-item">
            <span class="stat-value">1</span>
            <span class="stat-label">总成就</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">7</span>
            <span class="stat-label">连续签到</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">250</span>
            <span class="stat-label">积分</span>
          </div>
        </div>
        
        <div class="check-in">
          <el-button type="primary">
            每日签到
          </el-button>
        </div>
      </div>
      
      <div class="achievements-list">
        <h3>已获得成就</h3>
        <div class="achievements-grid">
          <div class="achievement-item completed">
            <div class="achievement-icon completed">
              <el-icon :size="32">
                <Star />
              </el-icon>
            </div>
            <div class="achievement-info">
              <h4>探索者</h4>
              <p class="achievement-desc">浏览10个招聘岗位</p>
              <div class="achievement-date">
                <span>获得于: 2026-03-05</span>
              </div>
            </div>
          </div>
          <div class="achievement-item">
            <div class="achievement-icon">
              <el-icon :size="32">
                <Calendar />
              </el-icon>
            </div>
            <div class="achievement-info">
              <h4>勤奋</h4>
              <p class="achievement-desc">连续签到10天</p>
              <div class="achievement-progress">
                <el-progress :percentage="70" :stroke-width="8" />
                <span class="progress-text">7/10</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="badges-section">
        <h3>我的徽章</h3>
        <div class="badges-grid">
          <div class="badge-item">
            <div class="badge-icon">
              <el-avatar :size="60" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=beginner%20badge%20blue&image_size=square" />
            </div>
            <span class="badge-name">新手上路</span>
          </div>
          <div class="badge-item">
            <div class="badge-icon">
              <el-avatar :size="60" src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=explorer%20badge%20green&image_size=square" />
            </div>
            <span class="badge-name">探索者</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Star, Calendar } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.achievements-view {
  padding: 20px;
  
  h2 {
    margin: 0 0 20px;
    color: #333;
    font-size: 24px;
    font-weight: 600;
  }
  
  .achievements-card {
    border-radius: 12px;
    
    .achievements-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;
      
      .achievement-stats {
        display: flex;
        gap: 40px;
        
        .stat-item {
          text-align: center;
          
          .stat-value {
            display: block;
            font-size: 24px;
            font-weight: 700;
            color: #1890ff;
            margin-bottom: 4px;
          }
          
          .stat-label {
            font-size: 14px;
            color: #666;
          }
        }
      }
    }
    
    .achievements-list {
      margin-bottom: 30px;
      
      h3 {
        margin: 0 0 16px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
      
      .achievements-grid {
        display: flex;
        flex-direction: column;
        gap: 16px;
        
        .achievement-item {
          display: flex;
          align-items: flex-start;
          gap: 16px;
          padding: 20px;
          border: 1px solid #f0f0f0;
          border-radius: 12px;
          transition: all 0.3s ease;
          
          &:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
          }
          
          &.completed {
            border-color: #e6f7ff;
            background-color: #f6ffed;
          }
          
          .achievement-icon {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f5f7fa;
            color: #999;
            flex-shrink: 0;
            transition: all 0.3s ease;
            
            &.completed {
              background-color: #e6f7ff;
              color: #1890ff;
            }
          }
          
          .achievement-info {
            flex: 1;
            
            h4 {
              margin: 0 0 8px;
              font-size: 16px;
              font-weight: 600;
              color: #333;
            }
            
            .achievement-desc {
              margin: 0 0 12px;
              font-size: 14px;
              color: #666;
            }
            
            .achievement-progress {
              .progress-text {
                display: block;
                font-size: 12px;
                color: #999;
                margin-top: 4px;
                text-align: right;
              }
            }
            
            .achievement-date {
              font-size: 12px;
              color: #999;
              margin-top: 8px;
            }
          }
        }
      }
    }
    
    .badges-section {
      
      h3 {
        margin: 0 0 16px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
      
      .badges-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
        gap: 20px;
        
        .badge-item {
          text-align: center;
          
          .badge-icon {
            margin-bottom: 8px;
          }
          
          .badge-name {
            display: block;
            font-size: 14px;
            color: #666;
          }
        }
      }
    }
  }
}
</style>