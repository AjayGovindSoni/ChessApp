package com.example.repository

import com.example.db.*
import com.example.model.otp_requests
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresOTPRepository : otp_requestReposiratory {
    override suspend fun findByEmail(email: String): otp_requests? = suspendTransaction {
        OtpDAO
            .find { OtpRequestTable.email eq email }
            .limit(1)
            .map(::OTPDaoToModel)
            .firstOrNull()
    }

    override suspend fun save(otpRequests: otp_requests): Boolean {
        try {
            suspendTransaction {
                OtpRequestTable.deleteWhere { OtpRequestTable.email eq otpRequests.email }

                OtpDAO.new() {
                    email = otpRequests.email
                    otp = otpRequests.otp
                }
            }
        } catch (ex: Exception) {
            return false
        }
        return true
    }


    override suspend fun delete() {
        OtpRequestTable.deleteWhere {
            expiresAt less CurrentDateTime
        }
    }

    override suspend fun remove(email: String) {
        suspendTransaction {
            OtpRequestTable.deleteWhere { OtpRequestTable.email eq email }
        }
    }
}