package com.apexinvest.app.data.repository

import com.apexinvest.app.data.NotificationEntity
import com.apexinvest.app.db.NotificationDao
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun deleteNotification(id: Int) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearAll() {
        notificationDao.clearAllNotifications()
    }
}
