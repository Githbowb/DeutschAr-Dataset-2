package com.example.feature.cheatsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.Atmosphere
import com.example.ui.theme.ambientAtmosphere
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSubCardBg
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted

@Composable
fun CheatSheetScreen(
    uiState: CheatSheetUiState,
    onSelectCategory: (String) -> Unit,
    onToggleRuleExpansion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .ambientAtmosphere()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = DarkCardBg,
                labelColor = TextMuted,
                selectedContainerColor = PrimaryAccent.copy(alpha = 0.2f),
                selectedLabelColor = PrimaryAccent
            )
            val chipBorder = FilterChipDefaults.filterChipBorder(
                borderColor = DarkCardBorder,
                selectedBorderColor = PrimaryAccent.copy(alpha = 0.5f),
                enabled = true,
                selected = false
            )

            GermanGrammarData.categories.forEach { cat ->
                FilterChip(
                    selected = uiState.selectedCategory == cat.id,
                    onClick = { onSelectCategory(cat.id) },
                    label = {
                        Text(
                            text = "${cat.icon} ${cat.titleAr}",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = chipColors,
                    border = chipBorder,
                    modifier = Modifier.testTag("filter_cat_${cat.id.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List of grammar rules
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cheatsheet_rules_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = uiState.rules,
                key = { it.id }
            ) { rule ->
                val isExpanded = uiState.expandedRuleId == rule.id
                GrammarRuleCard(
                    rule = rule,
                    isExpanded = isExpanded,
                    onToggleExpand = { onToggleRuleExpansion(rule.id) }
                )
            }
        }
    }
}

@Composable
private fun GrammarRuleCard(
    rule: GrammarRule,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("rule_card_${rule.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Atmosphere.glassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.titleAr,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PrimaryAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rule.titleDe,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SecondaryAccent
                    )
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.testTag("expand_rule_btn_${rule.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "طي" else "توسيع",
                        tint = PrimaryAccent
                    )
                }
            }

            Text(
                text = rule.summaryAr,
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 6.dp)
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = DarkCardBorder)

                    // 1. Explanations
                    rule.explanations.forEach { exp ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "📌 " + exp.heading,
                                fontFamily = CairoFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Text(
                                text = exp.content,
                                fontFamily = CairoFontFamily,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // 2. Grammar Tables
                    rule.tables.forEach { table ->
                        GrammarTableView(table = table)
                    }

                    // 3. Examples
                    if (rule.examples.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "📖 أمثلة توضيحية:",
                                fontFamily = CairoFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAccent
                            )
                            rule.examples.forEach { ex ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = DarkSubCardBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = ex.german,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMain
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = ex.arabic,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 13.sp,
                                            color = PrimaryAccent
                                        )
                                        if (ex.explanation.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "👈 " + ex.explanation,
                                                fontFamily = CairoFontFamily,
                                                fontSize = 12.sp,
                                                color = AmberAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Important Tips
                    if (rule.importantTips.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AmberAccent.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "نصائح مهمة:",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent
                                    )
                                }
                                rule.importantTips.forEach { tip ->
                                    Text(
                                        text = tip,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        color = TextMain
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarTableView(
    table: GrammarTable,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "📊 " + table.title,
            fontFamily = CairoFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryAccent
        )

        // Horizontally scrollable table
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSubCardBg)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Table Header
                Row(
                    modifier = Modifier
                        .background(
                            PrimaryAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                ) {
                    table.headers.forEach { header ->
                        Text(
                            text = header,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent,
                            modifier = Modifier
                                .width(115.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                // Table Rows
                table.rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (index % 2 == 0) Color.Transparent else Color(0x08FFFFFF),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 6.dp, horizontal = 6.dp)
                    ) {
                        row.forEach { cell ->
                            Text(
                                text = cell,
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                color = TextMain,
                                modifier = Modifier
                                    .width(115.dp)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
