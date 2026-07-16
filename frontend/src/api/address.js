import request from '../utils/request'

// 获取地址列表
export function getAddressList() {
  return request.get('/address/list')
}

// 添加地址
export function addAddress(data) {
  return request.post('/address/add', data)
}

// 更新地址
export function updateAddress(data) {
  return request.put('/address/update', data)
}

// 删除地址
export function deleteAddress(id) {
  return request.delete(`/address/delete/${id}`)
}

// 设置默认地址
export function setDefaultAddress(id) {
  return request.put(`/address/set-default/${id}`)
}