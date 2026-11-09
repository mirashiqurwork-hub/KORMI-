package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company)

    @Query("SELECT * FROM companies")
    fun getAllCompanies(): Flow<List<Company>>

    // নতুন: পালা চেক করার জন্য লিস্ট
    @Query("SELECT * FROM companies")
    suspend fun getAllCompaniesSync(): List<Company>

    @Query("SELECT * FROM companies WHERE companyId = :id")
    suspend fun getCompanyById(id: Int): Company?

    @Query("DELETE FROM companies WHERE companyId = :id")
    suspend fun deleteCompanyById(id: Int)
}
