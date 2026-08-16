import { post } from '../../utils/request'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
