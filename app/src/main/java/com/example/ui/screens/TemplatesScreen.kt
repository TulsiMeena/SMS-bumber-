package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SmsTemplate
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.SmsBlastViewModel

@Composable
fun TemplatesScreen(
    viewModel: SmsBlastViewModel,
    onTemplateLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = remember(templates) {
        listOf("All") + templates.map { it.category }.distinct()
    }

    val filteredTemplates = remember(templates, selectedCategory) {
        if (selectedCategory == null || selectedCategory == "All") {
            templates
        } else {
            templates.filter { it.category == selectedCategory }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Message Template Library",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryDark
                )
                Text(
                    text = "Choose a stress testing preset or broadcast payload to load into the blast engine.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            // Category Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = (selectedCategory == null && category == "All") || selectedCategory == category
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = if (category == "All") null else category }
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else DarkSurfaceBorder,
                                    RoundedCornerShape(20.dp)
                                ),
                            color = if (isSelected) CyberCyan.copy(alpha = 0.3f) else DarkSurfaceElevated
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else TextSecondaryDark
                            )
                        }
                    }
                }
            }

            // Templates List
            items(filteredTemplates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onLoad = {
                        viewModel.loadTemplate(template)
                        onTemplateLoaded()
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(template) },
                    onDelete = if (!template.isSystemDefault) {
                        { viewModel.deleteTemplate(template) }
                    } else null
                )
            }
        }

        // Add Custom Template FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_template_fab"),
            containerColor = NeonOrange,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Template")
        }
    }

    if (showAddDialog) {
        AddTemplateDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, category, body, count, delay ->
                viewModel.addCustomTemplate(title, category, body, count, delay)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: SmsTemplate,
    onLoad: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberCyan.copy(alpha = 0.2f),
                        modifier = Modifier.border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = template.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = template.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimaryDark
                    )
                }

                Row {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (template.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (template.isFavorite) NeonOrange else TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body text box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                color = DarkBg
            ) {
                Text(
                    text = template.templateBody,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimaryDark,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = BrightAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${template.defaultCount} pkts",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${template.defaultDelaySeconds}s delay",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Button(
                    onClick = onLoad,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text(
                        text = "LOAD TO BLASTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, body: String, count: Int, delay: Float) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom") }
    var body by remember { mutableStateOf("Alert #{index}: Critical code {code} at {time}") }
    var count by remember { mutableStateOf("5") }
    var delay by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Custom Blast Template", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Template Name") },
                    placeholder = { Text("e.g. VIP Broadcast") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Alerts, Tests, Promo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message Body with {tags}") },
                    modifier = Modifier.height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = count,
                        onValueChange = { count = it },
                        label = { Text("Count") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                    OutlinedTextField(
                        value = delay,
                        onValueChange = { delay = it },
                        label = { Text("Delay (sec)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        title,
                        category,
                        body,
                        count.toIntOrNull() ?: 5,
                        delay.toFloatOrNull() ?: 1.0f
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
            ) {
                Text("Save Template", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurface
    )
}
