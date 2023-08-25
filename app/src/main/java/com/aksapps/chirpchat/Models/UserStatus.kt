package com.aksapps.chirpchat.Models

class UserStatus {
    var name: String = ""
    var profileImage: String = ""
    var lastUpdated: Long = 0L
    var statuses: ArrayList<Status>? = null

    constructor()

    constructor(name: String, profileImage: String, lastUpdated: Long, statuses: ArrayList<Status>) {
        this.name = name
        this.profileImage = profileImage
        this.lastUpdated = lastUpdated
        this.statuses = statuses
    }
}