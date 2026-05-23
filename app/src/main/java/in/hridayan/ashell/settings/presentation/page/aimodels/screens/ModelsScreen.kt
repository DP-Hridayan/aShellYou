@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.aimodels.screens

import android.os.StatFs
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.ai.domain.model.AiModel
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiModelManagerViewModel
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.page.aimodels.components.modelmanager.ModelCard
import `in`.hridayan.ashell.settings.presentation.page.aimodels.components.modelmanager.StorageUsageBar

enum class ModelCategory(val titleResId: Int) {
    TINY(R.string.model_category_tiny),
    SMALL(R.string.model_category_small),
    MEDIUM(R.string.model_category_medium),
    LARGE(R.string.model_category_large);

    companion object {
        fun fromModel(model: AiModel): ModelCategory {
            val size = model.sizeBytes
            return when {
                size < 300_000_000L -> TINY
                size in 300_000_000L..600_000_000L -> SMALL
                size in 600_000_000L..1_200_000_000L -> MEDIUM
                else -> LARGE
            }
        }
    }
}

@Composable
fun ModelsScreen(
    modifier: Modifier = Modifier,
    viewModel: AiModelManagerViewModel = hiltViewModel(),
) {
    val models by viewModel.models.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()

    val context = LocalContext.current

    val totalAvailableBytes = remember {
        runCatching {
            val stat = StatFs(context.filesDir.path)
            stat.availableBytes
        }.getOrElse {
            Log.e("ModelsScreen", "Failed to get storage", it)
            0L
        }
    }

    val listState = rememberLazyListState()

    val modelsByCategory = remember(models) {
        models.groupBy { ModelCategory.fromModel(it.model) }
    }

    val categoriesInOrder = listOf(
        ModelCategory.TINY,
        ModelCategory.SMALL,
        ModelCategory.MEDIUM,
        ModelCategory.LARGE
    )

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.models),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
            ) {
                // Storage usage bar
                item {
                    StorageUsageBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        usedBytes = storageUsage,
                        totalAvailableBytes = totalAvailableBytes
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Categorized model lists
                categoriesInOrder.forEach { category ->
                    val modelsInCat = modelsByCategory[category] ?: emptyList()
                    if (modelsInCat.isNotEmpty()) {
                        item(key = "category_${category.name}") {
                            Text(
                                text = stringResource(category.titleResId),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 24.dp,
                                    bottom = 8.dp,
                                ),
                            )
                        }

                        items(
                            items = modelsInCat,
                            key = { it.model.id }
                        ) { modelState ->
                            ModelCard(
                                state = modelState,
                                onDownload = { viewModel.downloadModel(modelState.model.id) },
                                onCancelDownload = { viewModel.cancelDownload(modelState.model.id) },
                                onDelete = { viewModel.deleteModel(modelState.model.id) },
                                onSelect = { viewModel.selectModel(modelState.model.id) },
                                onDismissError = { viewModel.dismissError(modelState.model.id) }
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(32.dp))
                }
            }
        },
    )
}
