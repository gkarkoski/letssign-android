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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge_letssign.ui.theme.LetsSignColors

/**
 * 4 itens da Navegação Inferior (Componentes.png & fluxo_telas.md):
 * Documentos, Minhas Assinaturas, Aguardando Terceiros, Contatos.
 * (Mantém também SIGNATURES e PROFILE para compatibilidade durante a transição até o Milestone 6).
 */
enum class LetsSignBottomNavTab(val label: String) {
    DOCUMENTS("Documentos"),
    MY_SIGNATURES("Minhas\nAssinaturas"),
    WAITING_OTHERS("Aguardando\nTerceiros"),
    CONTACTS("Contatos"),
    SIGNATURES("Contatos"),
    PROFILE("Perfil");

    companion object {
        val figmaPrimaryTabs: List<LetsSignBottomNavTab> = listOf(
            DOCUMENTS,
            MY_SIGNATURES,
            WAITING_OTHERS,
            CONTACTS
        )
    }
}

@Composable
fun LetsSignBottomNavigationBar(
    selectedTab: LetsSignBottomNavTab,
    onTabSelected: (LetsSignBottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<LetsSignBottomNavTab> = LetsSignBottomNavTab.figmaPrimaryTabs
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
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab ||
                    (tab == LetsSignBottomNavTab.CONTACTS && selectedTab == LetsSignBottomNavTab.SIGNATURES)
                val iconColor = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body
                val textColor = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
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
                            LetsSignBottomNavTab.MY_SIGNATURES -> SignaturePenIcon(color = iconColor, size = 22.dp)
                            LetsSignBottomNavTab.WAITING_OTHERS -> HourglassIcon(color = iconColor, size = 22.dp)
                            LetsSignBottomNavTab.CONTACTS,
                            LetsSignBottomNavTab.SIGNATURES,
                            LetsSignBottomNavTab.PROFILE -> UserProfileIcon(color = iconColor, size = 22.dp)
                        }
                    }
                    Text(
                        text = tab.label,
                        color = textColor,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
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
    totalSteps: Int = 6,
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
                    .width(40.dp)
                    .height(40.dp)
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
                        .width(32.dp)
                        .height(32.dp)
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
                        CheckIcon(color = Color.White, size = 14.dp)
                    } else {
                        Text(
                            text = step.toString(),
                            color = if (isCurrent) LetsSignColors.Primary else LetsSignColors.Muted,
                            fontSize = 14.sp,
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
