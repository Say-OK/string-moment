import request from '../utils/request'

/**
 * 获取秒杀活动列表
 * @param {Object} params - 查询参数
 * @param {number} params.status - 活动状态（可选：0-未开始，1-进行中，2-已结束）
 * @returns {Promise}
 */
export function getSeckillActivityList(params) {
  return request.get('/seckill/activity/list', { params })
}

/**
 * 获取秒杀活动详情
 * @param {number} id - 活动ID
 * @returns {Promise}
 */
export function getSeckillActivityDetail(id) {
  return request.get(`/seckill/activity/detail/${id}`)
}

/**
 * 检查用户秒杀资格
 * @param {number} activityId - 活动ID
 * @returns {Promise}
 */
export function checkSeckillQualification(activityId) {
  return request.get(`/seckill/qualification/${activityId}`)
}

/**
 * 执行秒杀
 * @param {Object} data - 请求参数
 * @param {number} data.seckillActivityId - 秒杀活动ID
 * @param {number} data.addressId - 收货地址ID
 * @returns {Promise}
 */
export function executeSeckill(data) {
  return request.post('/seckill/execute', data)
}