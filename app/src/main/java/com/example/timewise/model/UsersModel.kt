package com.example.timewise.model

import java.io.Serializable

data class UsersModel (
    var id: Int = 0,
    var userName: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var password: String? = null
): Serializable