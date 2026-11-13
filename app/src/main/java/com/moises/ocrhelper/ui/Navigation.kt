package com.moises.ocrhelper.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moises.ocrhelper.OCRHelperApp
import com.moises.ocrhelper.camera.CameraScreen
import com.moises.ocrhelper.viewmodel.DetailViewModel
import com.moises.ocrhelper.viewmodel.HomeViewModel
import com.moises.ocrhelper.viewmodel.ResultViewModel

/** Rutas de la app. Los argumentos van entre llaves. */
private object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val RESULT = "result/{imageUri}"
    const val DOCUMENT = "document/{documentId}"

    // La URI lleva "/" y ":", así que se codifica para que quepa en la ruta
    fun result(imageUri: String) = "result/${Uri.encode(imageUri)}"
    fun document(documentId: Long) = "document/$documentId"
}

@Composable
fun OCRHelperNavHost() {
    val navController = rememberNavController()
    val container = (LocalContext.current.applicationContext as OCRHelperApp).container

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel(
                    factory = HomeViewModel.factory(container.documentRepository)
                ),
                onScan = { navController.navigate(Routes.CAMERA) },
                onImagePicked = { imageUri -> navController.navigate(Routes.result(imageUri)) },
                onOpenDocument = { id -> navController.navigate(Routes.document(id)) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onImageCaptured = { imageUri ->
                    navController.navigate(Routes.result(imageUri)) {
                        // Quita la cámara de la pila: "atrás" desde el resultado vuelve al inicio
                        popUpTo(Routes.CAMERA) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = checkNotNull(backStackEntry.arguments?.getString("imageUri"))
            ResultScreen(
                viewModel = viewModel(
                    factory = ResultViewModel.factory(
                        imageUri = imageUri,
                        textExtractor = container.textExtractor,
                        repository = container.documentRepository
                    )
                ),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }

        composable(
            route = Routes.DOCUMENT,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val documentId = checkNotNull(backStackEntry.arguments).getLong("documentId")
            DetailScreen(
                viewModel = viewModel(
                    factory = DetailViewModel.factory(documentId, container.documentRepository)
                ),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
