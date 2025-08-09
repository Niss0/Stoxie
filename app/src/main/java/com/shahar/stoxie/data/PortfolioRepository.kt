package com.shahar.stoxie.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.snapshots
import com.shahar.stoxie.models.PortfolioHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for portfolio operations.
 * Manages user stock holdings with real-time updates and transaction safety.
 */
class PortfolioRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Gets current authenticated user ID with logging.
     * @return User ID or null if not authenticated
     */
    private fun getCurrentUserId(): String? {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w("PortfolioRepository", "No authenticated user found")
        }
        return userId
    }

    /**
     * Provides real-time flow of user's portfolio holdings.
     * @return Flow of portfolio holdings or null if user not authenticated
     */
    fun getPortfolioHoldingsFlow(): Flow<List<PortfolioHolding>>? {
        val userId = getCurrentUserId() ?: return null
        return firestore.collection("users").document(userId).collection("portfolio")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(PortfolioHolding::class.java) }
    }

    /**
     * Adds stock to portfolio with atomic transaction handling.
     * Updates existing holdings or creates new ones with proper cost averaging.
     * @param symbol Stock symbol (converted to uppercase)
     * @param companyName Company display name
     * @param newShares Number of shares to add
     * @param newPrice Price per share
     */
    suspend fun addStockToPortfolio(symbol: String, companyName: String, newShares: Double, newPrice: Double) {
        val userId = getCurrentUserId() ?: return
        val docRef = firestore.collection("users").document(userId)
            .collection("portfolio").document(symbol.uppercase())

        withContext(Dispatchers.IO) {
            try {
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(docRef)
                    val existingHolding = snapshot.toObject<PortfolioHolding>()

                    if (existingHolding != null) {
                        // Update existing position with cost averaging
                        val oldShares = existingHolding.shares
                        val oldAveragePrice = existingHolding.averageBuyPrice

                        val newTotalShares = oldShares + newShares
                        val newAveragePrice = ((oldShares * oldAveragePrice) + (newShares * newPrice)) / newTotalShares

                        val updatedHolding = existingHolding.copy(
                            shares = newTotalShares,
                            averageBuyPrice = newAveragePrice
                        )
                        transaction.set(docRef, updatedHolding)
                    } else {
                        // Create new position
                        val newHolding = PortfolioHolding(
                            symbol = symbol.uppercase(),
                            companyName = companyName,
                            shares = newShares,
                            averageBuyPrice = newPrice
                        )
                        transaction.set(docRef, newHolding)
                    }
                }.await()
            } catch (e: Exception) {
                Log.e("PortfolioRepository", "Error adding stock in transaction", e)
            }
        }
    }

    /**
     * Removes stock from user's portfolio.
     * @param symbol Stock symbol to remove
     */
    suspend fun removeStockFromPortfolio(symbol: String) {
        val userId = getCurrentUserId() ?: return
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(userId)
                    .collection("portfolio").document(symbol.uppercase())
                    .delete().await()
            } catch (e: Exception) {
                Log.e("PortfolioRepository", "Error removing stock from portfolio", e)
            }
        }
    }
}