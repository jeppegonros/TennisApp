package com.example.tennisapp.view.navigation

sealed class Screen(val route: String, val label: String) {
    object Welcome : Screen("welcome", "Home")
    object Live : Screen("live", "Live")
    object Results : Screen("results", "Results")
    object Summary : Screen("summary", "Past Results")
}