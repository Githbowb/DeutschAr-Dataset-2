package com.example.feature.search

import org.junit.Assert.assertEquals
import org.junit.Test

class InputClassifierTest {

    @Test
    fun testClassification() {
        assertEquals(InputType.GERMAN_WORD, InputClassifier.classify("Haus"))
        assertEquals(InputType.ARABIC_WORD, InputClassifier.classify("بيت"))
        assertEquals(InputType.SENTENCE_OR_QUESTION, InputClassifier.classify("Ich gehe nach Hause"))
        assertEquals(InputType.SENTENCE_OR_QUESTION, InputClassifier.classify("Wie geht es dir?"))
        assertEquals(InputType.SENTENCE_OR_QUESTION, InputClassifier.classify("كيف حالك؟"))
    }
}
