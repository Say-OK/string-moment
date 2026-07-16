import request from '../utils/request'

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/file/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传商品图片
export function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/file/upload/product', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传秒杀活动图片
export function uploadSeckillImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post('/file/upload/seckill', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}