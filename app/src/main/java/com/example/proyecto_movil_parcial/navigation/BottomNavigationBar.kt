package com.example.proyecto_movil_parcial.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_movil_parcial.R
import com.example.proyecto_movil_parcial.ui.theme.Proyecto_Movil_parcialTheme

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    // Obtener la ruta actual del NavController
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        NavigationItem(
            title = "Inicio",
            icon = Icons.Default.Home,
            route = Screen.Inico.rout
        ),
        NavigationItem(
            title = "Mi Diccionario",
            icon = Icons.Default.FavoriteBorder,
            route = Screen.Diccionario.rout
        ),
        NavigationItem(
            title = "Desafíos",
            painter = painterResource(id = R.drawable.trofeo),
            route = Screen.Desafios.rout
        ),
        NavigationItem(
            title = "Perfil",
            icon = Icons.Default.Person,
            route = Screen.Perfil.rout
        )
    )

    // Encontrar el índice actual basado en la ruta
    val selectedIndex = navigationItems.indexOfFirst { it.route == currentRoute }
        .takeIf { it >= 0 } ?: 0

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.onSecondary) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    // Solo navegar si no estamos ya en esa pantalla
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Limpiar el back stack hasta el destino inicial
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Evitar múltiples copias de la misma pantalla
                            launchSingleTop = true
                            // Restaurar estado si existe
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    } else if (item.painter != null) {
                        Icon(
                            painter = item.painter,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                 label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(

                    selectedIconColor = Color(0xFFE67431),
                    selectedTextColor = Color(0xFFE67431),

                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,

                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector? = null,
    val painter: Painter? = null,
    val route: String
)

@Preview
@Composable
fun BottomNavigationBarPreview() {
    Proyecto_Movil_parcialTheme {

            BottomNavigationBar(navController = rememberNavController())
    }

}


