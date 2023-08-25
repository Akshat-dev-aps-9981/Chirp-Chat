package com.aksapps.chirpchat.Models

class Message {
    var messageId: String? = ""
    var message: String? = ""
    var senderId: String? = ""
    var timestamp: Long? = 0L
    var feeling: Int? = 0
    var imageUrl: String? = null

    constructor()

    constructor(messageId: String) {
        this.messageId = messageId
    }

    constructor(message: String, senderId: String, timestamp: Long, feeling: Int) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
        this.feeling = feeling
    }

}