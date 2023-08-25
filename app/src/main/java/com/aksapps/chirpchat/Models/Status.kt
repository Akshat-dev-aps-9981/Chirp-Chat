package com.aksapps.chirpchat.Models
class Status {
    var imageUrl: String? = null
    var timeStamp: Long? = null

    constructor() {
        // require empty constructor.
    }
    constructor(imageUrl: String, timeStamp: Long) {
        this.imageUrl = imageUrl
        this.timeStamp = timeStamp
    }
}