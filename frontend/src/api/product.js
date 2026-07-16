import request from '../utils/request'

// 获取商品列表
export function getProductList(params) {
  return request.get('/product/list', { params })
}

// 获取商品详情
export function getProductDetail(id) {
  return request.get(`/product/detail/${id}`)
}

// 获取商品分类列表
export function getCategoryList() {
  return request.get('/product/categories')
}