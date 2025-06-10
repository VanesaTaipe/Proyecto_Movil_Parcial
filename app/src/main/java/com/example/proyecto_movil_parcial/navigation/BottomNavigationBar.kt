package com.example.proyecto_movil_parcial.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    var selectedNavigationIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

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
            icon = Icons.Default.Create,
            route = Screen.Desafíos.rout
        ),
        NavigationItem(
            title = "Perfil",
            icon = Icons.Default.Person,
            route = Screen.Perfil.rout
        )
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedNavigationIndex == index,
                onClick = {
                    selectedNavigationIndex = index
                    navController.navigate(item.route)
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
                            contentDescription = item.title
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (index == selectedNavigationIndex)
                            Color.Black
                        else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.onSecondary
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
    MaterialTheme {
        BottomNavigationBar(navController = rememberNavController())
    }
}