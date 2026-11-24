package com.dyna.studenthub.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.dyna.studenthub.R
import com.dyna.studenthub.domain.CompanionMood
import com.dyna.studenthub.domain.CompanionAnimal

@Composable
fun CompanionView(
    mood: CompanionMood,
    message: String,
    companionId: String = "cat",
    companionName: String = "Mochi",
    modifier: Modifier = Modifier,
) {
    val animal = CompanionAnimal.fromId(companionId)
    val imageRes = when (animal) {
        CompanionAnimal.CAT -> R.drawable.pet_cat
        CompanionAnimal.DOG -> R.drawable.pet_dog
        CompanionAnimal.RABBIT -> R.drawable.pet_rabbit
        CompanionAnimal.FOX -> R.drawable.pet_fox
        CompanionAnimal.PANDA -> R.drawable.pet_panda
        CompanionAnimal.CAPYBARA -> R.drawable.pet_capybara
    }
    val floatY by animateFloatAsState(
        targetValue = when (mood) {
            CompanionMood.HAPPY -> -3f
            CompanionMood.ALERT -> 2f
            CompanionMood.DEFAULT -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "companionMoodMotion",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = .94f),
        tonalElevation = 1.dp,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = companionName.uppercase(),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Image(
                painter = painterResource(imageRes),
                contentDescription = "Illustration du ${animal.label.lowercase()} $companionName",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(116.dp).offset(y = floatY.dp),
            )
        }
    }
}
