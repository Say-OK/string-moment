import request from '../utils/request'

// 创建订单
export function createOrder(data) {
  return request.post('/order/create', data)
}

// 获取订单列表
export function getOrderList(params) {
  return request.get('/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/order/detail/${id}`)
}

// 取消订单
export function cancelOrder(id) {
  return request.post(`/order/cancel/${id}`)
}

// 支付订单
export function payOrder(id) {
  return request.post(`/order/pay/${id}`)
}

// 确认收货
export function confirmOrder(id) {
  return request.post(`/order/confirm/${id}`)
}