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

class PortfolioRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    fun getPortfolioHoldingsFlow(): Flow<List<PortfolioHolding>>? {
        val userId = getCurrentUserId() ?: return null
        return firestore.collection("users").document(userId).collection("portfolio")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(PortfolioHolding::class.java) }
    }

    // --- THIS IS THE NEW, CORRECTED FUNCTION ---
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
                        // Stock exists, update it
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
                        // Stock doesn't exist, create a new one
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