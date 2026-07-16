/**
 * 秒杀业务常量
 */
export const SeckillConstant = {
  // 秒杀活动状态
  ACTIVITY_NOT_STARTED: 0,  // 未开始
  ACTIVITY_ON_GOING: 1,     // 进行中
  ACTIVITY_ENDED: 2,        // 已结束

  // 秒杀资格校验码
  QUALIFY_CAN_SECKILL: 0,        // 可以秒杀
  QUALIFY_STOCK_LACK: 1,         // 库存不足
  QUALIFY_REPEAT_PURCHASE: 2,    // 已参与过
  QUALIFY_ACTIVITY_NOT_START: 3, // 活动未开始
  QUALIFY_ACTIVITY_ENDED: 4,     // 活动已结束

  // 秒杀执行结果状态
  RESULT_SUCCESS: 0,     // 秒杀成功
  RESULT_STOCK_LACK: 1,  // 库存不足
  RESULT_REPEAT: 2,      // 重复秒杀
  RESULT_NOT_START: 3,   // 活动未开始
  RESULT_END: 4,         // 活动已结束
  RESULT_ERROR: 5        // 参数错误/未知错误
}

/**
 * 获取活动状态文本
 */
export function getActivityStatusText(status) {
  const statusMap = {
    [SeckillConstant.ACTIVITY_NOT_STARTED]: '未开始',
    [SeckillConstant.ACTIVITY_ON_GOING]: '进行中',
    [SeckillConstant.ACTIVITY_ENDED]: '已结束'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取活动状态样式类名
 */
export function getActivityStatusClass(status) {
  const classMap = {
    [SeckillConstant.ACTIVITY_NOT_STARTED]: 'status-pending',
    [SeckillConstant.ACTIVITY_ON_GOING]: 'status-active',
    [SeckillConstant.ACTIVITY_ENDED]: 'status-ended'
  }
  return classMap[status] || ''
}

/**
 * 获取秒杀资格文本
 */
export function getQualificationText(qualification) {
  const qualificationMap = {
    [SeckillConstant.QUALIFY_CAN_SECKILL]: '可以参与秒杀',
    [SeckillConstant.QUALIFY_STOCK_LACK]: '库存不足',
    [SeckillConstant.QUALIFY_REPEAT_PURCHASE]: '您已参与过此活动',
    [SeckillConstant.QUALIFY_ACTIVITY_NOT_START]: '活动未开始',
    [SeckillConstant.QUALIFY_ACTIVITY_ENDED]: '活动已结束'
  }
  return qualificationMap[qualification] || '未知状态'
}

/**
 * 获取秒杀结果文本
 */
export function getSeckillResultText(result) {
  const resultMap = {
    [SeckillConstant.RESULT_SUCCESS]: '秒杀成功',
    [SeckillConstant.RESULT_STOCK_LACK]: '库存不足',
    [SeckillConstant.RESULT_REPEAT]: '重复秒杀',
    [SeckillConstant.RESULT_NOT_START]: '活动未开始',
    [SeckillConstant.RESULT_END]: '活动已结束',
    [SeckillConstant.RESULT_ERROR]: '参数错误'
  }
  return resultMap[result] || '未知错误'
}