package com.example.challenge_letssign.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge_letssign.ui.theme.LetsSignColors

enum class LetsSignBottomNavTab(val label: String) {
    DOCUMENTS("Documentos"),
    SIGNATURES("Assinaturas"),
    PROFILE("Perfil")
}

@Composable
fun LetsSignBottomNavigationBar(
    selectedTab: LetsSignBottomNavTab,
    onTabSelected: (LetsSignBottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LetsSignColors.Line)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LetsSignBottomNavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val iconColor = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body
                val textColor = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isSelected) LetsSignColors.VioletSoft else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (tab) {
                            LetsSignBottomNavTab.DOCUMENTS -> DocumentFileIcon(color = iconColor, size = 22.dp)
                            LetsSignBottomNavTab.SIGNATURES -> SignaturePenIcon(color = iconColor, size = 22.dp)
                            LetsSignBottomNavTab.PROFILE -> UserProfileIcon(color = iconColor, size = 22.dp)
                        }
                    }
                    Text(
                        text = tab.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun LetsSignWizardStepper(
    currentStep: Int,
    totalSteps: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        when {
                            isCurrent -> LetsSignColors.VioletSoft
                            else -> Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            when {
                                isCompleted -> LetsSignColors.Primary
                                else -> Color.White
                            }
                        )
                        .border(
                            width = if (isCurrent) 2.dp else 1.25.dp,
                            color = when {
                                isCompleted || isCurrent -> LetsSignColors.Primary
                                else -> LetsSignColors.Line
                            },
                            shape = RoundedCornerShape(999.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        CheckIcon(color = Color.White, size = 15.dp)
                    } else {
                        Text(
                            text = step.toString(),
                            color = if (isCurrent) LetsSignColors.Primary else LetsSignColors.Muted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (step < currentStep) LetsSignColors.Primary else LetsSignColors.Line
                        )
                )
            }
        }
    }
}
