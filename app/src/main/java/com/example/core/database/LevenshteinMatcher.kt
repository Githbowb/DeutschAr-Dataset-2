package com.example.core.database

import kotlin.math.min

object LevenshteinMatcher {

    /**
     * Calculates the Levenshtein distance between two strings.
     * Complexity: O(min(m, n)) space, O(m * n) time.
     */
    fun computeDistance(s1: String, s2: String): Int {
        val str1 = s1.trim().lowercase()
        val str2 = s2.trim().lowercase()
        
        if (str1 == str2) return 0
        if (str1.isEmpty()) return str2.length
        if (str2.isEmpty()) return str1.length

        val len1 = str1.length
        val len2 = str2.length

        var prevRow = IntArray(len2 + 1) { it }
        var currRow = IntArray(len2 + 1)

        for (i in 0 until len1) {
            currRow[0] = i + 1
            val c1 = str1[i]
            for (j in 0 until len2) {
                val c2 = str2[j]
                val cost = if (c1 == c2) 0 else 1
                currRow[j + 1] = min(
                    min(currRow[j] + 1, prevRow[j + 1] + 1), // insertion, deletion
                    prevRow[j] + cost // substitution
                )
            }
            val temp = prevRow
            prevRow = currRow
            currRow = temp
        }

        return prevRow[len2]
    }

    /**
     * Finds matching words within the maximum Levenshtein distance (default max 2).
     */
    fun findFuzzyMatches(
        query: String,
        wordList: List<WordEntity>,
        maxDistance: Int = 2,
        limit: Int = 5
    ): List<Pair<WordEntity, Int>> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        return wordList
            .map { word ->
                // Check distance against german word and arabic translation
                val deDist = computeDistance(q, word.germanWord)
                val arDist = if (word.arabicTranslation.isNotEmpty()) {
                    // Check direct or sub-tokens
                    val words = word.arabicTranslation.split('/', ' ', '،', '-')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    val minTokenDist = words.minOfOrNull { computeDistance(q, it) } ?: 99
                    min(computeDistance(q, word.arabicTranslation), minTokenDist)
                } else {
                    99
                }
                
                val bestDist = min(deDist, arDist)
                Pair(word, bestDist)
            }
            .filter { it.second <= maxDistance }
            .sortedWith(compareBy({ it.second }, { it.first.frequencyRank ?: 9999 }))
            .take(limit)
    }
}
