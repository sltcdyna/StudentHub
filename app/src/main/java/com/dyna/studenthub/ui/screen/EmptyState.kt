package com.dyna.studenthub.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dyna.studenthub.R
import com.dyna.studenthub.domain.CompanionAnimal

val LocalCompanionId = compositionLocalOf { CompanionAnimal.CAT.id }

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    val animal = CompanionAnimal.fromId(LocalCompanionId.current)
    val image = when (animal) {
        CompanionAnimal.CAT -> R.drawable.pet_cat
        CompanionAnimal.DOG -> R.drawable.pet_dog
        CompanionAnimal.RABBIT -> R.drawable.pet_rabbit
        CompanionAnimal.FOX -> R.drawable.pet_fox
        CompanionAnimal.PANDA -> R.drawable.pet_panda
        CompanionAnimal.CAPYBARA -> R.drawable.pet_capybara
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = "Illustration du ${animal.label.lowercase()}",
            modifier = Modifier.size(112.dp),
            contentScale = ContentScale.Fit,
        )
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
