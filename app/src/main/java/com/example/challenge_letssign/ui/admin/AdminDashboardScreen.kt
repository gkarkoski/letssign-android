package com.example.challenge_letssign.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.LetsSignBottomNavTab
import com.example.challenge_letssign.ui.components.LetsSignBottomNavigationBar
import com.example.challenge_letssign.ui.components.LetsSignGradientHeader
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors

enum class AdminDocumentTab(val title: String) {
    SENT("Enviados"),
    PARTIALLY_SIGNED("Parcialmente\nAssinados"),
    COMPLETED("Concluídos")
}

@Composable
fun AdminDashboardScreen(
    adminId: String,
    adminName: String,
    onLogout: () -> Unit,
    onNewDocumentClick: () -> Unit = {}
) {
    var selectedBottomTab by rememberSaveable { mutableStateOf(LetsSignBottomNavTab.DOCUMENTS) }
    var selectedDocTab by rememberSaveable { mutableStateOf(AdminDocumentTab.SENT) }

    Scaffold(
        containerColor = LetsSignColors.Mist,
        bottomBar = {
            LetsSignBottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { selectedBottomTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedBottomTab) {
                LetsSignBottomNavTab.DOCUMENTS -> {
                    LetsSignGradientHeader(
                        title = "Documentos",
                        subtitle = "Acompanhe o que você enviou para assinatura."
                    )

                    AdminStatusTabRow(
                        selectedTab = selectedDocTab,
                        onTabSelected = { selectedDocTab = it }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(brush = LetsSignColors.IconGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                DocumentFileIcon(color = Color.White, size = 34.dp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Nenhum documento enviado ainda",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Envie seu primeiro PDF para assinatura e acompanhe cada etapa por aqui.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LetsSignColors.Muted
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            LetsSignPrimaryButton(
                                text = "Novo documento",
                                onClick = onNewDocumentClick,
                                leadingIcon = {
                                    PlusIcon(color = Color.White, size = 18.dp)
                                }
                            )
                        }
                    }
                }

                LetsSignBottomNavTab.SIGNATURES -> {
                    LetsSignGradientHeader(
                        title = "Signatários",
                        subtitle = "Cadastre e gerencie os signatários dos seus documentos."
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gestão de Signatários (Milestone 3)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = LetsSignColors.Muted
                            )
                        )
                    }
                }

                LetsSignBottomNavTab.PROFILE -> {
                    LetsSignGradientHeader(
                        title = "Perfil",
                        subtitle = "Você está autenticado como Administrador."
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = adminName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "ID: $adminId",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LetsSignSecondaryButton(
                            text = "Sair da conta",
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatusTabRow(
    selectedTab: AdminDocumentTab,
    onTabSelected: (AdminDocumentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LetsSignColors.Mist)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminDocumentTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.height(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                if (isSelected) LetsSignColors.Primary else LetsSignColors.Line
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    Challenge_letsSignTheme {
        AdminDashboardScreen(
            adminId = "admin-1",
            adminName = "Administrador LetsSign",
            onLogout = {}
        )
    }
}
