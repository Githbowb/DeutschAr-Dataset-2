package com.example.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "german_verbs",
    indices = [
        Index(value = ["infinitive"], unique = true)
    ]
)
data class GermanVerbEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val infinitive: String,
    val auxiliary: String = "haben", // haben or sein
    val presentIch: String? = null,
    val presentDu: String? = null,
    val presentErSieEs: String? = null,
    val presentWir: String? = null,
    val presentIhr: String? = null,
    val presentSie: String? = null,
    val pastIch: String? = null,
    val pastDu: String? = null,
    val pastErSieEs: String? = null,
    val pastWir: String? = null,
    val pastIhr: String? = null,
    val pastSie: String? = null,
    val partizipZwei: String? = null,
    val konjunktivZwei: String? = null,
    val imperativSingularForm: String? = null,
    val imperativPluralForm: String? = null,
    val isSeparable: Boolean = false,
    val prefix: String? = null
) {
    // Aliases and convenience properties for German grammar nomenclature
    val hilfsverb: String get() = auxiliary
    val prasensIch: String? get() = presentIch
    val prasensDu: String? get() = presentDu
    val prasensErSieEs: String? get() = presentErSieEs
    val prateritumIch: String? get() = pastIch
    val konjunktivZweiIch: String? get() = konjunktivZwei
    val imperativSingular: String? get() = imperativSingularForm
    val imperativPlural: String? get() = imperativPluralForm

    constructor(
        id: Long = 0,
        infinitive: String,
        prasensIch: String? = null,
        prasensDu: String? = null,
        prasensErSieEs: String? = null,
        prateritumIch: String? = null,
        partizipZwei: String? = null,
        konjunktivZweiIch: String? = null,
        imperativSingular: String? = null,
        imperativPlural: String? = null,
        hilfsverb: String = "haben"
    ) : this(
        id = id,
        infinitive = infinitive,
        auxiliary = hilfsverb,
        presentIch = prasensIch,
        presentDu = prasensDu,
        presentErSieEs = prasensErSieEs,
        pastIch = prateritumIch,
        partizipZwei = partizipZwei,
        konjunktivZwei = konjunktivZweiIch,
        imperativSingularForm = imperativSingular,
        imperativPluralForm = imperativPlural
    )
}
