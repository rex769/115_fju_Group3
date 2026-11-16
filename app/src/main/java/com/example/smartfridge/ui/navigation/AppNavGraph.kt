package com.example.smartfridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartfridge.ui.screens.add_edit.AddEditFoodScreen
import com.example.smartfridge.ui.screens.fridge.FridgeScreen
import com.example.smartfridge.ui.screens.history.HistoryScreen
import com.example.smartfridge.ui.screens.recipe.RecipeScreen
import com.example.smartfridge.ui.screens.shopping.ShoppingScreen

object NavDestinations {
    const val FRIDGE_SCREEN = "fridge"
    const val ADD_FOOD_SCREEN = "add_food"
    const val SHOPPING_SCREEN = "shopping"
    const val RECIPE_SCREEN = "recipe"
    const val HISTORY_SCREEN = "history"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.FRIDGE_SCREEN
    ) {
        composable(NavDestinations.FRIDGE_SCREEN) {
            FridgeScreen(
                onNavigateToAddFood = { navController.navigate("${NavDestinations.ADD_FOOD_SCREEN}?foodItemId=0") },
                onNavigateToEditFood = { foodItemId -> navController.navigate("${NavDestinations.ADD_FOOD_SCREEN}?foodItemId=$foodItemId") },
                onNavigateToShopping = { navController.navigate(NavDestinations.SHOPPING_SCREEN) },
                onNavigateToRecipe = { navController.navigate(NavDestinations.RECIPE_SCREEN) },
                onNavigateToHistory = { navController.navigate(NavDestinations.HISTORY_SCREEN) }
            )
        }

        composable(
            route = "${NavDestinations.ADD_FOOD_SCREEN}?foodItemId={foodItemId}",
            arguments = listOf(
                navArgument("foodItemId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val foodItemId = backStackEntry.arguments?.getLong("foodItemId") ?: 0L
            AddEditFoodScreen(
                foodItemId = foodItemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.SHOPPING_SCREEN) {
            ShoppingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.RECIPE_SCREEN) {
            RecipeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.HISTORY_SCREEN) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
