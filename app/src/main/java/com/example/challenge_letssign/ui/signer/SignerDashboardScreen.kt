package com.example.challenge_letssign.ui.signer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.LetsSignBottomNavTab
import com.example.challenge_letssign.ui.components.LetsSignBottomNavigationBar
import com.example.challenge_letssign.ui.components.LetsSignGradientHeader
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun SignerDashboardScreen(
    signerId: String,
    signerName: String,
    onLogout: () -> Unit
) {
    var selectedBottomTab by rememberSaveable { mutableStateOf(LetsSignBottomNavTab.SIGNATURES) }

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
                LetsSignBottomNavTab.DOCUMENTS,
                LetsSignBottomNavTab.SIGNATURES -> {
                    LetsSignGradientHeader(
                        title = "Assinaturas",
                        subtitle = "Olá, $signerName. Confira os documentos aguardando sua assinatura."
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
                                text = "Nenhum documento pendente",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Quando um documento for enviado para você assinar, ele aparecerá aqui.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LetsSignColors.Muted
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                LetsSignBottomNavTab.PROFILE -> {
                    LetsSignGradientHeader(
                        title = "Perfil",
                        subtitle = "Você está autenticado como Signatário."
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = signerName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "ID: $signerId",
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

@Preview(showBackground = true)
@Composable
private fun SignerDashboardScreenPreview() {
    Challenge_letsSignTheme {
        SignerDashboardScreen(
            signerId = "signer-1",
            signerName = "Carlos Silva",
            onLogout = {}
        )
    }
}
