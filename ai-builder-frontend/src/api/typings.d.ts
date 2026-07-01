declare namespace API {
  type AccountLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type EmailLoginRequest = {
    userEmail?: string
    code?: string
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOParams = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userEmail?: string
    nickName?: string
    userAvatar?: string
    userProfile?: string
    gender?: number
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userEmail?: string
    nickName?: string
    userAvatar?: string
    userProfile?: string
    gender?: number
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userAccount?: string
    userEmail?: string
    nickName?: string
    userAvatar?: string
    userProfile?: string
    gender?: number
    userRole?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    keyword?: string
  }

  type UserRegisterRequest = {
    nickName?: string
    userAccount?: string
    userEmail?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userEmail?: string
    nickName?: string
    userAvatar?: string
    userProfile?: string
    gender?: number
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userEmail?: string
    nickName?: string
    userAvatar?: string
    userProfile?: string
    gender?: number
    userRole?: string
    createTime?: string
  }
}
